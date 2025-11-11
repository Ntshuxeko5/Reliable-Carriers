# Fix: Database Connection Error on Render

## ❌ The Problem

The error shows:
```
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, postgresql://...
```

**Issues:**
1. URL format is wrong - missing `jdbc:` prefix
2. Credentials are embedded in URL (should be separate)
3. Driver is trying to use MySQL instead of PostgreSQL

## ✅ The Solution

### Step 1: Set Environment Variables in Render (NOT in application.properties)

**DO NOT put database credentials in `application.properties`!** Use environment variables in Render instead.

### Step 2: Correct Environment Variables Format

In Render Dashboard → Your Web Service → Environment tab, set:

```bash
# Database Connection (CORRECT FORMAT)
DB_URL=jdbc:postgresql://dpg-d49hi4p5pdvs73ctrrvg-a:5432/reliable_carriers
DB_USERNAME=reliable_carriers_user
DB_PASSWORD=zMyj2mAYxzL1iFgy1pXsLAAHjxZanxqp
DB_DDL_AUTO=update

# Explicitly set PostgreSQL driver (to ensure it's used)
DB_DRIVER=org.postgresql.Driver
DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# Server
SERVER_PORT=8080
APP_BASE_URL=https://your-app-name.onrender.com
PRODUCTION_MODE=false

# Security
JWT_SECRET=<generate with: openssl rand -hex 32>
```

### Step 3: Revert application.properties

The `application.properties` file should have MySQL defaults (for local development), and you override with environment variables in Render.

---

## 📝 Correct PostgreSQL URL Format

**WRONG:**
```
postgresql://user:pass@host/db
jdbc:postgresql://user:pass@host/db
```

**CORRECT:**
```
jdbc:postgresql://host:port/database
```

**With separate credentials:**
- `DB_URL=jdbc:postgresql://host:5432/database`
- `DB_USERNAME=username`
- `DB_PASSWORD=password`

---

## 🔧 Quick Fix Steps

1. **In Render Dashboard:**
   - Go to your Web Service
   - Click "Environment" tab
   - Add/Update these variables:

```bash
DB_URL=jdbc:postgresql://dpg-d49hi4p5pdvs73ctrrvg-a:5432/reliable_carriers
DB_USERNAME=reliable_carriers_user
DB_PASSWORD=zMyj2mAYxzL1iFgy1pXsLAAHjxZanxqp
DB_DRIVER=org.postgresql.Driver
DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
DB_DDL_AUTO=update
```

2. **Redeploy** your application

3. **Check logs** - should see PostgreSQL connection successful

---

## ⚠️ Important Notes

1. **Never commit database credentials** to `application.properties`
2. **Always use environment variables** for production
3. **URL format must be:** `jdbc:postgresql://host:port/database`
4. **Credentials must be separate:** username and password as separate variables

---

## 🎯 After Fix

Your application should:
- ✅ Connect to PostgreSQL successfully
- ✅ Auto-create tables (with `DB_DDL_AUTO=update`)
- ✅ Start without errors

