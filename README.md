# Reliable Carriers - Package Delivery Management System

A comprehensive package delivery and logistics management platform built with Spring Boot, designed for reliable package tracking, driver management, business integrations, and customer self-service.

## 🚀 Features

### Customer Features
- 📦 Package tracking with real-time updates
- 💳 Online booking and payment (Paystack integration)
- 📱 Live tracking map with driver location
- 🔔 Email & SMS notifications
- ⭐ Post-delivery rating system
- 💬 In-app chat support
- 📊 Personal dashboard with package history
- 🎫 Quote calculator for multiple service types

### Driver Features
- 🚗 Uber-like driver dashboard
- 🗺️ Interactive workboard with map view
- 📍 Route optimization for multiple pickups/deliveries
- 💰 Earnings tracking
- 📱 Offline mode support
- 🔄 Batch operations (pickup/delivery)
- 📸 Package status updates with photos
- 📊 Performance analytics

### Admin Features
- 👥 User management (customers, drivers, businesses)
- 📦 Package management and assignment
- 📊 Comprehensive analytics dashboard
- 🗺️ Real-time driver tracking
- 🔐 Role-based access control
- 📧 Notification management
- 💼 Business verification
- 📈 Revenue reports

### Business Features
- 🔌 RESTful API for integration
- 🔑 API key management
- 📦 Bulk shipment creation
- 📊 Analytics and reporting
- 💳 Credit terms support
- 📱 Webhook notifications
- 🔍 Package tracking API
- 💼 Business dashboard

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.3.0
- **Database**: MySQL 8.0
- **Frontend**: Thymeleaf, Tailwind CSS, JavaScript
- **Authentication**: JWT, Spring Security, OAuth2 (Google/Facebook)
- **Payment**: Paystack Integration
- **Maps**: Google Maps API
- **Notifications**: Email (SMTP), SMS (SMSPortal)
- **Real-time**: WebSockets (SockJS, STOMP)
- **Security**: 2FA, Account Lockout, Rate Limiting

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- Node.js (for frontend assets, optional)

## 🔧 Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Reliable-Carriers
```

### 2. Database Setup
```bash
# Create database
mysql -u root -p
CREATE DATABASE reliable_carriers;
EXIT;
```

### 3. Configuration
```bash
# Copy environment template
cp .env.example .env

# Edit .env with your configuration
nano .env
```

**Required Environment Variables:**
- `DB_URL` - Database connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT secret key (min 32 characters)
- `GMAIL_USERNAME` - Email account
- `GMAIL_APP_PASSWORD` - Email app password
- `GOOGLE_MAPS_API_KEY` - Google Maps API key
- `PAYSTACK_SECRET_KEY` - Paystack secret key
- `PAYSTACK_PUBLIC_KEY` - Paystack public key
- `APP_BASE_URL` - Application base URL

### 4. Build the Application
```bash
mvn clean install
```

### 5. Run the Application
```bash
# Development
mvn spring-boot:run

# Production
java -jar target/Reliable-Carriers-0.0.1-SNAPSHOT.jar
```

## 🌐 Access the Application

- **Web Interface**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html (admin only in production)
- **Actuator**: http://localhost:8080/actuator/health (admin only)

## 👥 Default Accounts

After first run, default accounts are created:
- **Admin**: admin@reliablecarriers.co.za / admin123
- **Customer**: customer@demo.com / customer123
- **Driver**: driver@demo.com / driver123

**⚠️ Change these passwords in production!**

## 📚 Documentation

- [Customer User Manual](./docs/CUSTOMER_MANUAL.md)
- [Driver User Manual](./docs/DRIVER_MANUAL.md)
- [Admin User Manual](./docs/ADMIN_MANUAL.md)
- [Business User Manual](./docs/BUSINESS_MANUAL.md)
- [API Documentation](./docs/API_DOCUMENTATION.md)
- [Deployment Guide](./AFFORDABLE_HOSTING_GUIDE.md)

## 🔒 Security Features

- ✅ JWT-based authentication
- ✅ Two-Factor Authentication (2FA)
- ✅ Account lockout protection
- ✅ Password strength validation
- ✅ Rate limiting
- ✅ CSRF protection
- ✅ SQL injection protection
- ✅ XSS protection
- ✅ Security headers (HSTS, X-Frame-Options, etc.)

## 🚀 Deployment

See [AFFORDABLE_HOSTING_GUIDE.md](./AFFORDABLE_HOSTING_GUIDE.md) for deployment instructions.

**Quick Deploy:**
1. Set `PRODUCTION_MODE=true` in environment
2. Configure all environment variables
3. Build JAR: `mvn clean package -DskipTests`
4. Deploy to server
5. Run: `java -jar Reliable-Carriers-0.0.1-SNAPSHOT.jar`

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ClassNameTest

# Skip tests during build
mvn clean package -DskipTests
```

## 📊 Monitoring

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Application Logs**: Check logs/ directory or systemd journal

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

[Your License Here]

## 📞 Support

For support, email support@reliablecarriers.co.za or visit our website.

## 🙏 Acknowledgments

- Spring Boot Team
- Google Maps API
- Paystack
- All open-source contributors

---

**Built with ❤️ for reliable package delivery**

