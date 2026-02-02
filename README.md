# Real Estate Management System

This project is a desktop real estate management application built with JavaFX and PostgreSQL.  
It demonstrates database design, CRUD operations, foreign key handling, and complex SQL queries using joins.

The application provides a graphical user interface for managing people, properties, listings, preferences, deals, and ownership relations.

---

### CRUD Operations
- People
- Clients
- Agents
- Properties
- Listings
- Preferences
- Property Images
- Property Owners (many-to-many)
- Successful Deals

-> Note
> Delete operations are **protected by database constraints**.  
> If a record is referenced elsewhere, the UI displays a warning instead of crashing.

---

### Complex SQL Queries
The application includes a **Queries tab** with:
- **LEFT JOIN** queries (e.g. properties with optional owners, unsold properties)
- **INNER JOIN** queries (e.g. top agents by sales, clients by number of deals)
- Aggregations (`COUNT`, `SUM`, `AVG`)
- Parameterized queries (client budget matching)

---

## Database Setup

The project includes **two `.txt` files** in the root directory:

1. "postgresql DB create tables.txt" 
- Creates all tables
- Defines primary keys, foreign keys, and constraints
2. "postresql DB populate.txt"
- Inserts sample records for testing the application

### Steps to set up the database:
1. Create a PostgreSQL database 
2. Open your SQL tool (pgAdmin, psql, etc.)
3. Run `create_tables.txt`
4. Run `populate_tables.txt`

---

## Database Configuration

Update the database connection in: 
-> src/main/java/org/example/db/Db.java

Example:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/REALESTATEDB";
private static final String USER = "postgres";
private static final String PASS = "your_password";
```

## Running the Application
### Prerequisites
- Java 17+ (or compatible with your JavaFX version)
- Maven
- PostgreSQL

### Run with Maven(From the project root)
```bash
mvn javafx:run
```
