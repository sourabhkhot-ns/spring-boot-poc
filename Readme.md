# 🧪 Employee Notification Microservices POC

This project is a **proof-of-concept (POC)** microservices setup using **Spring Boot**, featuring three independent services:

- `employee-management-service`: Handles employee operations, stores employees in a JSON file, and notifies on changes.
- `notification-service`: Receives, logs, and stores notifications in a JSON file via HTTP.
- `activity-service`: Receives, logs, and stores all activity events from other services in a JSON file.

These services are designed to be **containerized with Docker** and **deployed to AWS ECS Fargate** without a load balancer (public IPs are used for inter-service communication).

---

## 🗂 Project Structure

```
employee-notification-poc/
├── employee-management-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/employee/...
├── notification-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/notification/...
├── activity-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/activity/...
└── README.md
```

---

## 🧰 Technologies Used

- Java 21 (Eclipse Temurin, used in Dockerfiles)
- Spring Boot 3+
- Maven
- Docker
- AWS ECS Fargate
- **No database**: All data is stored in JSON files

---

## 🧑‍💼 employee-management-service

### ➕ Responsibilities

- Create, read, update, and delete employee records
- Store employees in a local `employees.json` file
- Notify `notification-service` via HTTP POST when an employee is created, updated, or deleted
- Send activity events to `activity-service` for all employee changes

### 📦 REST Endpoints

| Method | Endpoint               | Description             |
|--------|------------------------|-------------------------|
| POST   | `/api/employees`       | Create a new employee   |
| GET    | `/api/employees`       | Get all employees       |
| GET    | `/api/employees/{id}`  | Get employee by ID      |
| PUT    | `/api/employees/{id}`  | Update employee by ID   |
| DELETE | `/api/employees/{id}`  | Delete employee by ID   |
| GET    | `/api/health`          | Healthcheck endpoint    |

### 🔔 Notification Call

When an employee is created, updated, or deleted, this service makes an HTTP POST call to:

```
http://${NOTIFICATION_URL}/api/notifications
```

Payload:
```json
{
  "message": "Employee Created|Employee Updated|Employee Deleted",
  "employeeId": 1
}
```

The `NOTIFICATION_URL` must be provided via an **environment variable**.

### 🗃 Storage
- Employees are stored in a local `employees.json` file (no database is used).

### 📝 Logging
- All actions and errors are logged using SLF4J (console output by default).

---

## 📢 notification-service

### ➕ Responsibilities

* Accept, log, and store incoming notifications
* Expose a GET endpoint to retrieve all notifications
* Send activity events to `activity-service` for all received notifications

### 📦 REST Endpoints

| Method | Endpoint             | Description                    |
| ------ | -------------------- | ------------------------------ |
| POST   | `/api/notifications` | Accept and store a notification|
| GET    | `/api/notifications` | Get all stored notifications   |
| GET    | `/api/health`        | Healthcheck endpoint           |

### 📄 Request Format (POST)

```json
{
  "message": "Employee Created|Employee Updated|Employee Deleted",
  "employeeId": 1
}
```

### 📄 Response Format (GET)

```json
[
  { "message": "Employee Created", "employeeId": 1 },
  { "message": "Employee Updated", "employeeId": 1 }
]
```

### 🗃 Storage
- Notifications are stored in a local `notifications.json` file (no database is used).

### 📝 Logging
- All received notifications, file operations, and errors are logged using SLF4J.

---

## 📝 activity-service

### ➕ Responsibilities

* Accept, log, and store all activity events from other services
* Expose a GET endpoint to retrieve all activities

### 📦 REST Endpoints

| Method | Endpoint           | Description                |
| ------ | ------------------ | -------------------------- |
| POST   | `/api/activities`  | Accept and store an activity|
| GET    | `/api/activities`  | Get all stored activities  |
| GET    | `/api/health`      | Healthcheck endpoint       |

### 📄 Request Format (POST)

```json
{
  "timestamp": "2024-07-10T12:34:56Z",
  "service": "employee-management-service",
  "type": "Employee Created",
  "details": {
    "employeeId": 1,
    "firstName": "Sourabh"
  }
}
```

### 📄 Response Format (GET)

```json
[
  {
    "timestamp": "2024-07-10T12:34:56Z",
    "service": "employee-management-service",
    "type": "Employee Created",
    "details": {
      "employeeId": 1,
      "firstName": "Sourabh"
    }
  }
]
```

### 🗃 Storage
- Activities are stored in a local `activities.json` file (no database is used).

### 📝 Logging
- All received activities, file operations, and errors are logged using SLF4J.

---

## 🐳 Docker & Docker Compose Setup

### Docker Compose (Recommended)

To build and run all services together:

```bash
docker-compose up --build
```

