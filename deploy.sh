#!/bin/bash

# Reliable Carriers - Deployment Script
# This script helps you deploy the application to a Hetzner Cloud server

set -e

echo "🚀 Reliable Carriers - Deployment Script"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "❌ Please run as root (use: sudo bash deploy.sh)"
    exit 1
fi

echo "📦 Step 1: Updating system..."
apt update && apt upgrade -y

echo "☕ Step 2: Installing Java 17..."
apt install -y openjdk-17-jdk

echo "🗄️  Step 3: Installing MySQL..."
DEBIAN_FRONTEND=noninteractive apt install -y mysql-server

echo "🌐 Step 4: Installing Nginx..."
apt install -y nginx

echo "🔒 Step 5: Installing Certbot for SSL..."
apt install -y certbot python3-certbot-nginx

echo "🔧 Step 6: Setting up MySQL..."
read -p "Enter MySQL root password: " MYSQL_ROOT_PASS
read -p "Enter database name [reliable_carriers]: " DB_NAME
DB_NAME=${DB_NAME:-reliable_carriers}
read -p "Enter database user [rc_user]: " DB_USER
DB_USER=${DB_USER:-rc_user}
read -p "Enter database password: " DB_PASS

mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASS}';"
mysql -uroot -p${MYSQL_ROOT_PASS} -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME};"
mysql -uroot -p${MYSQL_ROOT_PASS} -e "CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';"
mysql -uroot -p${MYSQL_ROOT_PASS} -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';"
mysql -uroot -p${MYSQL_ROOT_PASS} -e "FLUSH PRIVILEGES;"

echo "📁 Step 7: Creating application directory..."
mkdir -p /opt/reliable-carriers

echo "🔥 Step 8: Setting up firewall..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo ""
echo "✅ Server setup complete!"
echo ""
echo "📝 Next steps:"
echo "1. Upload your JAR file to /opt/reliable-carriers/"
echo "2. Create /opt/reliable-carriers/.env file with your configuration"
echo "3. Create systemd service (see DEPLOYMENT_STEPS.md)"
echo "4. Configure Nginx (see DEPLOYMENT_STEPS.md)"
echo "5. Setup SSL certificate"
echo ""
echo "📚 See DEPLOYMENT_STEPS.md for complete instructions"

