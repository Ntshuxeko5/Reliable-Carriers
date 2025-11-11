# Quick Setup: Render Database in 3 Steps

## ✅ Step 1: Get Your Render Database Connection String

1. Go to Render Dashboard → Your PostgreSQL Database
2. Copy the **Internal Database URL** or **Connection String**
   - Format: `postgresql://user:password@host:5432/dbname`

## ✅ Step 2: Set Environment Variables in Render Web Service

Go to your Web Service → Environment tab → Add these:

```bash
# Database (use your actual values from Step 1)
DB_URL=jdbc:postgresql://your-host:5432/your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_DDL_AUTO=update  # For initial setup - creates tables automatically

# Server
SERVER_PORT=8080
APP_BASE_URL=https://your-app-name.onrender.com
PRODUCTION_MODE=false

# Security
JWT_SECRET=<generate with: openssl rand -hex 32>
```

**That's it!** The application will:
- ✅ Auto-detect PostgreSQL from the JDBC URL
- ✅ Create all tables automatically
- ✅ Handle MySQL → PostgreSQL conversion automatically

## ✅ Step 3: Deploy and Verify

1. **Deploy** your application
2. **Check logs** - you should see table creation messages
3. **Test** - Register a user, create a shipment
4. **After first successful deployment**, change:
   ```bash
   DB_DDL_AUTO=validate  # Prevents accidental schema changes
   ```

---

## 🎯 Using Render's Database URL Variable

If your database is in the same Render account, you can use:

```bash
DB_URL=${{your-database-name.DATABASE_URL}}
```

Render will automatically substitute the full connection string!

**Note:** You may need to convert the format:
- Render provides: `postgresql://user:pass@host:5432/db`
- Spring Boot needs: `jdbc:postgresql://host:5432/db`

If using `${{database.DATABASE_URL}}`, you might need to prepend `jdbc:` or set it manually.

---

## 📝 Example Values

### If you have individual connection details:
```bash
DB_URL=jdbc:postgresql://dpg-xxxxx-a.oregon-postgres.render.com:5432/reliable_carriers_abc123
DB_USERNAME=reliable_carriers_user
DB_PASSWORD=your_actual_password_here
```

### If using Render's variable:
```bash
# First, check what format Render provides
# Then convert to jdbc: format if needed
DB_URL=jdbc:postgresql://host:5432/dbname
```

---

## ⚠️ Important Notes

1. **No manual migration needed!** Hibernate creates everything automatically
2. **MySQL → PostgreSQL conversion** is handled automatically
3. **After first deployment**, change `DB_DDL_AUTO=validate` for safety
4. **Your local MySQL data** - if you need to migrate data, see `RENDER_DATABASE_MIGRATION.md`

---

## 🚀 That's It!

Your database is configured. The application will handle the rest automatically!

