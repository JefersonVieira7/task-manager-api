# Task Manager API

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Flyway](https://img.shields.io/badge/Flyway-migrations-red?logo=flyway)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Status](https://img.shields.io/badge/Status-Em%20desenvolvimento-brightgreen)

REST API para gerenciamento de listas de tarefas (Task Lists) e tarefas (Tasks),
desenvolvida com Java 21 e Spring Boot 4.

## Tecnologias

- **Java 21**
- **Spring Boot 4.0**
- **Spring Data JPA** + **Hibernate 7**
- **PostgreSQL** (produção) / **H2** (testes)
- **Flyway** — migrations de banco de dados
- **Bean Validation** — validação de entrada
- **SpringDoc OpenAPI** — documentação automática (Swagger)
- **JUnit 5** + **Mockito** — testes unitários
- **Docker** + **Docker Compose**

## Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven 3.9+ (ou use o wrapper `./mvnw`)

## Como rodar localmente

### 1. Clone o repositório

```bash
git clone https://github.com/JefersonVieira7/task-manager-api.git
cd task-manager-api
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
# Edite o .env com sua senha do banco
```

### 3. Suba o banco de dados

```bash
docker-compose up -d
```

> O Flyway criará as tabelas automaticamente na primeira execução.

### 4. Rode a aplicação

Configure as variáveis de ambiente no IntelliJ (DB_URL, DB_USERNAME, DB_PASSWORD)
ou exporte no terminal antes de rodar:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tasks_dev
export DB_USERNAME=postgres
export DB_PASSWORD=sua_senha
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`

## Documentação

Após subir a aplicação, acesse a documentação interativa:

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Endpoints

### Task Lists

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/task-lists` | Lista todas as task lists |
| POST | `/api/task-lists` | Cria uma nova task list |
| GET | `/api/task-lists/{id}` | Busca task list por ID |
| PUT | `/api/task-lists/{id}` | Atualiza uma task list |
| DELETE | `/api/task-lists/{id}` | Deleta uma task list |

### Tasks

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/task-lists/{id}/tasks` | Lista todas as tasks de uma lista |
| POST | `/api/task-lists/{id}/tasks` | Cria uma nova task |
| GET | `/api/task-lists/{id}/tasks/{taskId}` | Busca task por ID |
| PUT | `/api/task-lists/{id}/tasks/{taskId}` | Atualiza uma task |
| DELETE | `/api/task-lists/{id}/tasks/{taskId}` | Deleta uma task |

## Exemplos de uso

### Criar uma Task List

```bash
curl -X POST http://localhost:8080/api/task-lists \
  -H "Content-Type: application/json" \
  -d '{"title": "Estudos", "description": "Lista de estudos de programação"}'
```

Resposta `201 Created`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Estudos",
  "description": "Lista de estudos de programação",
  "count": 0,
  "progress": null,
  "tasks": []
}
```

### Criar uma Task

```bash
curl -X POST http://localhost:8080/api/task-lists/{task_list_id}/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Estudar Spring Boot",
    "description": "Completar etapas do projeto",
    "priority": "HIGH",
    "dueDate": "2026-12-31T23:59:00"
  }'
```

Resposta `201 Created`:
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "title": "Estudar Spring Boot",
  "description": "Completar etapas do projeto",
  "status": "OPEN",
  "priority": "HIGH",
  "dueDate": "2026-12-31T23:59:00",
  "taskListId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Erros padronizados

Todos os erros seguem o formato:
```json
{
  "timestamp": "2026-05-07T21:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task list not found with id: 123",
  "path": "/api/task-lists/123"
}
```

## Testes

```bash
./mvnw test
```

## Estrutura do projeto


```text
src/
├── main/
│   ├── java/com/jefersondev/tasks/
│   │   ├── config/          # FlywayConfig, OpenApiConfig
│   │   ├── controllers/     # TaskListController, TasksController, GlobalExceptionHandler
│   │   ├── domain/
│   │   │   └── entities/    # Task, TaskList, TaskStatus, TaskPriority
│   │   │       └── dto/     # TaskDto, TaskListDto
│   │   ├── exceptions/      # ResourceNotFoundException, BusinessException
│   │   ├── mappers/         # TaskMapper, TaskListMapper
│   │   ├── repositories/    # TaskRepository, TaskListRepository
│   │   └── services/        # TaskService, TaskListService e implementações
│   └── resources/
│       ├── db/migration/    # V1, V2 — scripts Flyway
│       └── application*.properties
└── test/
└── java/com/jefersondev/tasks/
├── controllers/     # TaskListControllerTest
└── services/        # TaskListServiceImplTest

```

## Autor

**Jeferson Vieira** — estudante de Engenharia de Software (3º semestre)

[![GitHub](https://img.shields.io/badge/GitHub-JefersonVieira7-black?logo=github)](https://github.com/JefersonVieira7)