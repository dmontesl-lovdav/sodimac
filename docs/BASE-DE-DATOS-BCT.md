# Conexiones BD - Proyecto BCT Facturacion

Credenciales del proyecto `finanzas_bctfacturacion` (soporte/bctfacturacion).
Las credenciales activas en el properties estan encriptadas. Aqui se documentan en texto plano para referencia.

---

## Oracle BCT (Transacciones de tienda)

### Test
| Parametro | Valor |
|-----------|-------|
| **Host** | `f8cloud1129.falabella.cl` |
| **Puerto** | `1541` |
| **SID** | `arsmxts` |
| **Usuario** | `USW_BCT` |
| **Password** | `ubct392sK7` |
| **Driver** | `oracle.jdbc.driver.OracleDriver` |
| **JDBC URL** | `jdbc:oracle:thin:@f8cloud1129.falabella.cl:1541/arsmxts` |

### Produccion
| Parametro | Valor |
|-----------|-------|
| **Host** | `ramsay.falabella.cl` |
| **Puerto** | `1531` |
| **SID** | `arsmxpr` |
| **Usuario** | `BATSW_FAC` |
| **Password** | (encriptado en properties) |
| **JDBC URL** | `jdbc:oracle:thin:@ramsay.falabella.cl:1531/arsmxpr` |

### Tablas relevantes (esquema SW_CEM)
| Tabla | Descripcion |
|-------|-------------|
| `SW_CEM.TRX_POINTS` | Movimientos de puntos CES |
| `SW_CEM.TRX_POINTS_SKU` | Distribucion de puntos por SKU |
| `SW_CEM.TRX_POINTS_EXT` | Factor de conversion puntos-pesos |
| `SW_CEM.TRX_POINTS_BKP` | Backup/cancelaciones de puntos (pendiente analisis) |
| `TRX_HDR` | Cabecera de transacciones (tickets) |
| `TRX_DET` | Detalle de transacciones (SKU, descripcion) |
| `TRX_DET_IMPUESTO` | Impuestos por linea de detalle |
| `TRX_FRM_PAGO` | Formas de pago del ticket |

---

## SQL Server - Fiscal (Rebates / Fiscal)

### Desarrollo
| Parametro | Valor |
|-----------|-------|
| **Host** | `10.138.153.10` |
| **Puerto** | `5319` |
| **Base de datos** | `SODIMAC_REBATES_DEV` |
| **Usuario** | `SodimacDevUsr` |
| **Password** | (encriptado en properties) |
| **Driver** | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |

### Produccion
| Parametro | Valor |
|-----------|-------|
| **Host** | `10.138.150.124` |
| **Puerto** | `5319` |
| **Base de datos** | `SODIMAC_FISCAL_PROD` |
| **Usuario** | `UserBatchFinanzas` |
| **Password** | (encriptado en properties) |

---

## SQL Server - CES (Puntos CES replicados)

### Desarrollo
| Parametro | Valor |
|-----------|-------|
| **Host** | `10.138.153.10` |
| **Puerto** | (default 1433) |
| **Base de datos** | `SODIMAC_SAP_DEV` |
| **Usuario** | `SodimacDevUsr` |
| **Password** | (encriptado en properties) |
| **Driver** | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |

### Produccion
| Parametro | Valor |
|-----------|-------|
| **Host** | `10.138.150.124` |
| **Puerto** | `5319` |
| **Base de datos** | `SODIMAC_SAP_PROD` |
| **Usuario** | `SodimacEtlUsr` |
| **Password** | (encriptado en properties) |

### Tablas (escritura desde el batch)
| Tabla | Descripcion |
|-------|-------------|
| `AdminPuntosCes` | Registro de movimientos de puntos replicados |
| `VentaCab` | Cabecera de venta replicada |
| `VentaDetImpuesto` | Detalle de impuestos replicado |
| `ControVentaCes` | Control de procesamiento por fecha |

---

## MariaDB - Portal Facturacion (InHouse)

### Produccion
| Parametro | Valor |
|-----------|-------|
| **Host** | `10.138.150.77` |
| **Puerto** | `3306` |
| **Base de datos** | `facturacion` |
| **Usuario** | (encriptado en properties) |
| **Password** | (encriptado en properties) |
| **Driver** | `org.mariadb.jdbc.Driver` |

---

## Notas

- Las credenciales activas en `application.properties` estan encriptadas y apuntan a **produccion**
- Las de desarrollo estan comentadas con `#`
- Fuente: correo de Oscar Andres Romero Gonzalez (14-ago-2024)
