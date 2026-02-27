# Aclaraciones API
Backend RESTAPI service for Aclaraciones (AKA Help Center).

## Usage
### Installing:
```
mvn package
```

### Configuring:
Declare the following variables:
```
DATASOURCE_URL=jdbc:postgresql://localhost:5432/aclaraciones
DATASOURCE_USERNAME=root
DATASOURCE_PASSWORD=root

KEYCLOAK_REALM_CERT=MIIClzCCAX8CBgF...KYKBj1iX92F87k01ytA==
KEYCLOAK_GROUP_OPERADOR=/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-admin
KEYCLOAK_GROUP_PROVEEDOR=/Omnichannel-Retail/Merchandise/PPSOMX/ppsomx-proveedores

CUSTOM_AUTH_HEADER=Authorization
```

Where:
- `DATASOURCE_URL` is the JDBC database URL.
- `DATASOURCE_USERNAME` is the databse username.
- `DATASOURCE_PASSWORD` is the database password.
- `KEYCLOAK_REALM_CERT`  is the public RSA X509 KeyCloack certificate in PEM or DER format (base64 string).
- `KEYCLOAK_GROUP_OPERADOR` is the full group name of KeyCloak' admin user.
- `KEYCLOAK_GROUP_PROVEEDOR` is the full group name of KeyCloak' external (AKA proveedor) user.
- `CUSTOM_AUTH_HEADER` is a custom string for replacing the RFC2616 section 14.8 Authentication header.

### Execution:
Once built, you can execute the artifact as a standard fatjar, as follows:
```
java -jar ./build/libs/*.jar
```
