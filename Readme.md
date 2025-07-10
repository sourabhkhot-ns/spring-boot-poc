# 🧪 Employee Notification Microservices POC

This project is a **proof-of-concept (POC)** microservices setup using **Spring Boot**, featuring two independent services:

- `employee-management-service`: Handles employee operations, stores employees in a JSON file, and notifies on changes.
- `notification-service`: Receives, logs, and stores notifications in a JSON file via HTTP.

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
└── README.md
```

---

## 🧰 Technologies Used

- Java 17
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

### 📦 REST Endpoints

| Method | Endpoint               | Description             |
|--------|------------------------|-------------------------|
| POST   | `/api/employees`       | Create a new employee   |
| GET    | `/api/employees`       | Get all employees       |
| GET    | `/api/employees/{id}`  | Get employee by ID      |
| PUT    | `/api/employees/{id}`  | Update employee by ID   |
| DELETE | `/api/employees/{id}`  | Delete employee by ID   |

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

### 📦 REST Endpoints

| Method | Endpoint             | Description                    |
| ------ | -------------------- | ------------------------------ |
| POST   | `/api/notifications` | Accept and store a notification|
| GET    | `/api/notifications` | Get all stored notifications   |

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

## 🐳 Docker Setup

### Dockerfile (common to both services)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build & Run Locally

```bash
# Package the app
./mvnw clean package -DskipTests

# Build Docker image
# (do this in each service directory)
docker build -t employee-management-service .
docker build -t notification-service .

# Run with environment variable
# (run in separate terminals)
docker run -e NOTIFICATION_URL=http://<host-ip>:8082 \
  -p 8081:8081 employee-management-service

docker run -p 8082:8080 notification-service
```

---

## ☁️ Deploying to AWS ECS Fargate

### 1. Create Amazon ECR Repositories

```bash
aws ecr create-repository --repository-name employee-management-service
aws ecr create-repository --repository-name notification-service
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
```

---

## 🧾 ECS Setup for Fargate

### Task Definitions (One Per Service)

* `networkMode`: `awsvpc`
* `requiresCompatibilities`: `["FARGATE"]`
* CPU: `256`, Memory: `512`
* Port mappings: `8081` (employee), `8082` (notification)
* Enable `assignPublicIp: ENABLED`
* Add `NOTIFICATION_URL` as environment variable in employee task

### Services

* Create 2 ECS services using the above task definitions
* Each service should run in the same VPC/Subnet
* Use a security group that allows traffic on ports 8081 and 8082
* No ALB needed — each service will receive a public IP

---

## 🔗 Test the Flow

1. Get public IP of `employee-management-service` from ECS console.
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

3. This should trigger a notification sent to `notification-service`.

4. To view all notifications:

```bash
curl http://<NOTIFICATION_PUBLIC_IP>:8082/api/notifications
```

5. Check logs for both services for detailed action and error logs.

---

## ✅ Summary

| Component            | Description                        |
| -------------------- | ---------------------------------- |
| employee-service     | Spring Boot app on port 8081, stores employees in JSON |
| notification-service | Spring Boot app on port 8082, stores notifications in JSON |
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