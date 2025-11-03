# Affordable Hosting Guide for Testing

## 🎯 Best Options for Testing with Custom Domain

### **Recommended: VPS (Virtual Private Server) - Best Value**

---

## 🥇 **Top 3 Recommended Hosting Providers**

### **1. Hetzner Cloud (Best Value - €4.51/month ≈ R90/month)**
- **Cost**: €4.51/month (~R90/month)
- **Specs**: 2 vCPU, 4GB RAM, 40GB SSD
- **Location**: Germany (good for South Africa)
- **Pros**: 
  - Excellent price/performance ratio
  - Great for Spring Boot applications
  - Simple setup
  - Free bandwidth (20TB)
- **Cons**: Payment in EUR
- **Best for**: Production-like testing environment

### **2. DigitalOcean (Easy Setup - $6/month ≈ R110/month)**
- **Cost**: $6/month (~R110/month)
- **Specs**: 1 vCPU, 1GB RAM, 25GB SSD
- **Location**: Multiple regions (including Amsterdam)
- **Pros**: 
  - Excellent documentation
  - Easy deployment (Droplets)
  - Great community
  - Simple control panel
- **Cons**: Slightly more expensive
- **Best for**: Easy deployment for beginners

### **3. Vultr (Great Performance - $6/month ≈ R110/month)**
- **Cost**: $6/month (~R110/month)
- **Specs**: 1 vCPU, 1GB RAM, 25GB SSD
- **Location**: Multiple regions
- **Pros**: 
  - High performance
  - Good documentation
  - Simple interface
- **Best for**: Balanced option

---

## 💰 **Cost Comparison**

| Provider | Monthly Cost | RAM | Storage | Best For |
|----------|-------------|-----|---------|----------|
| **Hetzner** | €4.51 (R90) | 4GB | 40GB | Best value |
| **DigitalOcean** | $6 (R110) | 1GB | 25GB | Easiest setup |
| **Vultr** | $6 (R110) | 1GB | 25GB | Good balance |
| **Linode** | $5 (R92) | 1GB | 25GB | Alternative |

**Recommendation**: **Hetzner Cloud** for best value (4GB RAM is perfect for Spring Boot + MySQL)

---

## 🌐 **Domain Options**

### **Affordable Domain Registrars (South Africa):**

1. **Afrihost** (Local)
   - .co.za: R59/year (~R5/month)
   - .com: R139/year (~R12/month)
   - **Best for**: Local support, easy payment

2. **Namecheap**
   - .com: ~$8.88/year (~R160/year = R13/month)
   - **Best for**: International, cheap

3. **Google Domains** (Now Squarespace)
   - .com: ~$12/year (~R220/year = R18/month)

**Total Monthly Cost**: R90 (hosting) + R5-18 (domain) = **R95-108/month**

---

## 🚀 **Step-by-Step Deployment Guide**

### **Option 1: Hetzner Cloud (Recommended)**

#### **Step 1: Create Hetzner Account**
1. Go to https://www.hetzner.com/cloud
2. Sign up (need credit card)
3. Create new project
4. Create Cloud Server:
   - **Type**: CX11 (€4.51/month)
   - **Image**: Ubuntu 22.04
   - **Location**: Choose closest (Nuremberg recommended)
   - **SSH Key**: Add your public key (optional but recommended)
   - Create server

#### **Step 2: Configure Server**
```bash
# SSH into your server
ssh root@YOUR_SERVER_IP

# Update system
apt update && apt upgrade -y

# Install Java 17 (required for Spring Boot)
apt install -y openjdk-17-jdk

# Install MySQL
apt install -y mysql-server

# Install Nginx (reverse proxy)
apt install -y nginx

# Install Certbot (for free SSL)
apt install -y certbot python3-certbot-nginx
```

#### **Step 3: Configure MySQL**
```bash
# Secure MySQL installation
mysql_secure_installation

# Create database and user
mysql -u root -p
```

```sql
CREATE DATABASE reliable_carriers;
CREATE USER 'rc_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON reliable_carriers.* TO 'rc_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### **Step 4: Deploy Application**
```bash
# Create application directory
mkdir -p /opt/reliable-carriers
cd /opt/reliable-carriers

# Upload your JAR file (use scp from your local machine)
# scp target/Reliable-Carriers-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/reliable-carriers/

# Create systemd service
nano /etc/systemd/system/reliable-carriers.service
```

**Service file content:**
```ini
[Unit]
Description=Reliable Carriers Application
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/reliable-carriers
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/reliable-carriers/Reliable-Carriers-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=reliable-carriers
Environment="DB_URL=jdbc:mysql://localhost:3306/reliable_carriers"
Environment="DB_USERNAME=rc_user"
Environment="DB_PASSWORD=your_secure_password"
Environment="PRODUCTION_MODE=true"
Environment="APP_BASE_URL=https://yourdomain.com"

