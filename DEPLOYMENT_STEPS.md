# Quick Deployment Steps for Testing

## 🚀 Quick Start (Hetzner Cloud - R95/month)

### **Prerequisites:**
1. Application built: `mvn clean package -DskipTests`
2. Domain purchased (or will purchase)
3. Credit card for Hetzner

---

## ⚡ **5-Minute Quick Setup**

### **1. Create Server (5 min)**
```
1. Go to: https://console.hetzner.com/
2. Sign up / Login
3. Create Project → "Reliable Carriers Test"
4. Click "Add Server"
5. Select: CX11 (€4.51/month)
6. Image: Ubuntu 22.04
7. Location: Nuremberg (closest to SA)
8. SSH Key: Add your key (or use password)
9. Click "Create & Buy Now"
10. Wait 2 minutes for server to start
```

### **2. Connect to Server**
```bash
ssh root@YOUR_SERVER_IP
# or if using password:
ssh root@YOUR_SERVER_IP
# Enter password when prompted
```

### **3. One-Command Setup Script**
```bash
# Copy and paste this entire block:
cat > setup.sh << 'EOF'
#!/bin/bash
set -e

echo "🚀 Setting up Reliable Carriers server..."

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

# Secure MySQL
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'TempRootPass123!';"
mysql -e "DELETE FROM mysql.user WHERE User='';"
mysql -e "DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');"
mysql -e "DROP DATABASE IF EXISTS test;"
mysql -e "DELETE FROM mysql.db WHERE Db='test' OR Db='test\\_%';"
mysql -e "FLUSH PRIVILEGES;"

# Create database and user
mysql -uroot -pTempRootPass123! <<MYSQL_SCRIPT
CREATE DATABASE IF NOT EXISTS reliable_carriers;
CREATE USER IF NOT EXISTS 'rc_user'@'localhost' IDENTIFIED BY 'ChangeThisPassword123!';
GRANT ALL PRIVILEGES ON reliable_carriers.* TO 'rc_user'@'localhost';
FLUSH PRIVILEGES;
MYSQL_SCRIPT

# Create application directory
mkdir -p /opt/reliable-carriers

echo "✅ Server setup complete!"
echo ""
echo "📝 Next steps:"
echo "1. Upload your JAR file to /opt/reliable-carriers/"
echo "2. Update MySQL password in database creation script above"
echo "3. Configure application with environment variables"
EOF

chmod +x setup.sh
./setup.sh
```

### **4. Upload Your Application**
```bash
# From your LOCAL machine (not server):
cd /path/to/your/project

# Upload JAR file
scp target/Reliable-Carriers-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/reliable-carriers/

# If you don't have JAR built yet:
mvn clean package -DskipTests
scp target/Reliable-Carriers-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/reliable-carriers/
```

### **5. Create Systemd Service**
```bash
# On server, create service file:
cat > /etc/systemd/system/reliable-carriers.service << 'EOF'
[Unit]
Description=Reliable Carriers Application
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/reliable-carriers
ExecStart=/usr/bin/java -jar -Xmx512m -Xms256m /opt/reliable-carriers/Reliable-Carriers-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=reliable-carriers
Environment="DB_URL=jdbc:mysql://localhost:3306/reliable_carriers?useSSL=false&serverTimezone=UTC"
Environment="DB_USERNAME=rc_user"
Environment="DB_PASSWORD=ChangeThisPassword123!"
Environment="PRODUCTION_MODE=true"
Environment="APP_BASE_URL=http://YOUR_SERVER_IP:8080"
Environment="JWT_SECRET=your-production-jwt-secret-change-this-min-32-characters"

[Install]
WantedBy=multi-user.target
EOF

# Reload and start
systemctl daemon-reload
systemctl enable reliable-carriers
systemctl start reliable-carriers

# Check status
systemctl status reliable-carriers

# View logs
journalctl -u reliable-carriers -f
```

### **6. Configure Nginx**
```bash
# Create Nginx config
cat > /etc/nginx/sites-available/reliable-carriers << 'EOF'
server {
    listen 80;
    server_name YOUR_DOMAIN.com www.YOUR_DOMAIN.com;

    # Increase body size for file uploads
    client_max_body_size 10M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# Enable site
ln -s /etc/nginx/sites-available/reliable-carriers /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default  # Remove default if exists

# Test and reload
nginx -t
systemctl reload nginx
```

