-- =============================================
-- ТИПЫ ДАННЫХ
-- =============================================

-- Роли пользователей в системе
CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'EXECUTOR');

-- Статусы проектов
CREATE TYPE project_status AS ENUM ('ACTIVE', 'ARCHIVED', 'COMPLETED');

-- Типы проектов (локальные или Git)
CREATE TYPE project_type AS ENUM ('INTERNAL', 'GIT_LAB', 'GIT_HUB');

-- Статусы задач
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'REVIEW', 'DONE');

-- Приоритеты задач
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- Приоритеты уведомлений
CREATE TYPE notification_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- Типы связанных сущностей для уведомлений
CREATE TYPE related_entity_type AS ENUM ('TASK', 'PROJECT', 'USER', 'SYSTEM');

-- Типы отчетов
CREATE TYPE report_type AS ENUM (
    'PROJECT_PROGRESS',
    'TEAM_PRODUCTIVITY',
    'TASK_DISTRIBUTION',
    'TIME_ANALYTICS',
    'USER_ACTIVITY',
    'OVERDUE_TASKS',
    'CUSTOM'
);

-- Форматы отчетов
CREATE TYPE report_format AS ENUM ('PDF', 'EXCEL', 'JSON');

-- Статусы отчетов
CREATE TYPE report_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');

-- Git провайдеры
CREATE TYPE git_provider AS ENUM ('GITLAB', 'GITHUB');

-- Статусы спринтов
CREATE TYPE sprint_status AS ENUM ('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED');