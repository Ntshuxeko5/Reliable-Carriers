# How to Add Shipment Coordinate Columns to Database

## Problem
The SQL script might not execute due to MySQL version differences or syntax issues.

## Solutions (Choose One)

### Option 1: Let the Application Handle It (Recommended)
The Java migration component (`ShipmentCoordinatesMigration`) runs automatically when the application starts. Just start your application and it will add the columns.

**Check the console output** - you should see:
```
=== Shipment Coordinates Migration ===
Checking shipments table for coordinate columns...
Adding coordinate columns to shipments table...
✅ Shipment coordinate columns migration completed successfully!
```

### Option 2: Run Simple SQL Script Manually

1. **Connect to your MySQL database:**
   ```bash
   mysql -u root -p reliable_carriers
   ```

2. **Run the simple SQL script:**
   ```bash
   mysql -u root -p reliable_carriers < add-shipment-coordinates-simple.sql
   ```
   
   Or copy-paste these commands one at a time:
   ```sql
   USE reliable_carriers;
   
   ALTER TABLE shipments ADD COLUMN pickup_latitude DECIMAL(10, 8) NULL;
   ALTER TABLE shipments ADD COLUMN pickup_longitude DECIMAL(11, 8) NULL;
   ALTER TABLE shipments ADD COLUMN delivery_latitude DECIMAL(10, 8) NULL;
   ALTER TABLE shipments ADD COLUMN delivery_longitude DECIMAL(11, 8) NULL;
   ```

3. **If you get "Duplicate column" errors**, that means the columns already exist - that's fine!

### Option 3: Use Hibernate Auto-Update (Development Only)

If `spring.jpa.hibernate.ddl-auto=update` is set in `application.properties`, Hibernate will automatically create the columns when you start the application.

**⚠️ Warning:** Only use `ddl-auto=update` in development. Use `validate` or `none` in production.

### Option 4: Check What's Wrong

1. **Check if columns already exist:**
   ```sql
   DESCRIBE shipments;
   ```
   Look for `pickup_latitude`, `pickup_longitude`, `delivery_latitude`, `delivery_longitude`

2. **Check MySQL version:**
   ```sql
   SELECT VERSION();
   ```
   Some MySQL versions don't support certain syntax.

3. **Check application logs:**
   Look for migration errors in the console when starting the application.

## Verification

After running any method, verify the columns exist:
```sql
DESCRIBE shipments;
```

You should see these columns:
- `pickup_latitude` DECIMAL(10,8)
- `pickup_longitude` DECIMAL(11,8)
- `delivery_latitude` DECIMAL(10,8)
- `delivery_longitude` DECIMAL(11,8)

## Troubleshooting

### Error: "Duplicate column name"
✅ **This is OK!** The columns already exist. You can skip the migration.

### Error: "Table doesn't exist"
❌ Make sure you're using the correct database:
```sql
USE reliable_carriers;
SHOW TABLES;
```

### Error: "Access denied"
❌ Check your database user permissions:
```sql
SHOW GRANTS;
```

### Error: "Syntax error"
❌ Try the simple SQL script (`add-shipment-coordinates-simple.sql`) instead.

## Quick Fix Command

If you just want to add the columns quickly, run this in MySQL:

```sql
USE reliable_carriers;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS pickup_latitude DECIMAL(10, 8) NULL;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS pickup_longitude DECIMAL(11, 8) NULL;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS delivery_latitude DECIMAL(10, 8) NULL;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS delivery_longitude DECIMAL(11, 8) NULL;
```

**Note:** If your MySQL version doesn't support `IF NOT EXISTS`, run without it and ignore "duplicate column" errors.