[Install]
WantedBy=multi-user.target
```

```bash
# Reload systemd and start service
systemctl daemon-reload
systemctl enable reliable-carriers
systemctl start reliable-carriers
```

#### **Step 5: Configure Nginx (Reverse Proxy)**
```bash
nano /etc/nginx/sites-available/reliable-carriers
```

**Nginx configuration:**
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Enable site
ln -s /etc/nginx/sites-available/reliable-carriers /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx
```

#### **Step 6: Setup SSL (Free with Let's Encrypt)**
```bash
# Get SSL certificate
certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal is automatic, but test it:
certbot renew --dry-run
```

#### **Step 7: Configure Domain DNS**
**In your domain registrar (Afrihost, etc.):**

1. Go to DNS management
2. Add A records:
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
3. Wait 5-60 minutes for DNS propagation

---

## 🐳 **Alternative: Docker Deployment (Even Easier)**

### **Create Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/Reliable-Carriers-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

### **Docker Compose Setup:**
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://db:3306/reliable_carriers
      - DB_USERNAME=rc_user
      - DB_PASSWORD=your_password
      - PRODUCTION_MODE=true
      - APP_BASE_URL=https://yourdomain.com
    depends_on:
      - db
    restart: always

  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=reliable_carriers
      - MYSQL_USER=rc_user
      - MYSQL_PASSWORD=your_password
      - MYSQL_ROOT_PASSWORD=root_password
    volumes:
      - db_data:/var/lib/mysql
    restart: always

volumes:
  db_data:
```

**Deploy:**
```bash
docker-compose up -d
```

---

## 📋 **Complete Setup Checklist**

### **Before Deployment:**
- [ ] Choose hosting provider (recommend Hetzner)
- [ ] Purchase domain (Afrihost or Namecheap)
- [ ] Build application: `mvn clean package -DskipTests`
- [ ] Create `.env` file with production values
- [ ] Test application locally with production settings

### **Server Setup:**
- [ ] Create VPS instance
- [ ] Configure firewall (allow 80, 443, 22, 3306)
- [ ] Install Java 17
- [ ] Install MySQL
- [ ] Install Nginx
- [ ] Create database and user
- [ ] Deploy application JAR
- [ ] Create systemd service
- [ ] Configure Nginx reverse proxy
- [ ] Setup SSL with Let's Encrypt
- [ ] Configure DNS records
- [ ] Test application

---

## 🔒 **Security Setup**

### **Firewall Configuration (UFW):**
```bash
# Install UFW
apt install -y ufw

# Allow SSH, HTTP, HTTPS
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp

# Enable firewall
ufw enable
```

### **MySQL Security:**
- Only allow localhost connections
- Use strong passwords
- Disable root remote login

---

## 💡 **Tips for Cost Savings**

1. **Use Hetzner** - Best value (R90/month vs R110+)
2. **Buy .co.za domain** - Only R59/year (R5/month)
3. **Use Let's Encrypt** - Free SSL certificates
4. **Monitor usage** - Set up alerts for overages
5. **Start with smallest instance** - Can always upgrade later

---

## 📊 **Monthly Cost Breakdown**

### **Hetzner Setup:**
- VPS Server: R90/month
- Domain (.co.za): R5/month
- SSL Certificate: Free (Let's Encrypt)
- **Total: R95/month** (~$5/month)

### **DigitalOcean Setup:**
- Droplet: R110/month
- Domain: R5-18/month
- SSL Certificate: Free
- **Total: R115-128/month** (~$6-7/month)

---

## 🆓 **Free Alternatives (Limited)**

### **Railway.app**
- **Free tier**: $5 credit/month
- **Good for**: Quick testing
- **Limitations**: Sleeps after inactivity
- **Custom domain**: Supported

### **Render**
- **Free tier**: Available
- **Good for**: Testing
- **Limitations**: Sleeps after inactivity
- **Custom domain**: Supported

**Note**: Free tiers sleep after inactivity, so not ideal for continuous testing.

---

## 🎯 **Recommendation**

### **For Your Testing Needs:**

**Best Option: Hetzner Cloud + Afrihost Domain**
- **Total Cost**: ~R95/month (~$5/month)
- **Why**: 
  - 4GB RAM is perfect for Spring Boot + MySQL
  - Excellent performance for price
  - Local domain (.co.za) is cheaper
  - Full control over server
  - Can scale easily

### **Quick Start Steps:**
1. Sign up for Hetzner Cloud (€4.51/month)
2. Buy domain from Afrihost (R59/year for .co.za)
3. Follow deployment guide above
4. Total setup time: 1-2 hours
5. **Total monthly cost: R95/month**

---

## 📞 **Support Resources**

- **Hetzner Docs**: https://docs.hetzner.com/
- **Spring Boot Deployment**: https://spring.io/guides/gs/spring-boot-for-azure/
- **Nginx Setup**: https://www.nginx.com/blog/spring-boot-nginx/
- **Let's Encrypt**: https://letsencrypt.org/docs/

---

**Ready to deploy? Follow the step-by-step guide above!** 🚀

