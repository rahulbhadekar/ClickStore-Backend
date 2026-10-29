# ClickStore Backend — E-Commerce REST API

A full-stack e-commerce backend built using **Java, Spring Boot, MySQL, Spring Data JPA, and Spring Security**. ClickStore provides REST APIs for product management, user authentication, shopping cart, checkout, order management, and administrative operations.

## 🚀 Features

- **User Authentication:** Secure login and registration using JWT authentication.
- **Product Management:** Create, retrieve, update, and delete products.
- **Category Management:** Organize products into categories.
- **Shopping Cart:** Add products to the cart and manage cart items.
- **Order Management:** Support order creation and order-related operations.
- **Checkout:** Backend APIs for the checkout workflow.
- **Admin Panel Support:** APIs for administrative operations.
- **Seller Management:** Support seller-related functionality.
- **Database Integration:** Persistent data storage using MySQL and Spring Data JPA.
- **RESTful APIs:** HTTP-based endpoints for frontend-backend communication.
- **Cloud Image Upload:** Cloudinary integration for product images.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Backend application framework |
| Spring Security | Application security |
| JWT | Token-based authentication |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Dependency management and build |
| REST APIs | Frontend-backend communication |
| Cloudinary | Product image storage |
| Postman | API testing |
| Git & GitHub | Version control |

## 📁 Project Structure

```text
ClickStore-Backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/clickstore/clickstore/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       └── dto/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

*Note: The package and folder names above illustrate the intended structure; adjust them to match the actual source files.*

## ⚙️ Getting Started

### Prerequisites

Install the following tools:

- Java JDK compatible with the version configured in `pom.xml`
- MySQL Server
- Git
- Postman (recommended)

### 1. Clone the repository

```bash
git clone https://github.com/rahulbhadekar/ClickStore-Backend.git
cd ClickStore-Backend
```

### 2. Configure MySQL

Create a database in MySQL and configure the database connection in `src/main/resources/application.properties`.

Example configuration:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Configure the corresponding environment variables for your local environment. Keep database passwords and other secrets out of GitHub.

### 3. Configure application secrets

Set the required environment variables for JWT authentication and Cloudinary integration according to the application's configuration classes and properties.

**Important:** Never commit actual database credentials, JWT secrets, API keys, or Cloudinary secrets.

### 4. Run the application

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, if Maven is installed:

```bash
mvn spring-boot:run
```

The application will use the port configured in your Spring Boot settings. With the default configuration, it is typically available at:

```text
http://localhost:8080
```

## 🔌 API Testing

Use Postman to test the backend endpoints.

Example base URL:

```text
http://localhost:8080/api
```

The application includes API functionality for authentication, products, cart, orders, and administrative operations. Exact endpoint paths and request formats depend on the controller implementation.

For protected endpoints, provide a valid JWT access token in the request's `Authorization` header:

```text
Authorization: Bearer YOUR_ACCESS_TOKEN
```

## 🖥️ Frontend Integration

ClickStore Backend is designed to work with a React-based e-commerce frontend.

The frontend communicates with the backend through REST APIs to support product browsing, authentication, shopping cart operations, checkout, and order-related functionality.

## 🔒 Security Notes

- Do not upload `.env` files or production credentials.
- Use environment variables for sensitive configuration.
- Keep JWT signing secrets private.
- Configure CORS origins appropriately for development and production.
- Use HTTPS when deploying the application publicly.

## 👨‍💻 Author

**Rahul Bhadekar**

- GitHub: [@rahulbhadekar](https://github.com/rahulbhadekar)
- Project Repository: [ClickStore Backend](https://github.com/rahulbhadekar/ClickStore-Backend)

## 📌 Project Goal

ClickStore demonstrates the development of a Java-based e-commerce backend using layered architecture, REST APIs, database integration, and authentication.

