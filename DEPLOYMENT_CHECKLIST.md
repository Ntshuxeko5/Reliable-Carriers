# Deployment Checklist for Render & Railway

Use this checklist to ensure your application is ready for deployment.

## ✅ Pre-Deployment Checklist

### 1. Files Created
- [x] `Dockerfile` - Container configuration
- [x] `.dockerignore` - Excludes unnecessary files from Docker build
- [x] `railway.json` - Railway deployment configuration
- [x] `render.yaml` - Render deployment configuration
- [x] `env.example` - Environment variables template
- [x] `RENDER_RAILWAY_DEPLOYMENT.md` - Complete deployment guide

### 2. Code Preparation
- [ ] Code is pushed to GitHub repository
- [ ] All secrets removed from `application.properties` (use environment variables)
- [ ] Database migrations ready (if using `ddl-auto=update` for initial setup)
- [ ] Test the application locally with Docker:
  ```bash
  docker build -t reliable-carriers .
  docker run -p 8080:8080 reliable-carriers
  ```

### 3. Environment Variables
- [ ] Copy `env.example` to `.env` (for local reference, don't commit)
- [ ] Generate secure `JWT_SECRET` (use: `openssl rand -hex 32`)
- [ ] Prepare all API keys:
  - [ ] Gmail App Password
  - [ ] SMSPortal API Key & Secret
  - [ ] Google Maps API Key
  - [ ] Paystack Live Keys (not test keys!)
  - [ ] Google OAuth2 Client ID & Secret
  - [ ] Facebook OAuth2 Client ID & Secret

### 4. Database Setup
Choose one option:
- [ ] **Option A**: External MySQL database (PlanetScale, Aiven, etc.)
- [ ] **Option B**: Render PostgreSQL (free tier available)
- [ ] **Option C**: Railway PostgreSQL (included)

**Note**: If using PostgreSQL, Spring Boot will auto-detect the driver from the JDBC URL. No code changes needed!

### 5. OAuth2 Configuration
- [ ] Update Google OAuth2 redirect URIs:
  - Production: `https://your-domain.com/login/oauth2/code/google`
  - Staging: `https://staging.your-domain.com/login/oauth2/code/google`
- [ ] Update Facebook OAuth2 redirect URIs:
  - Production: `https://your-domain.com/login/oauth2/code/facebook`
  - Staging: `https://staging.your-domain.com/login/oauth2/code/facebook`

---

## 🚀 Railway Deployment Checklist

### Step 1: Initial Setup
- [ ] Sign up at [railway.app](https://railway.app)
- [ ] Create new project
- [ ] Connect GitHub repository
- [ ] Select your repository

### Step 2: Database (Optional)
- [ ] Add PostgreSQL database (if using Railway DB)
- [ ] Note connection details

### Step 3: Environment Variables
- [ ] Set `DB_URL` (or use Railway's `${{Postgres.DATABASE_URL}}`)
- [ ] Set `DB_USERNAME` and `DB_PASSWORD`
- [ ] Set `PRODUCTION_MODE=true`
- [ ] Set `APP_BASE_URL` (update after getting Railway URL)
- [ ] Set `JWT_SECRET` (secure random string)
- [ ] Set all API keys and secrets
- [ ] Set `DB_DDL_AUTO=validate` (or `update` for initial setup)

### Step 4: Deploy
- [ ] Railway auto-deploys on push
- [ ] Check build logs
- [ ] Verify app is running at `https://your-app.up.railway.app`
- [ ] Test health endpoint: `/actuator/health`

### Step 5: Custom Domain (Optional)
- [ ] Add custom domain in Railway settings
- [ ] Update DNS records
- [ ] Update `APP_BASE_URL` environment variable
- [ ] Update OAuth2 redirect URIs

---

## 🌐 Render Deployment Checklist

### Step 1: Initial Setup
- [ ] Sign up at [render.com](https://render.com)
- [ ] Create new Web Service
- [ ] Connect GitHub repository
- [ ] Select your repository

### Step 2: Service Configuration
- [ ] Name: `reliable-carriers` (or your preferred name)
- [ ] Environment: `Docker`
- [ ] Region: Choose closest to users
- [ ] Branch: `main` (or `staging`)
- [ ] Plan: `Free` (or `Starter` for always-on)

### Step 3: Database (Optional)
- [ ] Create PostgreSQL database (if using Render DB)
- [ ] Note connection details

### Step 4: Environment Variables
- [ ] Set all variables from `env.example`
- [ ] Update `APP_BASE_URL` after deployment
- [ ] Set `PRODUCTION_MODE=true` (or `false` for staging)
- [ ] Use Render's secret management for sensitive values

### Step 5: Deploy
- [ ] Click "Create Web Service"
- [ ] Wait for build (5-10 minutes first time)
- [ ] Verify app is running at `https://your-app.onrender.com`
- [ ] Test health endpoint: `/actuator/health`

### Step 6: Custom Domain (Optional)
- [ ] Add custom domain in Render settings
- [ ] Update DNS records
- [ ] Update `APP_BASE_URL` environment variable
- [ ] Update OAuth2 redirect URIs

---

## 🔍 Post-Deployment Verification

### Application Health
- [ ] Health check endpoint works: `/actuator/health`
- [ ] Application starts without errors
- [ ] Database connection successful
- [ ] Logs show no critical errors

### Functionality Tests
- [ ] User registration works
- [ ] User login works
- [ ] OAuth2 login works (Google/Facebook)
- [ ] Email notifications sent
- [ ] SMS notifications sent (if enabled)
- [ ] Payment integration works (Paystack)
- [ ] File uploads work
- [ ] API endpoints respond correctly

### Security Checks
- [ ] HTTPS enabled (automatic on both platforms)
- [ ] Environment variables not exposed in logs
- [ ] Production mode enabled
- [ ] Debug logging disabled
- [ ] Swagger access restricted (if needed)

---

## 🐛 Troubleshooting

### Build Fails
- Check Dockerfile is in root directory
- Check build logs for specific errors
- Test Docker build locally: `docker build -t test .`

### Application Won't Start
- Check all required environment variables are set
- Check database connection string
- Check application logs in dashboard
- Verify `SERVER_PORT=8080` is set

### Database Connection Issues
- Verify connection string format
- Check database allows connections from platform IPs
- Verify credentials are correct
- For PostgreSQL: Ensure driver auto-detects (it should)

### OAuth2 Not Working
- Verify redirect URIs match exactly (including https)
- Check client IDs and secrets
- Verify `APP_BASE_URL` matches actual domain

---

## 📝 Quick Reference

### Generate JWT Secret
```bash
openssl rand -hex 32
```

### Test Docker Build Locally
```bash
docker build -t reliable-carriers .
docker run -p 8080:8080 -e DB_URL=jdbc:mysql://host:3306/db reliable-carriers
```

### Health Check URLs
- Railway: `https://your-app.up.railway.app/actuator/health`
- Render: `https://your-app.onrender.com/actuator/health`

### Important Environment Variables
```bash
PRODUCTION_MODE=true
DB_DDL_AUTO=validate  # After initial setup
APP_BASE_URL=https://your-domain.com
JWT_SECRET=<generated-secret>
```

---

## 🎯 Next Steps After Deployment

1. [ ] Set up monitoring (both platforms have built-in monitoring)
2. [ ] Configure automated backups (for database)
3. [ ] Set up staging environment (if not already done)
4. [ ] Configure CI/CD (automatic deployments on push)
5. [ ] Set up error tracking (Sentry, etc.)
6. [ ] Configure logging aggregation
7. [ ] Set up performance monitoring

---

**Ready to deploy? Follow the step-by-step guide in `RENDER_RAILWAY_DEPLOYMENT.md`!**

