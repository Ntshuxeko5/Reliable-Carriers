# Quick Setup: Render Database Connection

## 🎯 Get Your Render Database Connection Details

### Step 1: Find Your Database in Render

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click on your PostgreSQL database service
3. You'll see connection information

### Step 2: Copy Connection Details

Render provides connection details in this format:
```
postgresql://username:password@host:5432/database_name
```

**Or individual values:**
- **Host:** `dpg-xxxxx-a.oregon-postgres.render.com`
- **Port:** `5432`
- **Database:** `reliable_carriers_xxxx`
- **Username:** `reliable_carriers_user`
- **Password:** `xxxxx` (click to reveal)

---

## 🔧 Set Environment Variables in Render

### For Your Web Service:

1. Go to your Web Service in Render
2. Click "Environment" tab
3. Add these variables:

```bash
# Database Connection (use your actual values from Step 2)
DB_URL=jdbc:postgresql://your-host:5432/your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_DDL_AUTO=update  # For initial setup

# Server
SERVER_PORT=8080
APP_BASE_URL=https://your-app-name.onrender.com
PRODUCTION_MODE=false

# Security
JWT_SECRET=<generate a secure secret>
```

### Using Render's Database URL Variable:

If your database is in the same Render account, you can use:
```bash
DB_URL=${{your-database-name.DATABASE_URL}}
```

Render will automatically substitute the full connection string!

---

## 📝 Example Connection String Format

### PostgreSQL (Render):
```
jdbc:postgresql://dpg-xxxxx-a.oregon-postgres.render.com:5432/reliable_carriers_xxxx
```

### MySQL (if using external):
```
jdbc:mysql://your-mysql-host:3306/reliable_carriers?useSSL=true&serverTimezone=UTC
```

---

## ✅ Quick Checklist

- [ ] Created PostgreSQL database in Render
- [ ] Copied connection details
- [ ] Set `DB_URL` environment variable
- [ ] Set `DB_USERNAME` environment variable
- [ ] Set `DB_PASSWORD` environment variable
- [ ] Set `DB_DDL_AUTO=update` for initial setup
- [ ] Deployed application
- [ ] Verified tables were created
- [ ] Changed `DB_DDL_AUTO=validate` after first deployment

---

## 🚀 What Happens Next?

1. **First Deployment:**
   - Application starts
   - Hibernate detects PostgreSQL from `DB_URL`
   - Hibernate creates all tables automatically
   - Your `DatabaseMigrationService` runs any custom migrations
   - Application is ready!

2. **After First Deployment:**
   - Change `DB_DDL_AUTO=validate`
   - Future deployments won't modify schema
   - Safer for production

---

## 🔍 Verify It's Working

1. **Check Application Logs:**
   - Look for "Hibernate: create table" messages
   - No connection errors

2. **Test the Application:**
   - Register a new user
   - Create a shipment
   - Verify data is saved

3. **Check Database:**
   - Connect to Render database
   - Verify tables exist

---

**That's it! Your database is ready for deployment!** 🎉

