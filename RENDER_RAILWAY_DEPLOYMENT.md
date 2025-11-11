# Deployment Guide: Render & Railway

This guide will help you deploy the Reliable Carriers application to both Render and Railway platforms.

## 📋 Prerequisites

1. **GitHub Account** - Your code should be in a GitHub repository
2. **Render Account** - Sign up at [render.com](https://render.com)
3. **Railway Account** - Sign up at [railway.app](https://railway.app)
4. **Database** - Choose one:
   - External MySQL database (recommended for production)
   - Render PostgreSQL (free tier available)
   - Railway PostgreSQL (included)

## 🚀 Quick Start

### Option 1: Deploy to Railway (Recommended for Production)

#### Step 1: Connect Repository
1. Go to [railway.app](https://railway.app)
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Authorize Railway to access your GitHub
5. Select your repository

#### Step 2: Configure Build
- Railway will auto-detect the Dockerfile
- No additional configuration needed if `railway.json` is present

#### Step 3: Add Database (Optional)
1. Click "New" → "Database" → "Add PostgreSQL"
2. Railway will automatically create a PostgreSQL database
3. The connection string will be available as environment variables

#### Step 4: Set Environment Variables
Go to your service → "Variables" tab and add:

```bash
# Database (if using Railway PostgreSQL)
DB_URL=${{Postgres.DATABASE_URL}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# Or for external MySQL
DB_URL=jdbc:mysql://your-host:3306/reliable_carriers
DB_USERNAME=your_user
DB_PASSWORD=your_password

# Application
SERVER_PORT=8080
PRODUCTION_MODE=true
APP_BASE_URL=https://your-app-name.up.railway.app

# JWT (generate a secure secret)
JWT_SECRET=your-very-long-random-secret-key

# Email
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password

# SMS
SMSPORTAL_API_KEY=your-key
SMSPORTAL_API_SECRET=your-secret

# Google Maps
GOOGLE_MAPS_API_KEY=your-key

# Paystack (PRODUCTION KEYS)
PAYSTACK_SECRET_KEY=sk_live_your_key
PAYSTACK_PUBLIC_KEY=pk_live_your_key
PAYSTACK_WEBHOOK_SECRET=your-webhook-secret

# OAuth2
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
FACEBOOK_CLIENT_ID=your-client-id
FACEBOOK_CLIENT_SECRET=your-client-secret

# Logging
LOG_LEVEL_RELIABLE_CARRIERS=INFO
LOG_LEVEL_SECURITY=WARN
DB_DDL_AUTO=validate
```

#### Step 5: Deploy
- Railway will automatically deploy on every push to your main branch
- Check the "Deployments" tab for build logs
- Your app will be available at `https://your-app-name.up.railway.app`

#### Step 6: Custom Domain (Optional)
1. Go to "Settings" → "Domains"
2. Add your custom domain
3. Update DNS records as instructed
4. Update `APP_BASE_URL` environment variable

---

### Option 2: Deploy to Render

#### Step 1: Create Web Service
1. Go to [render.com](https://render.com)
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Select your repository

#### Step 2: Configure Service
- **Name**: `reliable-carriers` (or your preferred name)
- **Environment**: `Docker`
- **Region**: Choose closest to your users
- **Branch**: `main` (or `staging` for staging environment)
- **Root Directory**: (leave blank)
- **Build Command**: (leave blank - Docker handles this)
- **Start Command**: (leave blank - defined in Dockerfile)

#### Step 3: Add Database (Optional)
1. Click "New +" → "PostgreSQL"
2. Name: `reliable-carriers-db`
3. Plan: `Free` (90 days) or `Starter` ($7/month)
4. Note the connection details

#### Step 4: Set Environment Variables
In the "Environment" section, add all variables from `.env.example`:

**Quick Copy-Paste (update values):**
```bash
SERVER_PORT=8080
PRODUCTION_MODE=true
APP_BASE_URL=https://your-app-name.onrender.com
DB_URL=jdbc:postgresql://your-render-db-host:5432/reliable_carriers
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
DB_DDL_AUTO=validate
JWT_SECRET=your-secret-key
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password
SMSPORTAL_API_KEY=your-key
SMSPORTAL_API_SECRET=your-secret
GOOGLE_MAPS_API_KEY=your-key
PAYSTACK_SECRET_KEY=sk_live_your_key
PAYSTACK_PUBLIC_KEY=pk_live_your_key
PAYSTACK_WEBHOOK_SECRET=your-webhook-secret
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
FACEBOOK_CLIENT_ID=your-client-id
FACEBOOK_CLIENT_SECRET=your-client-secret
LOG_LEVEL_RELIABLE_CARRIERS=INFO
LOG_LEVEL_SECURITY=WARN
```

**For Render PostgreSQL**, you can use:
```bash
DB_URL=${{reliable-carriers-db.DATABASE_URL}}
```

#### Step 5: Deploy
1. Click "Create Web Service"
2. Render will build and deploy your application
3. First deployment takes 5-10 minutes
4. Your app will be available at `https://your-app-name.onrender.com`

#### Step 6: Custom Domain (Optional)
1. Go to "Settings" → "Custom Domains"
2. Add your domain
3. Update DNS records
4. Update `APP_BASE_URL` environment variable

---

## 🔄 Deploying to Both Platforms

### Recommended Setup: Staging + Production

**Railway (Production):**
- Always-on instance
- Production database
- Production domain
- Cost: ~$5-10/month

**Render (Staging):**
- Free tier for testing
- Staging database
- Staging subdomain
- Cost: Free (or $7/month for always-on)

### Setup Steps:

1. **Create Staging Branch**
   ```bash
   git checkout -b staging
   git push origin staging
   ```

2. **Deploy to Railway (Production)**
   - Connect `main` branch
   - Set `PRODUCTION_MODE=true`
   - Use production database
   - Use production API keys

3. **Deploy to Render (Staging)**
   - Connect `staging` branch
   - Set `PRODUCTION_MODE=false`
   - Use staging database
   - Use test API keys

4. **Update OAuth2 Redirect URIs**
   - Add production URL to Google/Facebook OAuth2 console
   - Add staging URL to Google/Facebook OAuth2 console

---

## 🗄️ Database Setup

### Option 1: External MySQL (Recommended)

Use a managed MySQL service like:
- **PlanetScale** (free tier available)
- **Aiven** (free tier available)
- **DigitalOcean Managed Database**
- **AWS RDS**

Then set:
```bash
DB_URL=jdbc:mysql://your-host:3306/reliable_carriers?useSSL=true&serverTimezone=UTC
DB_USERNAME=your_user
DB_PASSWORD=your_password
```

### Option 2: Render PostgreSQL

1. Create PostgreSQL service in Render
2. Use connection string:
```bash
DB_URL=${{your-db-name.DATABASE_URL}}
```

### Option 3: Railway PostgreSQL

1. Add PostgreSQL database in Railway
2. Use Railway's automatic variables:
```bash
DB_URL=${{Postgres.DATABASE_URL}}
```

**Note**: If using PostgreSQL, update `application.properties`:
```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

---

## 🔐 Security Checklist

Before deploying to production:

- [ ] All secrets moved to environment variables
- [ ] `PRODUCTION_MODE=true` set
- [ ] `DB_DDL_AUTO=validate` (not `update`)
- [ ] Strong `JWT_SECRET` generated (min 32 characters)
- [ ] Production Paystack keys (not test keys)
- [ ] OAuth2 redirect URIs updated
- [ ] `APP_BASE_URL` set to production domain
- [ ] Debug logging disabled
- [ ] Swagger access restricted (if needed)

---

## 📊 Monitoring & Health Checks

Both platforms support health checks:

**Health Check Endpoint**: `/actuator/health`

**Railway**: Automatically configured in `railway.json`

**Render**: Configured in `render.yaml` or manually:
- Health Check Path: `/actuator/health`
- Health Check Timeout: 100 seconds

---

## 🔧 Troubleshooting

### Build Fails

1. **Check Dockerfile**: Ensure it's in the root directory
2. **Check logs**: View build logs in Railway/Render dashboard
3. **Test locally**: Run `docker build -t test .` locally

### Application Won't Start

1. **Check environment variables**: Ensure all required variables are set
2. **Check database connection**: Verify database URL and credentials
3. **Check logs**: View application logs in dashboard
4. **Check port**: Ensure `SERVER_PORT=8080` is set

### Database Connection Issues

1. **Check connection string**: Verify format is correct
2. **Check firewall**: Ensure database allows connections from platform IPs
3. **Check credentials**: Verify username and password
4. **For PostgreSQL**: Ensure driver is set correctly

### OAuth2 Not Working

1. **Check redirect URIs**: Must match exactly (including https)
2. **Check client IDs/secrets**: Verify they're correct
3. **Check `APP_BASE_URL`**: Must match your actual domain

---

## 💰 Cost Comparison

### Railway
- **Free Tier**: $5 credit/month (usually enough for small apps)
- **Paid**: ~$5-10/month for always-on
- **Database**: PostgreSQL included, or use external

### Render
- **Free Tier**: Sleeps after 15 min inactivity
- **Starter**: $7/month (always-on, 512MB RAM)
- **Database**: PostgreSQL free tier (90 days), then $7/month

### Recommended Setup
- **Railway (Production)**: $5-10/month
- **Render (Staging)**: Free (or $7/month for always-on)
- **Total**: $5-17/month

---

## 🚀 Continuous Deployment

Both platforms support automatic deployments:

**Railway**: Deploys on every push to connected branch

**Render**: Deploys on every push to configured branch

To deploy manually:
- **Railway**: Click "Redeploy" in dashboard
- **Render**: Click "Manual Deploy" → "Deploy latest commit"

---

## 📝 Environment Variables Reference

See `.env.example` for complete list of environment variables.

**Required for Production:**
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `APP_BASE_URL`
- `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD`
- `PAYSTACK_SECRET_KEY`, `PAYSTACK_PUBLIC_KEY`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET`

**Optional:**
- `SMSPORTAL_API_KEY`, `SMSPORTAL_API_SECRET`
- `GOOGLE_MAPS_API_KEY`
- All other variables have defaults

---

## 🎯 Next Steps

1. ✅ Deploy to Railway or Render
2. ✅ Set up database
3. ✅ Configure environment variables
4. ✅ Test the application
5. ✅ Set up custom domain
6. ✅ Configure OAuth2 redirect URIs
7. ✅ Set up monitoring
8. ✅ Configure backups

---

## 📞 Support

- **Railway Docs**: https://docs.railway.app
- **Render Docs**: https://render.com/docs
- **Spring Boot Deployment**: https://spring.io/guides/gs/spring-boot-docker/

---

**Happy Deploying! 🚀**

