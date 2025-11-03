# Quick Production Fixes (30 Minutes)

## 🔴 MUST DO BEFORE DEPLOYMENT

### 1. Create Environment Variables File (5 min)

Create `.env` file (DO NOT commit to git - add to .gitignore):

```bash
# Copy these values from application.properties and update with production values
export DB_PASSWORD="your_secure_password"
export JWT_SECRET="generate-a-long-random-secret-min-256-bits"
export GMAIL_APP_PASSWORD="your-app-password"
export SMSPORTAL_API_KEY="your-key"
export SMSPORTAL_API_SECRET="your-secret"
export GOOGLE_MAPS_API_KEY="your-key"
export PAYSTACK_SECRET_KEY="sk_live_your_production_key"
export PAYSTACK_PUBLIC_KEY="pk_live_your_production_key"
export GOOGLE_CLIENT_SECRET="your-secret"
export FACEBOOK_CLIENT_SECRET="your-secret"
export APP_BASE_URL="https://yourdomain.com"
```

### 2. Update application.properties (5 min)

Change these lines:

```properties
# Replace hardcoded values with environment variables
spring.datasource.password=${DB_PASSWORD:}
jwt.secret=${JWT_SECRET:}
spring.mail.password=${GMAIL_APP_PASSWORD:}
sms.api.key=${SMSPORTAL_API_KEY:}
sms.api.secret=${SMSPORTAL_API_SECRET:}
google.maps.api.key=${GOOGLE_MAPS_API_KEY:}
paystack.secret.key=${PAYSTACK_SECRET_KEY:}
paystack.public.key=${PAYSTACK_PUBLIC_KEY:}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET:}
app.base.url=${APP_BASE_URL:https://yourdomain.com}
app.tracking.url=${APP_BASE_URL:https://yourdomain.com}
```

### 3. Disable Debug Logging (2 min)

```properties
# Change from DEBUG to INFO
logging.level.com.reliablecarriers=INFO
logging.level.org.springframework.security=WARN
logging.level.org.springframework.web=INFO
spring.jpa.show-sql=false
spring.mail.properties.mail.debug=false
```

### 4. Restrict Swagger in Production (2 min)

In `SecurityConfig.java`, change line 102:

```java
// FROM:
.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()

// TO:
.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").hasRole("ADMIN")
```

### 5. Update Error Display (2 min)

```properties
# Hide stack traces from users
server.error.include-message=never
server.error.include-stacktrace=never
```

### 6. Set Production Profile (1 min)

```properties
spring.profiles.active=prod
```

## ✅ DONE! Ready for Testing

After these 6 steps (30 minutes), the application is ready for:
- ✅ Client testing
- ✅ Customer beta testing
- ✅ Staging deployment

## 🔒 Additional Security (Optional but Recommended)

1. Enable HTTPS
2. Set up firewall rules
3. Configure database backups
4. Set up monitoring
5. Enable CSRF for forms (already done in code)

## 📝 Deployment Command

```bash
# Load environment variables
source .env

# Build with production profile
mvn clean package -DskipTests

# Run with environment variables
java -jar target/Reliable-Carriers-0.0.1-SNAPSHOT.jar
```

---

**Total Time: ~30 minutes**
**Status: Ready for Testing ✅**

