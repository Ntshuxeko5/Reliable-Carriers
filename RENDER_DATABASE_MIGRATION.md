# Database Migration Guide: Local MySQL → Render PostgreSQL

This guide will help you migrate from your local MySQL database to Render's PostgreSQL database.

## 📋 Step 1: Get Render Database Connection Details

1. **Go to Render Dashboard**
   - Navigate to your PostgreSQL database service
   - Click on the database name

2. **Copy Connection Details**
   - You'll see connection details like:
     ```
     Internal Database URL: postgresql://user:password@host:5432/dbname
     External Connection String: postgresql://user:password@host:5432/dbname
     ```

3. **Note These Values:**
   - Host
   - Port (usually 5432)
   - Database name
   - Username
   - Password

---

## 🎯 Step 2: Configure Application for PostgreSQL

### Option A: Use Hibernate Auto-Update (Easiest - Recommended)

**This is the easiest approach!** Hibernate will automatically:
- Detect PostgreSQL from the JDBC URL
- Create all tables automatically
- Handle schema differences between MySQL and PostgreSQL

**Steps:**

1. **In Render Environment Variables, set:**
   ```bash
   DB_URL=jdbc:postgresql://your-render-host:5432/reliable_carriers
   DB_USERNAME=your_render_username
   DB_PASSWORD=your_render_password
   DB_DDL_AUTO=update  # For initial setup
   ```

2. **Deploy the application**
   - Hibernate will automatically create all tables
   - No manual migration needed!

3. **After first successful deployment:**
   ```bash
   DB_DDL_AUTO=validate  # Switch to validate mode
   ```

### Option B: Manual Schema Migration

If you want to migrate the schema manually, see the PostgreSQL conversion guide below.

---

## 🔄 Step 3: Migrate Data (Optional)

If you have important data in your local MySQL database that you want to keep:

### Method 1: Export/Import Data (Recommended for Small Databases)

1. **Export data from MySQL:**
   ```bash
   mysqldump -u root -p reliable_carriers --no-create-info --skip-triggers > data_export.sql
   ```

2. **Convert MySQL syntax to PostgreSQL:**
   - PostgreSQL uses different syntax for some things
   - You may need to manually edit the SQL file

3. **Import to PostgreSQL:**
   ```bash
   psql -h your-render-host -U your_username -d reliable_carriers -f data_export.sql
   ```

### Method 2: Use Hibernate Auto-Update + Manual Data Entry

1. Let Hibernate create the schema
2. Manually re-enter important data through the application
3. Good for fresh starts or when you don't have much data

---

## 🔧 Step 4: Update Application Configuration

The application is already configured to auto-detect the database type from the JDBC URL. However, we should ensure PostgreSQL support is optimal.

**Spring Boot will automatically:**
- Detect PostgreSQL from `jdbc:postgresql://` URL
- Use PostgreSQL driver (already in pom.xml)
- Use PostgreSQL dialect

**No code changes needed!** Just set the environment variables.

---

## 📝 Step 5: Environment Variables for Render

Set these in Render's Environment Variables section:

```bash
# Database Connection
DB_URL=jdbc:postgresql://your-render-host:5432/reliable_carriers
DB_USERNAME=your_render_username
DB_PASSWORD=your_render_password
DB_DDL_AUTO=update  # Change to 'validate' after first deployment

# Server
SERVER_PORT=8080
APP_BASE_URL=https://your-app-name.onrender.com
PRODUCTION_MODE=false

# Security
JWT_SECRET=<generate with: openssl rand -hex 32>

# Other variables as needed...
```

---

## ⚠️ Important Notes

### MySQL vs PostgreSQL Differences

1. **ENUM Types:**
   - MySQL: `ENUM('VALUE1', 'VALUE2')`
   - PostgreSQL: `VARCHAR` with CHECK constraint
   - **Hibernate handles this automatically!**

2. **AUTO_INCREMENT:**
   - MySQL: `AUTO_INCREMENT`
   - PostgreSQL: `SERIAL` or `BIGSERIAL`
   - **Hibernate handles this automatically!**

3. **ON UPDATE CURRENT_TIMESTAMP:**
   - MySQL: Supported
   - PostgreSQL: Use triggers (Hibernate handles this)

4. **Boolean:**
   - MySQL: `BOOLEAN` or `TINYINT(1)`
   - PostgreSQL: `BOOLEAN`
   - **Hibernate handles this automatically!**

**Good news:** Hibernate abstracts these differences, so you don't need to worry about them!

---

## 🚀 Quick Start (Recommended)

1. **Get Render database connection string**
2. **Set environment variables in Render:**
   ```bash
   DB_URL=jdbc:postgresql://host:5432/dbname
   DB_USERNAME=username
   DB_PASSWORD=password
   DB_DDL_AUTO=update
   ```
3. **Deploy application**
4. **Hibernate will create all tables automatically**
5. **After successful deployment, change:**
   ```bash
   DB_DDL_AUTO=validate
   ```

That's it! No manual migration needed.

---

## 🔍 Verify Migration

After deployment, check:

1. **Application logs** - Look for table creation messages
2. **Database** - Connect and verify tables exist:
   ```sql
   \dt  -- List all tables in PostgreSQL
   ```
3. **Application** - Test registration, login, etc.

---

## 📞 Troubleshooting

### Issue: Connection refused
- Check firewall settings in Render
- Verify connection string format
- Ensure database is running

### Issue: Tables not created
- Check `DB_DDL_AUTO=update` is set
- Check application logs for errors
- Verify database user has CREATE TABLE permissions

### Issue: Data type errors
- Hibernate should handle this automatically
- If issues occur, check application logs
- May need to adjust entity annotations

---

## 🎯 Summary

**Easiest Approach:**
1. Get Render database connection details
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DDL_AUTO=update`
3. Deploy - Hibernate creates everything automatically
4. Change to `DB_DDL_AUTO=validate` after first deployment

**No manual SQL migration needed!** Hibernate handles MySQL → PostgreSQL conversion automatically.

