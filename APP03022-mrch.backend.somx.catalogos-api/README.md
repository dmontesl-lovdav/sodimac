# Catalog Manager (AKA CatMan) API
Backend RESTAPI service for catalogs management.

## Usage
### Installing:
```
mvn package
```

### Configuring:
Declare the following variables:
```
DATASOURCE_URL=jdbc:postgresql://localhost:5432/catman
DATASOURCE_USERNAME=root
DATASOURCE_PASSWORD=root
DATASOURCE_DRIVER=org.postgresql.Driver
```

Where:
- `DATASOURCE_URL` is the JDBC database URL.
- `DATASOURCE_USERNAME` is the databse username.
- `DATASOURCE_PASSWORD` is the database password.
- `DATASOURCE_DRIVER` is the cannonical name of the JDBC compliant class driver. `org.apache.derby.jdbc.EmbeddedDriver` and `org.postgresql.Driver` are included.

### Execution:
Once built, you can execute the artifact as a standard fatjar, as follows:
```
java -jar ./target/*.jar
```
