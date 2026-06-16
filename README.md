# Quadro Platform — серверная часть

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-3.4.0-orange.svg)](https://ktor.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<div align="center">
  <strong>Микросервисная архитектура для управления проектами по методологии Kanban</strong>
  <br/><br/>
  <img src="https://img.shields.io/badge/Status-Active-brightgreen" alt="Status Active"/>
  <img src="https://img.shields.io/badge/Architecture-Microservices-blue" alt="Microservices Architecture"/>
</div>

---

## 📌 Содержание

- [Особенности](#-особенности)
- [Технологический стек](#-технологический-стек)
- [Структура проекта](#-структура-проекта)
- [Быстрый старт](#-быстрый-старт)
- [Установка и запуск](#-установка-и-запуск)
- [Переменные окружения](#-переменные-окружения)
- [Микросервисы](#-микросервисы)
- [Сборка и развёртывание](#-сборка-и-развёртывание)
- [Мониторинг](#-мониторинг)
- [Лицензия](#-лицензия)

---

## ✨ Особенности

- 🏗️ **Микросервисная архитектура** — независимые сервисы для каждой функциональности
- 🔐 **JWT-аутентификация** с рефреш-токенами и автоматическим обновлением
- 📊 **Kanban-доска** с управлением задачами, спринтами и проектами
- 💬 **Система уведомлений** в реальном времени через WebSockets и Kafka
- 📝 **Форматирование Markdown** в комментариях к задачам
- 📦 **Асинхронная коммуникация** через Kafka для масштабируемости
- 🔄 **Flyway** для миграций базы данных

---

## 🧩 Технологический стек

### Backend (Все сервисы)

| Технология | Назначение |
|------------|------------|
| **Kotlin 2.0** | Основной язык программирования |
| **Ktor 3.4** | Серверный фреймворк |
| **Exposed** | ORM для PostgreSQL |
| **Flyway** | Миграции базы данных |
| **Koin** | Dependency Injection |
| **Kotlinx Serialization** | JSON-сериализация |
| **JWT (java.jwt)** | Аутентификация |
| **Kafka Clients** | Асинхронная коммуникация между сервисами |
| **Netty** | HTTP-сервер |

### Инфраструктура

| Технология | Назначение |
|------------|------------|
| **Docker** | Контейнеризация |
| **Docker Compose** | Оркестрация контейнеров |
| **PostgreSQL 16** | База данных |
| **Kafka** | Message broker |

---

## 📁 Структура проекта

```
quadro-server/
├── api-gateway/              # API Gateway (координирует запросы между микросервисами)
├── auth-service/            # Сервис аутентификации и управления пользователями
├── project-service/         # Сервис управления проектами
├── team-service/            # Сервис управления командами
├── task-service/            # Сервис управления задачами и спринтами
├── notification-service/    # Сервис уведомлений (WebSockets + Kafka)
├── shared/                  # Общий модуль для всех сервисов
├── init-scripts/            # SQL-скрипты инициализации БД
├── docker-compose.yaml      # Конфигурация всех сервисов
├── Dockerfile.postgres      # Dockerfile для PostgreSQL с инициализацией
├── gradle.properties        # Глобальные настройки Gradle
├── build.gradle.kts         # Root Gradle конфигурация
└── settings.gradle.kts      # Настройки проекта
```

### Микросервисы

Каждый микросервис содержит:

```
service-name/
├── src/
│   └── main/
│       └── kotlin/
│           └── com.quadro.{service}/
│               ├── domain/      # Бизнес-логика и репозитории
│               ├── presentation/ # REST API endpoints
│               ├── plugins/     # Ktor плагины
│               └── di/          # Dependency Injection модули
├── build.gradle.kts
└── Dockerfile
```

### Основные схемы базы данных

- `auth_schema` — пользователи, роли, токены
- `project_schema` — проекты, доски
- `task_schema` — задачи, спринты, статусы
- `team_schema` — команды, участники
- `notification_schema` — уведомления

---

## 🚀 Быстрый старт

### Требования

- [JDK 21+](https://adoptium.net/) (для разработки)
- [Docker](https://www.docker.com/) (для развёртывания)
- [Docker Compose](https://docs.docker.com/compose/)

### Запуск через Docker Compose

```bash
# Установите переменные окружения (см. раздел "Переменные окружения")
# Или используйте значения по умолчанию (см. .env.example)

# Запустите все сервисы
docker-compose up -d

# Проверьте статус контейнеров
docker-compose ps
```

После запуска:

- **API Gateway**: `http://localhost:8080`
- **pgAdmin** (для управления БД): `http://localhost:5050`
- **Grafana** (мониторинг): `http://localhost:3000` (admin/admin)

---

## 🔧 Переменные окружения

### Обязательные переменные

Создайте файл `.env` в корне проекта:

```env
# PostgreSQL
POSTGRES_USER=quadro_user
POSTGRES_PASSWORD=your_secure_password
POSTGRES_DB=quadro_db
POSTGRES_VOLUME=postgres_data

# Database URL (внутренний URL контейнеров)
DB_HOST=postgres
DB_NAME=quadro_db
DB_USER=quadro_user
DB_PASSWORD=your_secure_password
DB_POOL_SIZE=10

# Kafka
KAFKA_PORT_EXTERNAL=9092
KAFKA_INTERNAL_HOST=kafka
KAFKA_INTERNAL_PORT=29092
KAFKA_EXTERNAL_HOST=localhost
KAFKA_EXTERNAL_PORT=9092
KAFKA_REPLICATION_FACTOR=1
KAFKA_NODE_ID=1
KAFKA_CLUSTER_ID=cx33_hoaQrG0wLT9oWx4Gw

# Redis
REDIS_PASSWORD=your_redis_password
REDIS_VOLUME=redis_data

# JWT
JWT_SECRET=your_super_secret_jwt_key_change_in_production
JWT_ISSUER=quadro-auth
JWT_AUDIENCE=quadro-app
JWT_REALM=quadro-realm
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

# Initial user (seed data)
SEED_USERNAME=admin
SEED_EMAIL=admin@quadro.local
SEED_PASSWORD=admin_password

# Docker registry (для продакшена)
CR_URI=
REPOSITORY=quadro-server
VERSION=latest

# Volumes
KAFKA_VOLUME=kafka_data
MINIO_VOLUME=minio_data
PROMETHEUS_DATA=prometheus_data
GRAFANA_DATA=grafana_data
```

### Настройка переменных

#### Linux/macOS (bash)

```bash
export POSTGRES_USER=quadro_user
export POSTGRES_PASSWORD=your_secure_password
# ... остальные переменные
```

#### Windows PowerShell

```powershell
$env:POSTGRES_USER="quadro_user"
$env:POSTGRES_PASSWORD="your_secure_password"
# ... остальные переменные
```

---

## 🏢 Микросервисы

### 🔐 auth-service

**Порт**: 8081  
**Схема**: `auth_schema`  
**Функционал**:
- Регистрация и аутентификация пользователей
- Управление JWT токенами
- Генерация рефреш-токенов
- Хэширование паролей (bcrypt)

**API Endpoints**:
- `POST /auth/register` — регистрация
- `POST /auth/login` — аутентификация
- `POST /auth/refresh` — обновление токена
- `POST /auth/logout` — выход

### 📁 project-service

**Порт**: 8082  
**Схема**: `project_schema`  
**Функционал**:
- Создание и управление проектами
- Управление досками Kanban
- Пользовательские роли в проектах

**API Endpoints**:
- `GET /projects` — список проектов
- `POST /projects` — создание проекта
- `GET /projects/{id}` — получение проекта
- `PUT /projects/{id}` — обновление проекта
- `DELETE /projects/{id}` — удаление проекта

### 👥 team-service

**Порт**: 8083  
**Схема**: `team_schema`  
**Функционал**:
- Управление командами
- Добавление/удаление участников
- Управление ролями

**API Endpoints**:
- `GET /teams` — список команд
- `POST /teams` — создание команды
- `GET /teams/{id}/members` — участники команды

### 📋 task-service

**Порт**: 8084  
**Схема**: `task_schema`  
**Функционал**:
- Управление задачами
- Спринты и их статусы
- Drag-and-drop перетаскивание
- Комментарии в Markdown
- PDF экспорт задач

**API Endpoints**:
- `GET /tasks` — список задач
- `POST /tasks` — создание задачи
- `PUT /tasks/{id}` — обновление задачи
- `DELETE /tasks/{id}` — удаление задачи
- `POST /tasks/{id}/move` — перемещение задачи

### 🔔 notification-service

**Порт**: 8085  
**Схема**: `notification_schema`  
**Функционал**:
- Вебсокеты для реалтайм уведомлений
- Kafka интеграция для асинхронной доставки
- Сохранение истории уведомлений

**API Endpoints**:
- `GET /ws/notifications` — WebSocket подключение
- `GET /notifications` — история уведомлений

### 🚪 api-gateway

**Порт**: 8080  
**Функционал**:
- Router всех запросов к соответствующим микросервисам
- Rate limiting (Redis-based)
- Metrics (Prometheus)
- CORS support
- Request logging
- Health check endpoint

---

## 🏗️ Сборка и развёртывание

### Локальная сборка

```bash
# Сборка всех сервисов
./gradlew build

# Сборка конкретного сервиса
./gradlew :auth-service:build
./gradlew :project-service:build
./gradlew :task-service:build

# Запуск тестов
./gradlew test

# Запуск с покрытием
./gradlew koverHtmlReport
```

### Сборка Docker образов

#### Вариант 1: Автоматическая сборка через Docker Compose

```bash
# Docker Compose автоматически соберет образы
docker-compose build

# Или с кэшированием
docker-compose build --compress
```

#### Вариант 2: Ручная сборка каждого сервиса

```bash
# Сборка auth-service
./gradlew :auth-service:build
docker build -t ${CR_URI}/${REPOSITORY}:auth-service.${VERSION} ./auth-service

# Сборка project-service
./gradlew :project-service:build
docker build -t ${CR_URI}/${REPOSITORY}:project-service.${VERSION} ./project-service

# И так далее для всех сервисов...
```

### Развёртывание

```bash
# Запуск всех сервисов
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка сервисов
docker-compose down

# Остановка с удалением томов
docker-compose down -v
```

---

## 📊 Мониторинг

### Prometheus

- **Эндпоинт**: `http://localhost:9090`
- **Порт в docker-compose**: 9090
- **Сбор метрик**: каждый эндпоинт `/metrics` у сервисов

### Grafana

- **URL**: `http://localhost:3000`
- **Логин**: `admin`
- **Пароль**: `admin`
- **Источник данных**: Prometheus (автоматически настроен)

### Loki (логи)

- **Эндпоинт**: `http://localhost:3100`
- **Сбор логов**: через Promtail

### Метрики сервисов

Каждый сервис предоставляет:

- `GET /metrics` — Prometheus метрики
- `GET /health` — статус здоровья сервиса

---

## 🔌 Интеграция с клиентом

Клиентское приложение (Compose Multiplatform) взаимодействует с сервером через:

- **REST API**: через API Gateway (`http://localhost:8080`)
- **WebSockets**: для реалтайм уведомлений (`ws://localhost:8080/ws/notifications`)
- **JWT**: для аутентификации

---

## 🛠️ Разработка

### Запуск отдельного сервиса локально

```bash
# Запустите только зависимости (PostgreSQL, Kafka)
docker-compose up -d postgres kafka

# Запустите сервис локально (не в Docker)
./gradlew :auth-service:run
```

### Миграции базы данных

```bash
# Применить миграции
./gradlew :auth-service:flywayMigrate
./gradlew :project-service:flywayMigrate
./gradlew :task-service:flywayMigrate

# Создать новую миграцию
./gradlew :auth-service:flywayMigrate -Pflyway.locations=filesystem:src/main/resources/db/migration
```

### Локальная отладка

```bash
# Запуск с дебагом
./gradlew :auth-service:run --debug-jvm

# Запуск с профилированием
./gradlew :auth-service:run -Dcom.sun.management.jmxremote
```

---

## 📄 Лицензия

Этот проект распространяется под лицензией **MIT**. Подробнее см. файл [LICENSE](LICENSE).
