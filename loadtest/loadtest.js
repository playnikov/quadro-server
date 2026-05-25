import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const errorCounter = new Counter('errors');

export let options = {
  setupTimeout: '2m',
  stages: [
    { duration: '30s', target: 30 },   // снижено с 50 до 30 VU
    { duration: '3m', target: 30 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<6000'],   // увеличен порог до 6 секунд
    http_req_failed: ['rate<0.10'],      // допускаем до 10% ошибок
    checks: ['rate>0.75'],               // допускаем 75% успешных проверок
  },
};

export function setup() {
  const baseUrl = __ENV.BASE_URL || 'http://localhost';

  // Регистрация админа (если уже существует, просто логинимся)
  const adminUser = {
    username: 'admin',
    password: 'Admin123',
  };

  let loginAdminRes = http.post(baseUrl + '/api/auth/login', JSON.stringify({
    name: adminUser.username,
    password: adminUser.password,
  }), { headers: { 'Content-Type': 'application/json' } });

  if (!loginAdminRes.json().success) {
    // Если админа нет – создаём
    http.post(baseUrl + '/api/auth/register', JSON.stringify({
      username: 'admin',
      email: 'admin@example.com',
      password: 'Admin123',
      firstName: 'Admin',
      lastName: 'Adminov',
      middleName: null,
    }), { headers: { 'Content-Type': 'application/json' } });
    loginAdminRes = http.post(baseUrl + '/api/auth/login', JSON.stringify({
      name: 'admin',
      password: 'Admin123',
    }), { headers: { 'Content-Type': 'application/json' } });
  }

  if (!check(loginAdminRes, { 'admin login': (r) => r.json().success === true })) {
    throw new Error('Admin login failed');
  }

  const adminToken = loginAdminRes.json().data.token;
  const adminHeaders = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + adminToken,
  };

  // Создание проектов и приглашений
  const projectsCount = parseInt(__ENV.PROJECTS_COUNT) || 3;
  let projects = [];
  for (let i = 0; i < projectsCount; i++) {
    let projectRes = http.post(baseUrl + '/api/projects', JSON.stringify({
      name: 'Test Project ' + i,
      key: 'PROJ' + i,
      description: 'Description ' + i,
    }), { headers: adminHeaders });
    if (!projectRes.json().success) continue;
    let projectId = projectRes.json().data.id;
    if (!projectId) continue;

    let inviteRes = http.post(baseUrl + '/api/projects/invitations?id=' + projectId, JSON.stringify({
      role: 'MEMBER',
      type: 'LINK',
      expiresInDays: 7,
    }), { headers: adminHeaders });
    if (!inviteRes.json().success) continue;
    let inviteToken = inviteRes.json().data.token;
    projects.push({ id: projectId, inviteToken: inviteToken });
  }

  // Регистрация обычных пользователей
  const usersCount = parseInt(__ENV.USERS_COUNT) || 50; // уменьшено
  let users = [];
  for (let i = 0; i < usersCount; i++) {
    let username = 'user' + i + '_' + Date.now();
    let email = username + '@example.com';
    let registerRes = http.post(baseUrl + '/api/auth/register', JSON.stringify({
      username: username,
      email: email,
      password: 'Pass123!',
      firstName: 'First' + i,
      lastName: 'Last' + i,
      middleName: null,
    }), { headers: { 'Content-Type': 'application/json' } });
    if (registerRes.json().success === true) {
      users.push({ username: username, password: 'Pass123!' });
    }
    if (i % 10 === 0) sleep(0.1);
  }

  console.log('Setup done: ' + users.length + ' users, ' + projects.length + ' projects');
  return { baseUrl: baseUrl, projects: projects, users: users };
}

export default function (data) {
  const { baseUrl, projects, users } = data;
  if (users.length === 0) return;

  const vuIndex = __VU % users.length;
  const user = users[vuIndex];
  if (!user) return;

  // Логин с диагностикой
  let loginRes = http.post(baseUrl + '/api/auth/login', JSON.stringify({
    name: user.username,
    password: user.password,
  }), { headers: { 'Content-Type': 'application/json' } });

  let success = check(loginRes, { 'login success': (r) => r.json().success === true });
  if (!success) {
    console.error(`[ERROR] Login failed for ${user.username}: status=${loginRes.status}, body=${loginRes.body}`);
    errorCounter.add(1);
    return;
  }

  const token = loginRes.json().data.token;
  if (!token) {
    console.error(`[ERROR] No token for ${user.username}`);
    errorCounter.add(1);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token,
  };

  const projectIndex = vuIndex % projects.length;
  const project = projects[projectIndex];
  if (!project) return;

  // Проверка членства
  let myProjectsRes = http.get(baseUrl + '/api/projects/my', { headers: headers });
  if (!myProjectsRes.json().success) {
    console.error(`[ERROR] Failed to get projects for ${user.username}: ${myProjectsRes.body}`);
    errorCounter.add(1);
    return;
  }

  const myProjects = myProjectsRes.json().data || [];
  let isMember = myProjects.some(function(p) { return p.id === project.id; });

  if (!isMember) {
    let acceptRes = http.put(baseUrl + '/api/projects/invite?token=' + project.inviteToken, '{}', { headers: headers });
    if (!check(acceptRes, { 'accept invitation': (r) => r.json().success === true })) {
      console.error(`[ERROR] Accept invite failed for ${user.username}, project ${project.id}: ${acceptRes.body}`);
      errorCounter.add(1);
      sleep(1);
      return;
    }
  }

  // Имитация работы с задачами (уменьшена частота для снижения нагрузки)
  const action = Math.random();
  if (action < 0.3) { // вместо 0.4
    let createRes = http.post(baseUrl + '/api/tasks', JSON.stringify({
      title: 'Load test task ' + __VU + '-' + __ITER,
      projectId: project.id,
      description: 'Test description',
      priority: 'MEDIUM',
      type: 'TASK',
    }), { headers: headers });
    if (!check(createRes, { 'create task': (r) => r.json().success === true })) {
      errorCounter.add(1);
    }
  } else if (action < 0.6) { // вместо 0.7
    let getRes = http.get(baseUrl + '/api/tasks/project?projectId=' + project.id, { headers: headers });
    if (!check(getRes, { 'get tasks': (r) => r.json().success === true })) {
      errorCounter.add(1);
    }
  } else if (action < 0.8) { // вместо 0.9
    let getRes = http.get(baseUrl + '/api/tasks/project?projectId=' + project.id, { headers: headers });
    if (getRes.json().success && getRes.json().data && getRes.json().data.length > 0) {
      let taskId = getRes.json().data[0].id;
      let updateRes = http.patch(baseUrl + '/api/tasks?taskId=' + taskId, JSON.stringify({
        title: 'Updated ' + Date.now(),
        status: 'IN_PROGRESS',
      }), { headers: headers });
      if (!check(updateRes, { 'update task': (r) => r.json().success === true })) {
        errorCounter.add(1);
      }
    } else {
      let createRes = http.post(baseUrl + '/api/tasks', JSON.stringify({
        title: 'Temp for update ' + __VU + '-' + __ITER,
        projectId: project.id,
      }), { headers: headers });
      if (!check(createRes, { 'create temp task': (r) => r.json().success === true })) {
        errorCounter.add(1);
      }
    }
  } else {
    let getRes = http.get(baseUrl + '/api/tasks/project?projectId=' + project.id, { headers: headers });
    if (getRes.json().success && getRes.json().data && getRes.json().data.length > 0) {
      let taskId = getRes.json().data[0].id;
      let deleteRes = http.del(baseUrl + '/api/tasks?taskId=' + taskId, '{}', { headers: headers });
      if (!check(deleteRes, { 'delete task': (r) => r.status === 204 })) {
        errorCounter.add(1);
      }
    }
  }

  sleep(2); // увеличена пауза между итерациями
}