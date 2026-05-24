import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import exec from 'k6/execution';

// Метрики
const createTaskDuration = new Trend('create_task_duration', true);
const updateTaskDuration = new Trend('update_task_duration', true);
const statsDuration = new Trend('stats_duration', true);

export const options = {
  setupTimeout: '5m',
  scenarios: {
    main_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 30 },
        { duration: '3m', target: 50 },
        { duration: '1m', target: 0 },
      ],
      gracefulRampDown: '100s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95) < 500', 'p(99) < 1000'],
    http_req_failed: ['rate < 0.02'],
    'http_req_duration{type:create_task}': ['p(95) < 600'],
    'http_req_duration{type:update_task}': ['p(95) < 400'],
    'http_req_duration{type:stats}': ['p(95) < 300'],
    create_task_duration: ['p(95) < 700'],
    update_task_duration: ['p(95) < 500'],
    stats_duration: ['p(95) < 350'],
  },
};

// ---------- API функции (без изменений, но защищены) ----------
function loginUser(baseUrl, username, password) {
  const res = http.post(`${baseUrl}/api/auth/login`, JSON.stringify({
    name: username,
    password: password
  }), { headers: { 'Content-Type': 'application/json' } });
  const ok = check(res, { 'логин успешен': (r) => r.status === 200 });
  if (!ok) return null;
  const body = res.json();
  if (body && body.data && body.data.token) {
    return body.data.token;
  }
  return null;
}

function createProject(baseUrl, adminToken, projectName, projectKey) {
  const payload = JSON.stringify({
    type: 'TEAM_MANAGED',
    name: projectName,
    key: projectKey,
    description: 'Нагрузочный проект',
    priority: 'LOW',
    visibility: 'PUBLIC'
  });
  const res = http.post(`${baseUrl}/api/projects`, payload, {
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    }
  });
  const ok = check(res, { 'проект создан': (r) => r.status === 201 });
  if (!ok) return null;
  const body = res.json();
  if (body && body.data && body.data.id) {
    return body.data.id;
  }
  return null;
}

function createInvitation(baseUrl, adminToken, projectId, userEmail) {
  const payload = JSON.stringify({
    role: 'MANAGER',
    type: 'EMAIL',
    identifier: userEmail,
    message: 'Приглашение в проект',
    expiresInDays: 7
  });
  const res = http.post(`${baseUrl}/api/projects/invitations?id=${projectId}`, payload, {
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    }
  });
  const ok = check(res, { 'приглашение создано': (r) => r.status === 201 });
  if (!ok) return null;
  const body = res.json();
  if (body && body.data && body.data.token) {
    return body.data.token;
  }
  return null;
}

function acceptInvitation(baseUrl, userToken, invitationToken) {
  const res = http.post(`${baseUrl}/api/projects/invite?token=${invitationToken}`, null, {
    headers: { 'Authorization': `Bearer ${userToken}` }
  });
  check(res, { 'приглашение принято': (r) => r.status === 200 });
}

function createTask(baseUrl, userToken, projectId, iteration) {
  const payload = JSON.stringify({
    title: `Задача от ВП ${exec.vu.idInTest} итерация ${iteration}`,
    projectId: projectId,
    description: 'Тестовое описание',
    priority: 'MEDIUM',
    type: 'TASK',
    assigneeId: null,
    assignedTeamId: null,
    sprintId: null,
    parentTaskId: null,
    storyPoints: 3,
    estimatedHours: 4.0,
    dueDate: new Date(Date.now() + 7*86400000).toISOString(),
    labels: ['loadtest']
  });
  const start = Date.now();
  const res = http.post(`${baseUrl}/api/tasks`, payload, {
    headers: {
      'Authorization': `Bearer ${userToken}`,
      'Content-Type': 'application/json'
    },
    tags: { type: 'create_task' }
  });
  createTaskDuration.add(Date.now() - start);
  const ok = check(res, { 'задача создана': (r) => r.status === 200 || r.status === 201 });
  if (!ok) return null;
  const body = res.json();
  if (body && body.data && body.data.id) {
    return body.data.id;
  }
  return null;
}

