#!/bin/bash

# Tailwind CSS Build Script for Reliable Carriers
# This script builds the production CSS from the Tailwind configuration

echo "Building Tailwind CSS for Reliable Carriers..."

# Check if Tailwind CLI is installed
if ! command -v npx &> /dev/null; then
    echo "Error: npx is not installed. Please install Node.js and npm first."
    exit 1
fi

# Build the CSS
echo "Building CSS from Tailwind configuration..."
npx tailwindcss -i ./src/main/resources/static/css/tailwind.css -o ./src/main/resources/static/css/tailwind-built.css --minify

if [ $? -eq 0 ]; then
    echo "✅ CSS build successful!"
    echo "Built file: src/main/resources/static/css/tailwind-built.css"
    echo ""
    echo "To use the built CSS in production:"
    echo "1. Update templates to reference '/css/tailwind-built.css' instead of '/css/tailwind.css'"
    echo "2. The built file is minified and optimized for production"
else
    echo "❌ CSS build failed!"
    exit 1
fi