- This will build all three services using Java 21 images and start them on ports 8081 (employee), 8082 (notification), and 8083 (activity).
- Inter-service URLs are set automatically via environment variables.
- You can access the APIs at:
  - Employee: http://localhost:8081/api/employees
  - Notification: http://localhost:8082/api/notifications
  - Activity: http://localhost:8083/api/activities

To stop all services:
```bash
docker-compose down
```

### Manual Docker Build & Run (Advanced)

All Dockerfiles use Java 21 (Eclipse Temurin) for both build and runtime images.

You can still build and run each service manually as described below:

```bash
# Package the app
./mvnw clean package -DskipTests

# Build Docker image
# (do this in each service directory)
docker build -t employee-management-service .
docker build -t notification-service .
docker build -t activity-service .

# Run with environment variable
# (run in separate terminals)
docker run -e NOTIFICATION_URL=http://<host-ip>:8082 -e ACTIVITY_URL=http://<host-ip>:8083 \
  -p 8081:8081 employee-management-service

docker run -e ACTIVITY_URL=http://<host-ip>:8083 -p 8082:8082 notification-service

docker run -p 8083:8083 activity-service
```

#### Healthchecks

Each service exposes a `/api/health` endpoint for health monitoring. Docker Compose is configured to use these endpoints for container healthchecks:

- Employee: http://localhost:8081/api/health
- Notification: http://localhost:8082/api/health
- Activity: http://localhost:8083/api/health

You can check the health of each service by visiting the respective endpoint in your browser or using `curl`:

```bash
curl http://localhost:8081/api/health
curl http://localhost:8082/api/health
curl http://localhost:8083/api/health
```

---

## ☁️ Deploying to AWS ECS Fargate

### 1. Create Amazon ECR Repositories

```bash
aws ecr create-repository --repository-name employee-management-service
aws ecr create-repository --repository-name notification-service
aws ecr create-repository --repository-name activity-service
```

### 2. Push Docker Images

```bash
# Authenticate Docker
aws ecr get-login-password --region <region> | \
  docker login --username AWS --password-stdin <account>.dkr.ecr.<region>.amazonaws.com

# Tag and push images
docker tag employee-management-service:latest <ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/employee-management-service
docker push <ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/employee-management-service

docker tag notification-service:latest <ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/notification-service
docker push <ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/notification-service

docker tag activity-service:latest <ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/activity-service
docker push <ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/activity-service
```

---

## 🧾 ECS Setup for Fargate

### Task Definitions (One Per Service)

* `networkMode`: `awsvpc`
* `requiresCompatibilities`: `["FARGATE"]`
* CPU: `256`, Memory: `512`
* Port mappings: `8081` (employee), `8082` (notification), `8083` (activity)
* Enable `assignPublicIp: ENABLED`
* Add `NOTIFICATION_URL` and `ACTIVITY_URL` as environment variables in employee and notification tasks

### Services

* Create 3 ECS services using the above task definitions
* Each service should run in the same VPC/Subnet
* Use a security group that allows traffic on ports 8081, 8082, and 8083
* No ALB needed — each service will receive a public IP

---

## 🔗 Test the Flow

1. Get public IPs of all services from ECS console.
2. Use curl or Postman to hit the create employee endpoint:

```bash
curl -X POST http://<EMPLOYEE_PUBLIC_IP>:8081/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sourabh",
    "lastName": "Khot",
    "email": "sourabh@example.com"
}'
```

3. This should trigger a notification sent to `notification-service` and activity events sent to `activity-service`.

4. To view all notifications:

```bash
curl http://<NOTIFICATION_PUBLIC_IP>:8082/api/notifications
```

5. To view all activities:

```bash
curl http://<ACTIVITY_PUBLIC_IP>:8083/api/activities
```

6. Check logs for all services for detailed action and error logs.

---

## ✅ Summary

| Component            | Description                        |
| -------------------- | ---------------------------------- |
| employee-service     | Spring Boot app on port 8081, stores employees in JSON, sends notifications and activities |
| notification-service | Spring Boot app on port 8082, stores notifications in JSON, sends activities |
| activity-service     | Spring Boot app on port 8083, stores activities in JSON |
| Inter-service comm   | HTTP using public IPs              |
| Dockerized           | Yes (Dockerfile in each service)   |
| Fargate ready        | Yes (stateless, env-configurable)  |
| Load Balancer        | ❌ Not used (public IPs only)       |
| Infrastructure       | AWS ECS Fargate + ECR + VPC/Subnet |
| Logging              | SLF4J logging in all services      |

---

## 🚀 Next Steps (Optional Enhancements)

* Use **Spring Cloud OpenFeign** instead of `RestTemplate`
* Add **Swagger/OpenAPI** documentation
* Integrate with **AWS Cloud Map** for service discovery
* Replace HTTP with **SQS or Kafka** for async messaging
* Add a **CI/CD pipeline** to push images and deploy automatically