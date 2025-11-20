-- =============================================
-- ФУНКЦИИ И ТРИГГЕРЫ
-- =============================================

-- Функция для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры для автоматического обновления updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_git_integrations_updated_at BEFORE UPDATE ON git_integrations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_project_git_links_updated_at BEFORE UPDATE ON project_git_links FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_sprints_updated_at BEFORE UPDATE ON sprints FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_notification_settings_updated_at BEFORE UPDATE ON user_notification_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Функция для проверки уникальности ключа проекта
CREATE OR REPLACE FUNCTION check_project_key_unique()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM projects WHERE key = NEW.key AND id != NEW.id) THEN
        RAISE EXCEPTION 'Project key must be unique';
END IF;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER check_project_key_unique BEFORE INSERT OR UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION check_project_key_unique();

-- Функция для автоматического добавления создателя как участника проекта
CREATE OR REPLACE FUNCTION add_creator_as_project_owner()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO project_members (project_id, user_id, role) VALUES (NEW.id, NEW.created_by, 'OWNER');
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER add_creator_as_project_owner AFTER INSERT ON projects FOR EACH ROW EXECUTE FUNCTION add_creator_as_project_owner();