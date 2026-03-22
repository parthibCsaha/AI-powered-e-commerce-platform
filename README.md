# 🛒 AI-Powered E-Commerce Platform

A modern, full-featured e-commerce backend built with **Spring Boot 4** and **Java 21**, featuring **AI-powered product search** via Groq LLM integration. Users can search for products using natural language queries, and the AI engine intelligently extracts filters like brand, price range, rating, and keywords to deliver precise results.

---

## ✨ Features

### 🤖 AI-Powered Search
- Natural language product search powered by **Groq AI**
- Automatically extracts structured filters (brand, price range, rating, keywords) from plain English queries
- Intelligent typo correction and query understanding
- Example: *"cheap Nike running shoes under $100"* → filters by brand, keywords, and max price

### 🔐 Authentication & Security
- JWT-based stateless authentication with **BCrypt** password hashing
- Role-based access control (`CUSTOMER` / `ADMIN`)
- Method-level security with `@PreAuthorize` annotations
- IP-based **rate limiting** with tiered buckets (auth, AI, general) using **Bucket4j**
- `X-Forwarded-For` header support for proxy-aware IP resolution

### 🛍️ E-Commerce Core
- **Products** — Full CRUD with admin-only write access, paginated listing, and multi-filter search (name, brand, price range)
- **Cart** — Add products with quantities, paginated cart view per user
- **Orders** — Direct buy or cart checkout, paginated order history with flexible sorting
- **Reviews** — Authenticated users can review products

---

## 🏗️ Tech Stack

| Layer          | Technology                                  |
|----------------|---------------------------------------------|
| Framework      | Spring Boot 4.0.2                           |
| Language       | Java 21                                     |
| Database       | PostgreSQL                                  |
| Caching        | Redis (Spring Data Redis)                   |
| Security       | Spring Security + JWT (jjwt 0.12.5)         |
| Rate Limiting  | Bucket4j                                    |
| AI Integration | Groq API (LLM chat completions)             |
| Build Tool     | Gradle                                      |
| ORM            | Spring Data JPA / Hibernate                 |
| Utilities      | Lombok, Jackson                             |

---

## 📁 Project Structure

```
backend/src/main/java/com/backend/
├── BackendApplication.java          # Application entry point
├── config/
│   ├── AppConfig.java               # General app config (RestTemplate bean, etc.)
│   ├── RateLimitConfig.java         # Tiered rate limit configuration
│   └── RedisConfig.java             # Redis connection & template config
├── controller/
│   ├── AiController.java            # POST /api/ai/search
│   ├── AuthController.java          # POST /api/auth/register, /login
│   ├── CartController.java          # GET/POST /api/cart
│   ├── OrderController.java         # GET/POST /api/order
│   └── ProductController.java       # CRUD /api/product + search + reviews
├── dto/                             # Request/Response records & classes
├── entity/
│   ├── User.java                    # Users with roles, cart, orders, reviews
│   ├── Product.java                 # Product catalog
│   ├── Cart.java / CartItem.java    # Shopping cart
│   ├── Order.java / OrderItem.java  # Order management
│   ├── OrderStatus.java             # Enum: order lifecycle states
│   └── Review.java                  # Product reviews
├── exception/                       # Custom exception handling
├── repository/                      # Spring Data JPA repositories
├── security/
│   ├── SecurityConfig.java          # Filter chain, auth provider, CORS
│   ├── JwtFilter.java               # JWT token validation filter
│   ├── JwtUtil.java                 # Token generation & parsing
│   ├── RateLimitFilter.java         # IP-based tiered rate limiting
│   └── CustomUserDetailsService.java
├── service/
│   ├── AiService.java               # Groq API integration & filter extraction
│   ├── AuthService.java             # Registration & login logic
│   ├── CartService.java             # Cart operations
│   ├── OrderService.java            # Buy & checkout logic
│   └── ProductService.java          # Product CRUD, search, reviews
└── specification/                   # JPA Specifications for dynamic queries
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+**
- **PostgreSQL** (running instance)
- **Redis** (running instance)
- **Groq API key** — get one at [console.groq.com](https://console.groq.com)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/ai-powered-e-commerce.git
   cd ai-powered-e-commerce/backend
   ```

2. **Configure the application**

   Copy the example config and fill in your values:
   ```bash
   cp src/main/resources/application-example.yaml src/main/resources/application.yaml
   ```

   Edit `application.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/your_db_name
       username: postgres
       password: your_password

   app:
     ai:
       groq:
         api-key: your_groq_api_key
         model: llama-3.3-70b-versatile  
     jwt:
       secret: your_jwt_secret
       expiration: 86400000              
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```
   The server starts at `http://localhost:8080`.

---

## 📡 API Reference

### Authentication
| Method | Endpoint              | Description         | Auth |
|--------|-----------------------|---------------------|------|
| POST   | `/api/auth/register`  | Register a new user | ❌   |
| POST   | `/api/auth/login`     | Login & get JWT     | ❌   |

### Products
| Method | Endpoint                | Description              | Auth   |
|--------|-------------------------|--------------------------|--------|
| GET    | `/api/product`          | List products (paginated)| ✅     |
| GET    | `/api/product/{id}`     | Get product by ID        | ✅     |
| GET    | `/api/product/search`   | Filter search            | ✅     |
| POST   | `/api/product`          | Add product              | ✅     |
| PUT    | `/api/product/{id}`     | Update product           | 🔒 Admin |
| DELETE | `/api/product/{id}`     | Delete product           | 🔒 Admin |
| POST   | `/api/product/review`   | Add a review             | ✅     |

### AI Search
| Method | Endpoint          | Description                        | Auth |
|--------|-------------------|------------------------------------|------|
| POST   | `/api/ai/search`  | Natural language product search    | ✅   |

**Request body:**
```json
{ "query": "red winter jacket between $50 and $150" }
```

### Cart
| Method | Endpoint        | Description           | Auth |
|--------|-----------------|-----------------------|------|
| GET    | `/api/cart`     | View cart (paginated) | ✅   |
| POST   | `/api/cart/add` | Add item to cart      | ✅   |

### Orders
| Method | Endpoint             | Description                  | Auth |
|--------|----------------------|------------------------------|------|
| GET    | `/api/order`         | Order history (paginated)    | ✅   |
| POST   | `/api/order/buy`     | Buy product directly         | ✅   |
| POST   | `/api/order/checkout`| Checkout entire cart         | ✅   |

---

## 🛡️ Rate Limiting

Requests are rate-limited per IP with **tiered buckets**:

| Tier      | Applies To          | Purpose                            |
|-----------|---------------------|------------------------------------|
| `auth`    | `/api/auth/**`      | Prevents brute-force login attacks |
| `ai`      | `/api/ai/**`        | Protects AI/LLM API usage         |
| `general` | All other endpoints | General abuse prevention           |

When rate-limited, the API responds with:
- **HTTP 429** Too Many Requests
- `Retry-After` header (seconds until next allowed request)
- `X-Rate-Limit-Remaining` header on every response

---

## 🧑‍💻 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).