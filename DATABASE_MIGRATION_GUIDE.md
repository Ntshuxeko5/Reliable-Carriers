# Database Migration Guide for Render & Railway

This guide explains how to handle database migrations when deploying to Render and Railway.

## 📊 Current Setup

Your application currently uses:
- **Hibernate `ddl-auto=update`** for automatic schema management
- **Custom `DatabaseMigrationService`** for runtime schema updates
- **`database-setup.sql`** for initial database creation

## 🎯 Migration Strategy for Deployment

### Option 1: Hibernate Auto-Update (Current - Recommended for Initial Setup)

**Best for:** Initial deployment and development

**How it works:**
- Hibernate automatically creates/updates tables based on your JPA entities
- No manual migration scripts needed
- Simple and fast for getting started

**Configuration:**
```properties
# For initial setup (first deployment)
DB_DDL_AUTO=update

# After initial setup (production)
DB_DDL_AUTO=validate
```

**Steps for Deployment:**

1. **First Deployment (Initial Setup):**
   ```bash
   # Set in Railway/Render environment variables
   DB_DDL_AUTO=update
   ```
   - Hibernate will create all tables automatically
   - Your `DatabaseMigrationService` will run any custom migrations
   - Application will start with full schema

2. **After Initial Setup (Production):**
   ```bash
   # Change to validate mode
   DB_DDL_AUTO=validate
   ```
   - Hibernate will only validate schema matches entities
   - Prevents accidental schema changes
   - Safer for production

---

### Option 2: Manual SQL Script (For Fresh Database)

**Best for:** When you want full control over initial schema

**Steps:**

1. **Create Database:**
   ```sql
   CREATE DATABASE reliable_carriers;
   ```

2. **Run Setup Script:**
   - Use the provided `database-setup.sql`
   - Or run it via database client/CLI

3. **Set Environment Variable:**
   ```bash
   DB_DDL_AUTO=validate
   ```

4. **Deploy Application:**
   - Application will connect to existing database
   - No schema changes will be made automatically

---

### Option 3: Flyway Migration (Recommended for Production)

**Best for:** Production environments with version-controlled migrations

**Benefits:**
- Version-controlled migrations
- Track migration history
- Rollback support
- Team collaboration

**Setup Steps:**

#### Step 1: Add Flyway Dependency

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

#### Step 2: Create Migration Directory

Create: `src/main/resources/db/migration/`

#### Step 3: Create Initial Migration

Create: `src/main/resources/db/migration/V1__Initial_schema.sql`

Copy contents from `database-setup.sql` or generate from your entities.

#### Step 4: Configure Flyway

Add to `application.properties`:
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true