function updateTask(baseUrl, userToken, taskId) {
  const payload = JSON.stringify({
    status: 'IN_PROGRESS',
    loggedHours: 2.5
  });
  const start = Date.now();
  const res = http.patch(`${baseUrl}/api/tasks/${taskId}`, payload, {
    headers: {
      'Authorization': `Bearer ${userToken}`,
      'Content-Type': 'application/json'
    },
    tags: { type: 'update_task' }
  });
  updateTaskDuration.add(Date.now() - start);
  check(res, { 'задача обновлена': (r) => r.status === 200 });
}

function getProjectTasks(baseUrl, userToken, projectId) {
  const start = Date.now();
  const res = http.get(`${baseUrl}/api/tasks/project?projectId=${projectId}`, {
    headers: { 'Authorization': `Bearer ${userToken}` },
    tags: { type: 'stats' }
  });
  statsDuration.add(Date.now() - start);
  check(res, { 'список задач получен': (r) => r.status === 200 });
}

// ---------- ПОДГОТОВКА (выполняется один раз) ----------
export function setup() {
  const baseUrl = 'http://84.54.57.58';
  const users = [];

  // Регистрируем 30 пользователей
  for (let i = 0; i < 50; i++) {
    const username = `user_${i}_${Date.now()}`;
    const email = `${username}@mail.example`;
    const lastName = `lastName`;
    const firstName = `firstName`;
    const middleName = `middleName`;
    const password = 'Test123!';

    const regRes = http.post(`${baseUrl}/api/auth/register`, JSON.stringify({
      email, username, lastName, firstName, middleName, password
    }), { headers: { 'Content-Type': 'application/json' } });

    if (regRes.status === 201) {
      users.push({ username, password, email });
      console.log(`Зарегистрирован ${username}`);
    } else {
      console.error(`Ошибка регистрации ${username}: ${regRes.status}`);
    }
    sleep(0.1);
  }

  const adminToken = loginUser(baseUrl, 'playnikov', '12345678af');
  if (!adminToken) {
    console.error('Не удалось авторизовать администратора');
    return { baseUrl, users, projectIds: [] };
  }

  // Создаём проекты
  const projectIds = [];
  for (let p = 0; p < 5; p++) {
    const projectName = `Проект_${p}_${Date.now()}`;
    const projectKey = `PROJ2${p}`;
    const projId = createProject(baseUrl, adminToken, projectName, projectKey);
    if (projId) {
      projectIds.push(projId);
    } else {
      console.error(`Не удалось создать проект ${projectName}`);
    }
    sleep(0.5);
  }

  // Создаём приглашения и сразу принимаем их за каждого пользователя
  for (const user of users) {
    // Логинимся как пользователь
    const userToken = loginUser(baseUrl, user.username, user.password);
    if (!userToken) {
      console.error(`Не удалось залогинить ${user.username} для принятия приглашений`);
      continue;
    }
    // Для каждого проекта создаём приглашение админом и принимаем пользователем
    for (const proj of projectIds) {
      const invToken = createInvitation(baseUrl, adminToken, proj, user.email);
      if (invToken) {
        acceptInvitation(baseUrl, userToken, invToken);
        sleep(0.2);
      }
    }
  }

  return {
    baseUrl: baseUrl,
    users: users.map(u => ({ username: u.username, password: u.password, email: u.email })),
    projectIds: projectIds,
  };
}

// ---------- ОСНОВНАЯ НАГРУЗКА (без __VU.state и без принятия приглашений) ----------
export default function (data) {
  const { baseUrl, users, projectIds } = data;

  const vuId = exec.vu.idInTest;
  const user = users[vuId % users.length];
  if (!user) return;

  const token = loginUser(baseUrl, user.username, user.password);
  if (!token) {
    console.error(`Не удалось получить токен для ${user.username} ${user.password}`);
    return;
  }

  const randomProject = projectIds[Math.floor(Math.random() * projectIds.length)];
  // Используем простой псевдо-счетчик на основе времени и vuId
  const action = (Date.now() + vuId) % 10;

  if (action < 5) {
    const taskId = createTask(baseUrl, token, randomProject, vuId);
    if (taskId && action === 2) updateTask(baseUrl, token, taskId);
  } else if (action < 8) {
    getProjectTasks(baseUrl, token, randomProject);
  } else {
    http.get(`${baseUrl}/api/tasks/sprint?sprintId=some-id`, {
      headers: { 'Authorization': `Bearer ${token}` },
      tags: { type: 'view' }
    });
  }
  sleep(Math.random() * 5 + 0.5);
}

export function teardown(data) {
  console.log('Тест завершён');
}