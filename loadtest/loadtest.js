import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const errorCounter = new Counter('errors');

const isStress = __ENV.STRESS_MODE === 'true';

let stages, usersToCreate, sleepDuration, thresholdsConfig;

if (isStress) {
  const maxVU = parseInt(__ENV.MAX_VU) || 100;
  stages = [
    { duration: '1m', target: maxVU },
    { duration: '5m', target: maxVU },
    { duration: '30s', target: 0 },
  ];
  usersToCreate = maxVU * 2;
  sleepDuration = 0.5;
  thresholdsConfig = {};
} else {
  stages = [
    { duration: '30s', target: 30 },
    { duration: '10m', target: 30 },
    { duration: '30s', target: 0 },
  ];
  usersToCreate = 50;
  sleepDuration = 2;
  thresholdsConfig = {
    http_req_duration: ['p(95)<6000'],
    http_req_failed: ['rate<0.10'],
    checks: ['rate>0.75'],
  };
}

export let options = {
  setupTimeout: '2m',
  stages: stages,
  thresholds: thresholdsConfig,
};

const TASK_STATUSES = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'DONE', 'IN_REVIEW', 'CANCELLED'];
const COMMENTS = [
  '# Это хорошая задача',
  'Пожалуйста, исправь `dockerfile`',
  'Я протестирую работу',
  'Готово, отправил на проверку',
  'Нужно больше информации',
  'Одобрено',
  'Открыто заново, нужно доработать',
  'Тестовый комментарий от ' + Date.now(),
];

