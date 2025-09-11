#!/bin/bash

echo "========================================"
echo "Reliable Carriers - Quick Start Script"
echo "========================================"
echo

# Check Java installation
echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or later"
    exit 1
fi

# Check Maven installation
echo "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or later"
    exit 1
fi

# Check MySQL connection
echo "Checking MySQL connection..."
if ! mysql -u root -p -e "SELECT 1;" &> /dev/null; then
    echo "WARNING: MySQL connection failed"
    echo "Please ensure MySQL is running and accessible"
    echo "You may need to set up the database first"
    echo
    echo "To set up the database:"
    echo "1. Run: mysql -u root -p < database-setup.sql"
    echo "2. Or follow the MYSQL_SETUP_GUIDE.md"
    echo
    read -p "Continue anyway? (y/n): " continue
    if [[ ! $continue =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo
echo "Building the application..."
if ! mvn clean install -DskipTests; then
    echo "ERROR: Build failed"
    exit 1
fi

echo
echo "Starting the application..."
echo "Application will be available at: http://localhost:8080"
echo
echo "Default login credentials:"
echo "Admin: admin@reliablecarriers.com / admin123"
echo "Driver: driver@reliablecarriers.com / admin123"
echo "Customer: customer@example.com / admin123"
echo
echo "Press Ctrl+C to stop the application"
echo

mvn spring-boot:run