### **7. Setup Firewall**
```bash
# Allow required ports
ufw allow 22/tcp   # SSH
ufw allow 80/tcp   # HTTP
ufw allow 443/tcp  # HTTPS
ufw enable
```

### **8. Configure Domain DNS**
1. Go to your domain registrar (Afrihost, etc.)
2. Find DNS management
3. Add these records:
   ```
   Type: A
   Name: @
   Value: YOUR_SERVER_IP
   TTL: 3600
   
   Type: A
   Name: www
   Value: YOUR_SERVER_IP
   TTL: 3600
   ```
4. Wait 5-60 minutes for DNS to propagate

### **9. Setup SSL (Free HTTPS)**
```bash
# Once DNS is pointing to your server, run:
certbot --nginx -d YOUR_DOMAIN.com -d www.YOUR_DOMAIN.com

# Follow prompts:
# - Enter email
# - Agree to terms
# - Choose to redirect HTTP to HTTPS (option 2)

# Update APP_BASE_URL in service file:
sed -i 's|APP_BASE_URL=http://|APP_BASE_URL=https://|g' /etc/systemd/system/reliable-carriers.service
systemctl daemon-reload
systemctl restart reliable-carriers
```

### **10. Update Application Environment Variables**
```bash
# Edit service file to add all required variables:
nano /etc/systemd/system/reliable-carriers.service

# Add all your environment variables (see .env.example)
# Example:
Environment="GMAIL_USERNAME=your-email@gmail.com"
Environment="GMAIL_APP_PASSWORD=your-app-password"
Environment="GOOGLE_MAPS_API_KEY=your-key"
# ... etc

# Reload and restart
systemctl daemon-reload
systemctl restart reliable-carriers
```

---

## ✅ **Verification**

### **Check Application:**
```bash
# Check if running
systemctl status reliable-carriers

# Check logs
journalctl -u reliable-carriers -n 50

# Test locally
curl http://localhost:8080

# Test from outside
curl http://YOUR_SERVER_IP:8080
```

### **Check Nginx:**
```bash
# Test config
nginx -t

# Check status
systemctl status nginx

# Test from browser
# http://YOUR_DOMAIN.com
```

---

## 🔧 **Troubleshooting**

### **Application Not Starting:**
```bash
# Check logs
journalctl -u reliable-carriers -n 100

# Check Java version
java -version  # Should be 17+

# Check if port is in use
netstat -tlnp | grep 8080
```

### **Database Connection Issues:**
```bash
# Test MySQL connection
mysql -u rc_user -p reliable_carriers

# Check MySQL status
systemctl status mysql

# Check if database exists
mysql -u root -p -e "SHOW DATABASES;"
```

### **Nginx Issues:**
```bash
# Check Nginx logs
tail -f /var/log/nginx/error.log

# Test configuration
nginx -t

# Check if Nginx is running
systemctl status nginx
```

---

## 📝 **Quick Reference Commands**

```bash
# View application logs
journalctl -u reliable-carriers -f

# Restart application
systemctl restart reliable-carriers

# Stop application
systemctl stop reliable-carriers

# Start application
systemctl start reliable-carriers

# Update application (after uploading new JAR)
systemctl restart reliable-carriers

# Check application status
systemctl status reliable-carriers

# Reload Nginx
systemctl reload nginx

# Check SSL certificate expiry
certbot certificates

# Renew SSL certificate
certbot renew
```

---

## 💰 **Total Cost Breakdown**

- **Hetzner Cloud (CX11)**: €4.51/month ≈ **R90/month**
- **Domain (.co.za)**: R59/year ≈ **R5/month**
- **SSL Certificate**: Free (Let's Encrypt)
- **Total**: **~R95/month** (~$5/month)

---

## 🎯 **You're Done!**

Your application should now be accessible at:
- **HTTP**: http://YOUR_DOMAIN.com
- **HTTPS**: https://YOUR_DOMAIN.com (after SSL setup)

**Next Steps:**
1. Test all features
2. Monitor logs
3. Set up backups (optional)
4. Configure monitoring (optional)

---

**Need help? Check the main AFFORDABLE_HOSTING_GUIDE.md for detailed information!**

