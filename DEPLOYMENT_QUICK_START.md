# 🚀 Quick Deployment Guide

## Ready to Deploy! Let's Get Started

### **Step 1: Choose Your Hosting** (5 minutes)

**Recommended: Hetzner Cloud** (Best Value - R90/month)

1. **Sign up**: https://console.hetzner.com/
2. **Create Server**: 
   - Plan: CX11 (€4.51/month)
   - Image: Ubuntu 22.04
   - Location: Nuremberg (closest to SA)
3. **Note your server IP** (e.g., 123.45.67.89)

---

### **Step 2: Connect to Server** (2 minutes)

```bash
ssh root@YOUR_SERVER_IP
# Enter password when prompted
```

---

### **Step 3: One-Command Setup** (10 minutes)

Copy and paste this entire command block:

```bash
# Update system
apt update && apt upgrade -y

# Install Java 17
apt install -y openjdk-17-jdk

# Install MySQL
DEBIAN_FRONTEND=noninteractive apt install -y mysql-server

# Install Nginx
apt install -y nginx

# Install Certbot for SSL
apt install -y certbot python3-certbot-nginx

# Secure MySQL and create database
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'TempRootPass123!';"
mysql -e "CREATE DATABASE IF NOT EXISTS reliable_carriers;"
mysql -e "CREATE USER IF NOT EXISTS 'rc_user'@'localhost' IDENTIFIED BY 'ChangeThisPassword123!';"
mysql -e "GRANT ALL PRIVILEGES ON reliable_carriers.* TO 'rc_user'@'localhost';"
mysql -e "FLUSH PRIVILEGES;"

# Create application directory
mkdir -p /opt/reliable-carriers

# Setup firewall
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo "✅ Server setup complete!"
```

---

### **Step 4: Build Application** (5 minutes)

**On your local machine:**

```bash
# Build JAR file
mvn clean package -DskipTests

# Upload to server (replace YOUR_SERVER_IP)
scp target/Reliable-Carriers-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/reliable-carriers/
```

---

### **Step 5: Configure Application** (5 minutes)

**On the server**, create environment file:

```bash
nano /opt/reliable-carriers/.env
```

Add your production values:

```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/reliable_carriers?useSSL=false&serverTimezone=UTC
DB_USERNAME=rc_user
DB_PASSWORD=ChangeThisPassword123!

# JWT Secret (generate with: openssl rand -hex 32)
JWT_SECRET=your-generated-secret-key-minimum-32-characters-long

# Email
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password

# Google Maps
GOOGLE_MAPS_API_KEY=your-google-maps-key

# Paystack (USE LIVE KEYS IN PRODUCTION!)
PAYSTACK_SECRET_KEY=sk_live_your_production_key
PAYSTACK_PUBLIC_KEY=pk_live_your_production_key

# Application URL
APP_BASE_URL=https://yourdomain.com
PRODUCTION_MODE=true
```

**Save**: `Ctrl+X`, then `Y`, then `Enter`

---

### **Step 6: Create Systemd Service** (5 minutes)

```bash
cat > /etc/systemd/system/reliable-carriers.service << 'EOF'
[Unit]
Description=Reliable Carriers Application
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/reliable-carriers
ExecStart=/usr/bin/java -jar -Xmx1024m -Xms512m /opt/reliable-carriers/Reliable-Carriers-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
EnvironmentFile=/opt/reliable-carriers/.env

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
systemctl daemon-reload
systemctl enable reliable-carriers
systemctl start reliable-carriers

# Check status
systemctl status reliable-carriers
```

---

### **Step 7: Configure Nginx** (5 minutes)

```bash
cat > /etc/nginx/sites-available/reliable-carriers << 'EOF'
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    client_max_body_size 10M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# Enable site
ln -s /etc/nginx/sites-available/reliable-carriers /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Test and reload
nginx -t
systemctl reload nginx
```

---

### **Step 8: Configure Domain DNS** (5 minutes)

1. Go to your domain registrar (Afrihost, etc.)
2. Add A records:
   ```
   Type: A
   Name: @
   Value: YOUR_SERVER_IP
   
   Type: A
   Name: www
   Value: YOUR_SERVER_IP
   ```
3. Wait 5-60 minutes for DNS propagation

---

### **Step 9: Setup SSL Certificate** (5 minutes)

**After DNS is pointing to your server:**

```bash
certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

Follow prompts:
- Enter email
- Agree to terms
- Choose option 2 (Redirect HTTP to HTTPS)

**Update .env file** to use HTTPS:
```bash
nano /opt/reliable-carriers/.env
# Change APP_BASE_URL to https://yourdomain.com
systemctl restart reliable-carriers
```

---

### **Step 10: Verify Deployment** (5 minutes)

1. **Check application logs:**
   ```bash
   journalctl -u reliable-carriers -f
   ```

2. **Visit your site:**
   - http://yourdomain.com (should redirect to HTTPS)
   - https://yourdomain.com

3. **Test features:**
   - Homepage loads
   - Registration works
   - Login works
   - Tracking works

---

## ✅ Deployment Complete!

Your application is now live at **https://yourdomain.com**

---

## 📋 Post-Deployment Checklist

- [ ] Test registration
- [ ] Test login
- [ ] Test quote calculator
- [ ] Test package booking
- [ ] Test payment (use test mode first)
- [ ] Test tracking
- [ ] Check email notifications
- [ ] Test driver dashboard (if applicable)
- [ ] Monitor logs for errors
- [ ] Set up backups (recommended)

---

## 🔧 Useful Commands

```bash
# View application logs
journalctl -u reliable-carriers -f

# Restart application
systemctl restart reliable-carriers

# Check application status
systemctl status reliable-carriers

# View Nginx logs
tail -f /var/log/nginx/error.log

# Update application (after uploading new JAR)
systemctl restart reliable-carriers
```

---

## 📞 Need Help?

- **Full Guide**: See `DEPLOYMENT_STEPS.md`
- **Hosting Guide**: See `AFFORDABLE_HOSTING_GUIDE.md`
- **Troubleshooting**: Check logs and `DEPLOYMENT_STEPS.md`

---

**Congratulations! Your application is now live! 🎉**

