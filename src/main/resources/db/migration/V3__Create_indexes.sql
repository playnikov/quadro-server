
-- =============================================
-- ИНДЕКСЫ ДЛЯ ПРОИЗВОДИТЕЛЬНОСТИ
-- =============================================

-- Пользователи
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Проекты
CREATE INDEX idx_projects_created_by ON projects(created_by);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_type ON projects(project_type);
CREATE INDEX idx_projects_key ON projects(key);
CREATE INDEX idx_projects_created_at ON projects(created_at);

-- Участники проектов
CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);

-- Задачи
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_reporter ON tasks(reporter_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);

-- Комментарии
CREATE INDEX idx_task_comments_task ON task_comments(task_id);
CREATE INDEX idx_task_comments_author ON task_comments(author_id);
CREATE INDEX idx_task_comments_created_at ON task_comments(created_at);

-- Файлы
CREATE INDEX idx_task_attachments_task ON task_attachments(task_id);
CREATE INDEX idx_task_attachments_uploaded_by ON task_attachments(uploaded_by);

-- Уведомления
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_related_entity ON notifications(related_entity_type, related_entity_id);

-- Настройки уведомлений
CREATE INDEX idx_notification_settings_telegram ON user_notification_settings(telegram_enabled, telegram_chat_id);

-- Отчеты
CREATE INDEX idx_report_requests_user_id ON report_requests(user_id);
CREATE INDEX idx_report_requests_status ON report_requests(status);
CREATE INDEX idx_report_requests_created_at ON report_requests(created_at);
CREATE INDEX idx_report_parameters_dates ON report_parameters(date_from, date_to);

-- Git интеграции
CREATE INDEX idx_git_integrations_user ON git_integrations(user_id);
CREATE INDEX idx_git_integrations_provider ON git_integrations(provider);

-- Git связи проектов
CREATE INDEX idx_project_git_links_project ON project_git_links(project_id);
CREATE INDEX idx_project_git_links_provider ON project_git_links(git_provider);

-- Спринты
CREATE INDEX idx_sprints_project ON sprints(project_id);
CREATE INDEX idx_sprints_dates ON sprints(start_date, end_date);
CREATE INDEX idx_sprints_status ON sprints(status);

-- Задачи спринтов
CREATE INDEX idx_sprint_tasks_sprint ON sprint_tasks(sprint_id);
CREATE INDEX idx_sprint_tasks_task ON sprint_tasks(task_id);