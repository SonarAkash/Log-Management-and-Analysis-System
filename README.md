# LogFlux - Log Management & Analysis System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A high-performance, log management and analysis system built with Spring Boot and PostgreSQL. LogFlux allows you to collect, store, search, and analyze log data in real-time with advanced querying capabilities using LSQL (Log Structured Query Language).

## 📸 Workflow

![Dashboard](./images/img1.png)
![Live Tail](./images/img2.png)
![Search Interface](./images/img3.png)
![Analytics](./images/img4.png)
![Configuration](./images/img5.png)
![Documentation](./images/img6.png)

## 🚀 Key Features

- **Real-time Log Ingestion**: High-throughput log ingestion with Write-Ahead Logging (WAL)
- **Advanced Search**: Custom LSQL query language for powerful log searching
- **Live Tail**: Real-time log streaming with WebSocket support
- **Geolocation Analysis**: IP-based geolocation tracking using MaxMind GeoIP2
- **User Agent Analysis**: Detailed user agent parsing and analysis
- **Multi-tenant Architecture**: Secure isolation between different organizations
- **Rate Limiting**: Built-in rate limiting for API endpoints
- **Authentication & Authorization**: JWT-based secure authentication system
- **Interactive Dashboard**: Real-time visualization and analytics
- **REST API**: Comprehensive API for integration

## 🛠️ Technologies

- **Backend**:
  - Java 21
  - Spring Boot 3.5.4
  - Spring Security
  - Spring WebSocket
  - Spring Data JPA
  - PostgreSQL 15
  - JWT Authentication
  - MaxMind GeoIP2 (CLICK ON THE [LINK](https://www.maxmind.com/en/home) TO DOWNLOAD : GeoLite2-City_20250815.tar.gz, and place it in resources folder)
  - YAUAA (User Agent Analysis)

- **Frontend**:
  - HTML5
  - CSS3
  - JavaScript
  - WebSocket

- **DevOps**:
  - Docker
  - Docker Compose
  - k6 (Load Testing)

## 📋 Prerequisites

- Java Development Kit (JDK) 21
- Docker and Docker Compose
- Maven
- PostgreSQL 15 (if running locally)
- Git

## 🚀 Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/SonarAkash/Log-Management-and-Analysis-System.git
   cd Log-Management-and-Analysis-System
   ```

2. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run with Docker Compose** (Recommended)
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL database
   - LogFlux application
   - k6 load testing service (optional)

4. **Run Locally** (Alternative)
   - Configure your PostgreSQL database
   - Update `application-dev.properties` with your database credentials
   - Run the application:
     ```bash
     mvn spring-boot:run -Dspring.profiles.active=dev
     ```

## ⚙️ Configuration

### Environment Variables

```properties
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/log_management
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
LOG_INGESTION_WAL_PATH=/path/to/wal
LOG_MANAGER_SECURE_KEY=your_secure_key
```

### Properties Files

- `application.properties`: Base configuration
- `application-dev.properties`: Development profile
- `application-prod.properties`: Production profile

## 📖 API Documentation

Access the OpenAPI documentation at:
- Local: http://localhost:8080/swagger-ui.html
- Production: https://your-domain/swagger-ui.html

## 🔍 LSQL Query Examples

1. **Basic Search**
   ```sql
   level:error AND service:payment
   ```

2. **Complex Query**
   ```sql
   service:auth AND (login OR logout) NOT clientIp:"192.168.1.5"
   ```

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LogManagerApplicationTests

# Run with coverage
mvn verify
```

## 🔒 Security

- All API endpoints are secured with JWT authentication
- Rate limiting is enabled for ingestion endpoints
- Multi-tenant data isolation
- Secure WebSocket connections

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MaxMind GeoIP2](https://www.maxmind.com/en/geoip2-services-and-databases)
- [YAUAA](https://github.com/nielsbasjes/yauaa)
- [Docker](https://www.docker.com/)
- [k6](https://k6.io/)