# Disable Hibernate auto DDL when using Flyway
spring.jpa.hibernate.ddl-auto=validate
```

#### Step 5: Deploy

Flyway will automatically run migrations on application startup.

---

## 🚀 Deployment Steps by Platform

### Railway Deployment

#### Initial Setup (First Time):

1. **Create Database:**
   - Railway Dashboard → New → Database → Add PostgreSQL (or use external MySQL)
   - Note connection details

2. **Set Environment Variables:**
   ```bash
   DB_URL=${{Postgres.DATABASE_URL}}  # Or your MySQL URL
   DB_USERNAME=${{Postgres.PGUSER}}   # Or your MySQL username
   DB_PASSWORD=${{Postgres.PGPASSWORD}} # Or your MySQL password
   DB_DDL_AUTO=update  # For initial setup
   ```

3. **Deploy Application:**
   - Railway will build and deploy
   - On first startup, Hibernate will create all tables
   - `DatabaseMigrationService` will run custom migrations

4. **After First Deployment:**
   ```bash
   # Change to validate mode
   DB_DDL_AUTO=validate
   ```
   - Redeploy or update environment variable
   - Future deployments won't modify schema

#### Using Manual SQL Script:

1. **Connect to Database:**
   ```bash
   # Get connection string from Railway
   # Use any MySQL/PostgreSQL client
   ```

2. **Run Setup Script:**
   ```bash
   mysql -h host -u user -p reliable_carriers < database-setup.sql
   # OR for PostgreSQL:
   psql -h host -U user -d reliable_carriers -f database-setup.sql
   ```

3. **Set Environment:**
   ```bash
   DB_DDL_AUTO=validate
   ```

4. **Deploy Application**

---

### Render Deployment

#### Initial Setup (First Time):

1. **Create Database:**
   - Render Dashboard → New + → PostgreSQL
   - Name: `reliable-carriers-db`
   - Note connection details

2. **Set Environment Variables:**
   ```bash
   DB_URL=${{reliable-carriers-db.DATABASE_URL}}
   # OR for external MySQL:
   DB_URL=jdbc:mysql://host:3306/reliable_carriers
   DB_USERNAME=your_user
   DB_PASSWORD=your_password
   DB_DDL_AUTO=update  # For initial setup
   ```

3. **Deploy Application:**
   - Render will build and deploy
   - On first startup, Hibernate will create all tables

4. **After First Deployment:**
   ```bash
   DB_DDL_AUTO=validate
   ```

#### Using Manual SQL Script:

1. **Connect to Database:**
   - Use Render's database connection string
   - Connect via any database client

2. **Run Setup Script:**
   ```bash
   # Using psql (PostgreSQL)
   psql $DATABASE_URL -f database-setup.sql
   
   # OR using MySQL client
   mysql -h host -u user -p reliable_carriers < database-setup.sql
   ```

3. **Set Environment:**
   ```bash
   DB_DDL_AUTO=validate
   ```

4. **Deploy Application**

---

## 📝 Migration Workflow

### For Schema Changes (Future Updates)

#### Using Hibernate (Simple):

1. **Update Entity Classes:**
   ```java
   @Entity
   public class User {
       // Add new field
       private String newField;
   }
   ```

2. **Temporarily Enable Update:**
   ```bash
   DB_DDL_AUTO=update
   ```

3. **Deploy:**
   - Hibernate will add the new column

4. **Switch Back to Validate:**
   ```bash
   DB_DDL_AUTO=validate
   ```

#### Using Flyway (Recommended):

1. **Create Migration File:**
   ```
   src/main/resources/db/migration/V2__Add_new_field_to_user.sql
   ```

2. **Write SQL:**
   ```sql
   ALTER TABLE users ADD COLUMN new_field VARCHAR(255);
   ```

3. **Deploy:**
   - Flyway will run migration automatically
   - Keep `DB_DDL_AUTO=validate`

---

## 🔍 Verification Steps

### After Initial Deployment:

1. **Check Tables Created:**
   ```sql
   -- MySQL
   SHOW TABLES;
   
   -- PostgreSQL
   \dt
   ```

2. **Check Application Logs:**
   - Look for "Hibernate: create table" messages (if using update)
   - Look for "Flyway: Migrating schema" messages (if using Flyway)
   - Check for any migration errors

3. **Test Application:**
   - Register a new user
   - Create a shipment
   - Verify data is saved correctly

---

## ⚠️ Important Notes

### DO NOT Use `update` in Production Long-Term

**Why:**
- Can cause data loss
- No rollback capability
- Hard to track changes
- Can break with complex changes

**When to Use:**
- ✅ Initial setup (first deployment)
- ✅ Development environment
- ✅ Temporary schema updates

**When NOT to Use:**
- ❌ Production (after initial setup)
- ❌ When you need migration history
- ❌ Team environments

### Database Compatibility

**MySQL:**
- Your app is configured for MySQL
- Works with `database-setup.sql`
- Hibernate dialect: `MySQLDialect`

**PostgreSQL (Railway/Render Default):**
- Spring Boot auto-detects driver from JDBC URL
- Update dialect if needed:
  ```properties
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
  ```
- May need to adjust SQL syntax in `database-setup.sql`

---

## 🛠️ Troubleshooting

### Issue: Tables Not Created

**Solution:**
1. Check `DB_DDL_AUTO=update` is set
2. Check database connection is working
3. Check application logs for errors
4. Verify database user has CREATE TABLE permissions

### Issue: Migration Fails

**Solution:**
1. Check database connection
2. Verify SQL syntax is correct
3. Check for existing tables/columns
4. Review application logs

### Issue: Schema Mismatch

**Solution:**
1. Use `DB_DDL_AUTO=update` temporarily
2. Let Hibernate fix the schema
3. Switch back to `validate`
4. Or manually fix via SQL

---

## 📋 Quick Reference

### Environment Variables:

```bash
# Initial Setup
DB_DDL_AUTO=update

# Production (After Initial Setup)
DB_DDL_AUTO=validate

# Database Connection
DB_URL=jdbc:mysql://host:3306/reliable_carriers
DB_USERNAME=your_user
DB_PASSWORD=your_password
```

### Commands:

```bash
# Check database connection
mysql -h host -u user -p -e "SHOW DATABASES;"

# Run setup script
mysql -h host -u user -p reliable_carriers < database-setup.sql

# Check tables
mysql -h host -u user -p reliable_carriers -e "SHOW TABLES;"
```

---

## 🎯 Recommended Approach

### For Your First Deployment:

1. ✅ Use **Hibernate `ddl-auto=update`** for initial setup
2. ✅ Let Hibernate create all tables automatically
3. ✅ After successful deployment, switch to `validate`
4. ✅ For future changes, consider adding Flyway

### For Production (Long-term):

1. ✅ Use **Flyway** for version-controlled migrations
2. ✅ Keep `ddl-auto=validate` always
3. ✅ Create migration scripts for all schema changes
4. ✅ Test migrations in staging first

---

## 📚 Additional Resources

- [Hibernate DDL Auto](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Database Initialization](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization)

---

**Ready to deploy? Follow the steps above based on your chosen approach!**

