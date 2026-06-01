# Rebate Agreements Sync

Batch job for synchronizing commercial rebate agreements from external FBC service to local database.

## Overview

This application is a scheduled batch job that downloads commercial agreements (rebates) published in the FBC web service and stores them in the local Rebates database for Mexico, maintaining synchronized information and making it available for applying commercial discounts.

## Architecture

The project follows Hexagonal Architecture (Ports and Adapters) with the following layers:

- **Domain Layer**: Core business logic, models, and interfaces (ports)
- **Application Layer**: Use cases, DTOs, and application-level business rules
- **Infrastructure Layer**: Adapters for external services, persistence, and web controllers

## Key Features

- **Full Sync Strategy**: Deletes existing data and loads all commercial agreements from the external service
- **Scheduled Execution**: Runs daily at 03:00 AM (Mexico City timezone, GMT-6)
- **Retry Mechanism**: 2 retry attempts every 30 minutes in case of failure
- **Pagination Support**: Handles paginated responses from external API
- **Control Metrics**: Records sync statistics in control tables (CtrlProceCab, CtrlProceDet, ctrlLog)
- **Logging & Auditing**: Comprehensive logging with timestamp, severity, and phase details
- **Error Handling**: Robust exception handling with notifications
- **Configurable Parameters**: Contract ID, page size, and timeouts are configurable

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- SQL Server
- Resilience4j (for retry mechanism)
- MapStruct (for object mapping)
- Lombok
- JUnit 5 & Mockito (for testing)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- SQL Server database access
- Network access to external Rebate Management API

## Configuration

### Database Configuration

The application connects to two databases:

1. **SODIMAC_SAP_DEV** - Main database for storing rebate agreements
   - IP: 10.138.153.10
   - Port: 1433
   - User: SodimacETLUSR

2. **SODIMAC_BATCH_DEV** - Control database for storing execution logs and metrics
   - IP: 10.138.153.10
   - Port: 1433
   - User: SodimacETLUSR

### External API Configuration

- **Contracts Endpoint**: `https://rebate-management-prd.eastus2.cloudapp.azure.com/rebate-management-cl-sod-api/v1/contracts/MX`
- **Agreements Endpoint**: `https://rebate-management-prd.eastus2.cloudapp.azure.com/rebate-management-cl-sod-api/v1/contracts/MX/agreements`

### Environment Variables

For production, set the following environment variables:

```bash
export DB_PASSWORD=your_database_password
export BATCH_DB_PASSWORD=your_batch_database_password
```

## Building the Application

```bash
./mvnw clean package
```

## Running the Application

### Development Mode

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Mode

```bash
java -jar target/rebate-agreements-sync-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## API Endpoints

### Sync Operations

- `POST /api/sync/full` - Manually trigger full sync
- `POST /api/sync/contract/{contractId}` - Sync specific contract
- `GET /api/sync/last-result` - Get last sync result
- `GET /api/sync/summary` - Get sync summary statistics

### Rebate Agreements Management

- `GET /api/rebate-agreements` - Get all rebate agreements
- `GET /api/rebate-agreements/{id}` - Get rebate agreement by ID
- `GET /api/rebate-agreements/supplier/{supplierNumber}` - Get agreements by supplier
- `DELETE /api/rebate-agreements` - Delete all rebate agreements

## Acceptance Criteria

### CA1 - Successful Execution
- Connects to web service and database
- Runs at 03:00 AM
- Downloads all commercial agreements (complete pagination)
- Loads data into SODIMAC_SAP_DEV transactionally

### CA2 - Full Synchronization
- Strategy is full-sync
- Deletes previous data before loading
- Loads new information in a single transaction

### CA3 - Control Metrics
- Records total downloaded, total loaded, total per page
- Records differences if any

### CA4 - Logging & Auditing
- Logs info/warn/error events
- Records extraction, transformation, and load errors
- Includes timestamp, severity, detail, and phase

### CA5 - Retry on Failure
- Retries on temporal errors (5xx/timeout)
- Performs 2 retries every 30 minutes
- Marks job as failed after exhausting retries

### CA6 - Payload Validation
- Validates required fields in JSON response
- Handles missing mandatory fields gracefully
- Continues processing without interrupting total load

### CA7 - Idempotency
- No duplicates when origin doesn't change
- Consistent totals across consecutive executions

### CA8 - Security
- Credentials stored in vault, not in code
- Secure connections (TLS/HTTPS)

### CA9 - Configurability
- Parameters like contract_id, page_size, timeouts are configurable
- No code changes needed for parameter adjustments

### CA10 - Alerting
- Sends alerts to support/operations on final failure
- Includes summary and associated logs

## Database Schema

### RebateAcuerdosTemp Table

| Column | Type | Description |
|--------|------|-------------|
| NumeroProveedor | String | Supplier number (vendor tax ID) |
| RFC | String | Tax ID (set to null) |
| RazonSocial | String | Business name |
| Estado | String | Status |
| Familia | String | Product family |
| ClasificacionComercial | String | Commercial classification |
| NumeroAcuerdo | String | Agreement number |
| TipoAcuerdo | String | Agreement type |
| Moneda | String | Currency |
| Valor | Decimal | Value amount |
| TipoValor | String | Value type |
| FillRate | Decimal | Fill rate |
| ProgramaPago | String | Payment program |
| Marca | String | Brand |

## Testing

Run unit tests:

```bash
./mvnw test
```

## Monitoring

The application exposes actuator endpoints for monitoring:

- Health: `http://localhost:8081/actuator/health`
- Metrics: `http://localhost:8081/actuator/metrics`
- Info: `http://localhost:8081/actuator/info`

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify database credentials
   - Check network connectivity
   - Ensure SQL Server is accessible

2. **External API Timeout**
   - Check network connectivity to Azure service
   - Verify API endpoints are correct
   - Increase timeout if needed in configuration

3. **Sync Fails After Retries**
   - Check application logs for detailed error messages
   - Verify external service availability
   - Check database space and connectivity

## License

Copyright © 2024 Sodimac. All rights reserved.

## Contact

For support or questions, contact the development team or operations team.
