# Admin-Only Driver and Tracking Manager Account Management

## Overview

This document describes the implementation of admin-only driver and tracking manager account creation for the Reliable Carriers system. Only administrators can create, manage, and modify driver and tracking manager accounts, ensuring proper control over who can access the driver dashboard, tracking system, and delivery management.

## Security Implementation

### 1. Registration Restrictions

#### Public Registration Endpoint (`/api/auth/register`)
- **Restriction**: Driver and Tracking Manager role creation is blocked
- **Response**: Returns HTTP 403 Forbidden with message "Driver/Tracking Manager accounts can only be created by administrators"
- **Allowed Roles**: CUSTOMER, STAFF only

#### Admin-Only Driver Creation (`/api/auth/admin/create-driver`)
- **Access**: Requires ADMIN role (`@PreAuthorize("hasRole('ADMIN')")`)
- **Functionality**: Creates driver accounts with proper role assignment
- **Security**: JWT token validation required

#### Admin-Only Tracking Manager Creation (`/api/auth/admin/create-tracking-manager`)
- **Access**: Requires ADMIN role (`@PreAuthorize("hasRole('ADMIN')")`)
- **Functionality**: Creates tracking manager accounts with proper role assignment
- **Security**: JWT token validation required

### 2. Frontend Restrictions

#### Registration Form (`/register`)
- **Driver/Tracking Manager Options**: Removed from public registration form
- **User Notice**: Added explanatory text "Driver and Tracking Manager accounts can only be created by administrators"
- **Available Options**: Customer and Staff only

#### Admin Dashboard (`/admin/dashboard`)
- **Access**: Restricted to ADMIN role only
- **Features**: Complete user management interface
- **Driver Creation**: Dedicated modal for creating driver accounts
- **Tracking Manager Creation**: Dedicated modal for creating tracking manager accounts

## API Endpoints

### Public Registration (Restricted)
```
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+1234567890",
  "role": "DRIVER"  // This will be rejected
}

Response: 403 Forbidden
"Driver accounts can only be created by administrators. Please contact support."

{
  "firstName": "Jane",
  "lastName": "Manager",
  "email": "jane@example.com",
  "password": "password123",
  "phone": "+1234567890",
  "role": "TRACKING_MANAGER"  // This will be rejected
}

Response: 403 Forbidden
"Tracking Manager accounts can only be created by administrators. Please contact support."
```

### Admin Driver Creation
```
POST /api/auth/admin/create-driver
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json

{
  "firstName": "Mike",
  "lastName": "Driver",
  "email": "mike@reliablecarriers.com",
  "password": "driver123",
  "phone": "+1234567890",
  "address": "123 Driver St",
  "city": "Johannesburg",
  "state": "Gauteng",
  "zipCode": "2000",
  "country": "South Africa",
  "role": "DRIVER"
}

Response: 201 Created
{
  "id": 123,
  "firstName": "Mike",
  "lastName": "Driver",
  "email": "mike@reliablecarriers.com",
  "role": "DRIVER"
}
```

### Admin User Management
```
GET /api/admin/users                    // Get all users
GET /api/admin/drivers                  // Get all drivers
GET /api/admin/tracking-managers        // Get all tracking managers
GET /api/admin/users/role/{role}        // Get users by role
GET /api/admin/users/{id}               // Get specific user
PUT /api/admin/users/{id}               // Update user
PUT /api/admin/users/{id}/role          // Update user role
DELETE /api/admin/users/{id}            // Delete user
GET /api/admin/users/statistics         // Get user statistics
POST /api/admin/drivers                 // Create driver account
POST /api/admin/tracking-managers       // Create tracking manager account
```

## Database Schema

### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password; // Encoded with BCrypt
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    // Other fields...
}
```

### UserRole Enum
```java
public enum UserRole {
    ADMIN,
    CUSTOMER,
    DRIVER,
    STAFF,
    TRACKING_MANAGER
}
```

## Security Configuration

### Spring Security Setup
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

## Admin Dashboard Features

### 1. User Statistics
- Total users count
- Drivers count
- Customers count
- Staff count
- Tracking Managers count
- Real-time updates

### 2. User Management
- View all users in a table
- Filter by role
- Edit user details
- Change user roles
- Delete users (except admins)

### 3. Driver Account Creation
- Dedicated modal form
- All required fields validation
- Automatic role assignment
- Password encoding
- Success/error notifications

### 4. Tracking Manager Account Creation
- Dedicated modal form
- All required fields validation
- Automatic role assignment
- Password encoding
- Success/error notifications

### 5. Security Features
- JWT token validation
- Role-based access control
- CSRF protection
- Input validation
- SQL injection prevention

## Workflow

### 1. Driver Account Creation Process
```
Admin Login → Admin Dashboard → Create Driver Account → Fill Form → Submit → Account Created
```

### 2. Tracking Manager Account Creation Process
```
Admin Login → Admin Dashboard → Create Tracking Manager Account → Fill Form → Submit → Account Created
```

### 3. Driver Access Process
```
Driver Login → Authentication → Role Verification → Driver Dashboard Access
```

### 4. Tracking Manager Access Process
```
Tracking Manager Login → Authentication → Role Verification → Tracking Dashboard Access
```

### 5. Security Verification
```
Request → JWT Token Validation → Role Check → Method Security → Response
```

## Error Handling

### Registration Attempts
- **Driver Role in Public Registration**: 403 Forbidden
- **Tracking Manager Role in Public Registration**: 403 Forbidden
- **Invalid Email**: 400 Bad Request
- **Duplicate Email**: 400 Bad Request
- **Missing Required Fields**: 400 Bad Request

### Admin Operations
- **Unauthorized Access**: 401 Unauthorized
- **Insufficient Permissions**: 403 Forbidden
- **User Not Found**: 404 Not Found
- **Validation Errors**: 400 Bad Request

## Testing Scenarios

### 1. Public Registration Tests
- ✅ Customer registration allowed
- ✅ Staff registration allowed
- ❌ Driver registration blocked
- ❌ Tracking Manager registration blocked
- ❌ Admin registration blocked

### 2. Admin Operations Tests
- ✅ Admin can create driver accounts
- ✅ Admin can create tracking manager accounts
- ✅ Admin can view all users
- ✅ Admin can edit user roles
- ✅ Admin can delete non-admin users
- ❌ Non-admin cannot access admin endpoints

### 3. Driver Access Tests
- ✅ Driver can access driver dashboard
- ✅ Driver cannot access admin dashboard
- ✅ Driver can view assigned packages
- ✅ Driver can update package status

### 4. Tracking Manager Access Tests
- ✅ Tracking Manager can access tracking dashboard
- ✅ Tracking Manager cannot access admin dashboard
- ✅ Tracking Manager can view all driver locations
- ✅ Tracking Manager can view package tracking information

## Deployment Considerations

### 1. Environment Variables
```properties
# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/reliable_carriers
spring.datasource.username=your-username
spring.datasource.password=your-password
```

### 2. Security Headers
```java
@Bean
public SecurityHeadersFilter securityHeadersFilter() {
    return new SecurityHeadersFilter();
}
```

### 3. CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## Monitoring and Logging

### 1. Security Events
- Failed login attempts
- Unauthorized access attempts
- Role-based access violations
- Driver account creation events

### 2. Audit Trail
- User creation timestamps
- Role changes
- Account modifications
- Deletion events

### 3. Performance Metrics
- API response times
- Database query performance
- Authentication latency
- User session duration

## Future Enhancements

### 1. Advanced Security
- Two-factor authentication for admins
- IP-based access restrictions
- Session timeout management
- Account lockout policies

### 2. User Management
- Bulk user operations
- User import/export
- Advanced filtering and search
- User activity monitoring

### 3. Driver Management
- Driver verification process
- Document upload (license, ID)
- Background check integration
- Performance tracking

## Conclusion

The admin-only driver and tracking manager account creation system provides a secure and controlled way to manage access to the Reliable Carriers platform. By restricting driver and tracking manager account creation to administrators only, the system ensures proper oversight and maintains security standards while providing a smooth user experience for all role types.

The implementation includes comprehensive security measures, proper error handling, and a user-friendly admin interface for managing all aspects of user accounts and roles.