export function setup() {
  const baseUrl = __ENV.BASE_URL || 'http://localhost';

  const adminUser = { username: 'superadmin', password: 'Admin123' };
  let loginAdminRes = http.post(baseUrl + '/api/auth/login', JSON.stringify({
    name: adminUser.username,
    password: adminUser.password,
  }), { headers: { 'Content-Type': 'application/json' } });

  if (!check(loginAdminRes, { 'admin login': (r) => r.json().success === true })) {
    throw new Error('Admin login failed');
  }

  const adminToken = loginAdminRes.json().data.token;
  const adminHeaders = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + adminToken,
  };

  let projects = [];
  const projectsCount = parseInt(__ENV.PROJECTS_COUNT) || 3;

  let existingProjectsRes = http.get(baseUrl + '/api/projects/my', { headers: adminHeaders });
  if (existingProjectsRes.json().success && existingProjectsRes.json().data) {
    const existingProjects = existingProjectsRes.json().data;
    for (let i = 0; i < existingProjects.length && projects.length < projectsCount; i++) {
      const p = existingProjects[i];
      if (p.key && p.key.startsWith('PROJ')) {
        let inviteRes = http.post(baseUrl + '/api/projects/invitations?id=' + p.id, JSON.stringify({
          role: 'MEMBER',
          type: 'LINK',
          expiresInDays: 7,
        }), { headers: adminHeaders });
        if (inviteRes.json().success) {
          projects.push({
            id: p.id,
            inviteToken: inviteRes.json().data.token,
          });
        } else {
          console.warn(`Failed to get invite for existing project ${p.id}`);
        }
      }
    }
  }

  for (let i = projects.length; i < projectsCount; i++) {
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

  let users = [];
  for (let i = 0; i < usersToCreate; i++) {
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

  console.log(`Setup done: ${users.length} users, ${projects.length} projects, stress mode = ${isStress}`);
  return { baseUrl: baseUrl, projects: projects, users: users };
}

export default function (data) {
  const { baseUrl, projects, users } = data;
  if (users.length === 0 || projects.length === 0) return;

  const vuIndex = __VU % users.length;
  const user = users[vuIndex];
  if (!user) return;

  // Логин
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

  let myProjectsRes = http.get(baseUrl + '/api/projects/my', { headers: headers });
  if (!myProjectsRes.json().success) {
    console.error(`[ERROR] Failed to get projects for ${user.username}: ${myProjectsRes.body}`);
    errorCounter.add(1);
    return;
  }

  const myProjects = myProjectsRes.json().data || [];
  let isMember = myProjects.some(p => p.id === project.id);

  if (!isMember) {
    let acceptRes = http.put(baseUrl + '/api/projects/invite?token=' + project.inviteToken, '{}', { headers: headers });
    if (!check(acceptRes, { 'accept invitation': (r) => r.json().success === true })) {
      console.error(`[ERROR] Accept invite failed for ${user.username}, project ${project.id}: ${acceptRes.body}`);
      errorCounter.add(1);
      sleep(1);
      return;
    }
  }

  // --- Работа с задачами ---
  const action = Math.random();

  // 1. Создать задачу
  if (action < 0.3) {
    let createRes = http.post(baseUrl + '/api/tasks', JSON.stringify({
      title: `Load test task ${__VU}-${__ITER}`,
      projectId: project.id,
      description: 'Test description',
      priority: ['LOW', 'MEDIUM', 'HIGH'][Math.floor(Math.random() * 3)],
      type: 'TASK',
    }), { headers: headers });
    if (!check(createRes, { 'create task': (r) => r.json().success === true })) {
      errorCounter.add(1);
    }
  }
  // 2. Получить задачи (30%)
  else if (action < 0.6) {
    let getRes = http.get(baseUrl + '/api/tasks/project?projectId=' + project.id, { headers: headers });
    if (!check(getRes, { 'get tasks': (r) => r.json().success === true })) {
      errorCounter.add(1);
    }
  }
  // 3. Обновить задачу и/или добавить комментарий (20%)
  else if (action < 0.8) {
    let getRes = http.get(baseUrl + '/api/tasks/project?projectId=' + project.id, { headers: headers });
    if (getRes.json().success && getRes.json().data && getRes.json().data.length > 0) {
      const tasks = getRes.json().data;
      const task = tasks[Math.floor(Math.random() * tasks.length)];
      const taskId = task.id;

      // С вероятностью 0.5 меняем статус
      if (Math.random() < 0.5) {
        const newStatus = TASK_STATUSES[Math.floor(Math.random() * TASK_STATUSES.length)];
        let updateRes = http.patch(baseUrl + '/api/tasks?taskId=' + taskId, JSON.stringify({
          title: task.title,
          status: newStatus,
        }), { headers: headers });
        if (!check(updateRes, { 'update task status': (r) => r.json().success === true })) {
          errorCounter.add(1);
        }
      }

      // С вероятностью 0.5 добавляем комментарий
      if (Math.random() < 0.5) {
        const commentText = COMMENTS[Math.floor(Math.random() * COMMENTS.length)] + ` (iter ${__ITER})`;
        let commentRes = http.post(baseUrl + '/api/tasks/comments', JSON.stringify({
          taskId: taskId,
          content: commentText,
        }), { headers: headers });
        if (!check(commentRes, { 'add comment': (r) => r.json().success === true })) {
          errorCounter.add(1);
        }
      }
    } else {
      let createRes = http.post(baseUrl + '/api/tasks', JSON.stringify({
        title: `Task for comment/update ${__VU}-${__ITER}`,
        projectId: project.id,
      }), { headers: headers });
      if (!check(createRes, { 'create temp task': (r) => r.json().success === true })) {
        errorCounter.add(1);
      }
    }
  }
  // 4. Удалить задачу (20%)
  else {
    let getRes = http.get(baseUrl + '/api/tasks/project?projectId=' + project.id, { headers: headers });
    if (getRes.json().success && getRes.json().data && getRes.json().data.length > 0) {
      const taskId = getRes.json().data[0].id;
      let deleteRes = http.del(baseUrl + '/api/tasks?taskId=' + taskId, '{}', { headers: headers });
      if (!check(deleteRes, { 'delete task': (r) => r.status === 204 })) {
        errorCounter.add(1);
      }
    }
  }

  sleep(sleepDuration);
}