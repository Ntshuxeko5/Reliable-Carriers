# MySQL Setup Guide for Reliable Carriers

## Overview
This guide will help you set up the Reliable Carriers application with MySQL database and TurboSMTP email service.

## Prerequisites

### 1. MySQL Server
- Install MySQL Server 8.0 or later
- Ensure MySQL service is running
- Create a database user with appropriate permissions

### 2. TurboSMTP Account
- Sign up at [TurboSMTP](https://turbosmtp.com/)
- Get your SMTP credentials (username and password)
- Verify your domain for sending emails

### 3. Java Environment
- Java 17 or later
- Maven 3.6 or later

## Database Setup

### 1. Install MySQL (if not already installed)

#### Windows:
```bash
# Download MySQL Installer from https://dev.mysql.com/downloads/installer/
# Run the installer and follow the setup wizard
# Set root password during installation
```

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

#### macOS:
```bash
# Using Homebrew
brew install mysql
brew services start mysql
```

### 2. Create Database and User

```bash
# Connect to MySQL as root
mysql -u root -p

# Create database
CREATE DATABASE reliable_carriers CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Create user (replace 'your_password' with a secure password)
CREATE USER 'reliable_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON reliable_carriers.* TO 'reliable_user'@'localhost';
FLUSH PRIVILEGES;

# Exit MySQL
EXIT;
```

### 3. Run Database Setup Script

```bash
# Run the database setup script
mysql -u reliable_user -p reliable_carriers < database-setup.sql
```

## Application Configuration

### 1. Environment Variables Setup

Create a `.env` file in the project root (or set system environment variables):

```bash
# Database Configuration
MYSQL_USERNAME=reliable_user
MYSQL_PASSWORD=your_password
MYSQL_DATABASE=reliable_carriers

# TurboSMTP Configuration
TURBOSMTP_USERNAME=your-turbosmtp-username
TURBOSMTP_PASSWORD=your-turbosmtp-password

# SMS Configuration (SMSPortal)
SMSPORTAL_API_KEY=your-smsportal-api-key
SMSPORTAL_API_SECRET=your-smsportal-api-secret

# Google Maps API (for tracking)
GOOGLE_MAPS_API_KEY=your-google-maps-api-key

# JWT Secret (generate a secure random string)
JWT_SECRET=your-secure-jwt-secret-key
```

### 2. Update Application Properties

The application is already configured to use MySQL. You can customize the settings in `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/reliable_carriers?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=${MYSQL_USERNAME:reliable_user}
spring.datasource.password=${MYSQL_PASSWORD:your_password}

# Email Configuration (TurboSMTP)
spring.mail.host=smtp.turbosmtp.com
spring.mail.port=587
spring.mail.username=${TURBOSMTP_USERNAME:your-turbosmtp-username}
spring.mail.password=${TURBOSMTP_PASSWORD:your-turbosmtp-password}
```

## Building and Running the Application

### 1. Build the Application

```bash
# Clean and build the project
mvn clean install

# Or skip tests for faster build
mvn clean install -DskipTests
```

### 2. Run the Application

```bash
# Run with default profile (MySQL)
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 3. Verify the Application

- Open browser and navigate to `http://localhost:8080`
- Check application logs for any errors
- Verify database connection in logs

## Default Login Credentials

After running the database setup script, you can use these default accounts:

### Admin Account
- **Email**: admin@reliablecarriers.com
- **Password**: admin123
- **Role**: ADMIN

### Driver Account
- **Email**: driver@reliablecarriers.com
- **Password**: admin123
- **Role**: DRIVER

### Customer Account
- **Email**: customer@example.com
- **Password**: admin123
- **Role**: CUSTOMER

## Testing the Setup

### 1. Test Database Connection

```bash
# Connect to MySQL and verify tables
mysql -u reliable_user -p reliable_carriers

# Check tables
SHOW TABLES;

# Check users
SELECT email, role FROM users;

# Exit
EXIT;
```

### 2. Test Email Service

1. Login as admin
2. Navigate to the admin dashboard
3. Try sending a test email
4. Check application logs for email delivery status

### 3. Test Package Creation

1. Login as a customer
2. Create a new package/shipment
3. Verify it's saved in the database
4. Check tracking functionality

## Troubleshooting

### Common Issues

#### 1. Database Connection Error
```
Error: Communications link failure
```
**Solution**: 
- Verify MySQL service is running
- Check database credentials
- Ensure database exists
- Check firewall settings

#### 2. Email Sending Error
```
Error: Authentication failed
```
**Solution**:
- Verify TurboSMTP credentials
- Check if domain is verified in TurboSMTP
- Ensure correct SMTP settings

#### 3. JPA/Hibernate Errors
```
Error: Table doesn't exist
```
**Solution**:
- Run the database setup script
- Check if `spring.jpa.hibernate.ddl-auto=update` is set
- Verify database schema

#### 4. Port Already in Use
```
Error: Port 8080 is already in use
```
**Solution**:
- Change port in `application.properties`
- Or kill the process using port 8080

### Debug Mode

Enable debug logging by adding to `application.properties`:

```properties
logging.level.com.reliablecarriers=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Production Deployment

### 1. Security Considerations

- Change default passwords
- Use strong JWT secret
- Enable SSL/TLS for database
- Configure firewall rules
- Use environment variables for sensitive data

### 2. Database Optimization

```sql
-- Create additional indexes for performance
CREATE INDEX idx_shipments_status_date ON shipments(status, created_at);
CREATE INDEX idx_tracking_shipment_date ON shipment_tracking(shipment_id, created_at);
CREATE INDEX idx_users_email_role ON users(email, role);
```

### 3. Application Properties for Production

```properties
# Production settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.com.reliablecarriers=INFO
logging.level.org.springframework.security=INFO

# Connection pool optimization
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
```

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Documentation**: `http://localhost:8080/api-docs`

## Support

For issues or questions:

1. Check the application logs
2. Verify database connectivity
3. Test email configuration
4. Review this setup guide
5. Check the project documentation files

## Next Steps

After successful setup:

1. **Customize the application**:
   - Update company information
   - Configure pricing rules
   - Set up notification templates

2. **Add more features**:
   - Implement payment processing
   - Add mobile app integration
   - Set up analytics dashboard

3. **Scale the application**:
   - Set up load balancing
   - Implement caching (Redis)
   - Configure monitoring tools

## Files Modified/Created

- `src/main/resources/application.properties` - Updated for MySQL and TurboSMTP
- `src/main/resources/application-mysql.properties` - MySQL-specific configuration
- `database-setup.sql` - Database schema and initial data
- `MYSQL_SETUP_GUIDE.md` - This setup guide

The application is now ready to run with MySQL database and TurboSMTP email service!
