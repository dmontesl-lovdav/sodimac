# Soporte - Eli

## 2026-03-09 - Parcialidad en complementos de pago (CFDI)

**Contexto:** Al asignar un complemento de pago a un folio de factura con 7 facturas, el sistema genera parcialidad 8 en lugar de 2.

**Proyecto:** `soporte/cfdi` - `ComplementoPagoServiceImpl.merge()`

---

### Confirmación lógica parcialidad en complementos de pago

Eli, te comparto cómo funciona actualmente el cálculo de parcialidad y la propuesta de corrección:

**Comportamiento actual:**
Cuando se asigna un complemento de pago a un folio de factura, el sistema recorre todos los registros de detalle previos (uno por cada factura del folio) e incrementa un contador por cada registro. Si el folio tiene 7 facturas y ya existe 1 complemento previo, hay 7 registros de detalle → el nuevo complemento queda con parcialidad 8.

**Comportamiento esperado (a confirmar):**
La parcialidad debe representar el **número de complemento de pago** asignado al folio de factura, independientemente de cuántas facturas tenga el folio. Es decir:
- 1er complemento asignado al folio → parcialidad **1** (para todas sus facturas)
- 2do complemento asignado al folio → parcialidad **2** (para todas sus facturas)
- 3er complemento → parcialidad **3**, etc.

**Ejemplo con folio de 7 facturas:**

| Complemento | Facturas del folio | Parcialidad actual | Parcialidad esperada |
|---|---|---|---|
| Complemento 1 | 7 facturas | 1,2,3,4,5,6,7 | 1 (para las 7) |
| Complemento 2 | 7 facturas | 8 | 2 (para las 7) |

**¿Es correcto este entendimiento?**

> **2026-03-10 - Eli confirmó que el planteamiento es correcto.** Parcialidad = número de complemento asignado al folio, no cantidad de registros de detalle.

### Análisis técnico

**Archivo:** `ComplementoPagoServiceImpl.java` → método `merge()` (línea ~490)

```java
// Escenario 2: cuando ya existen pagos previos
int parcialidad = 1;  // se inicializa UNA vez

for (FolioFacturaModel facturaTotal : listFoliosFactura) {
    // obtiene detalles previos de cada factura
    List<...> listFacturtasAsignadas = ...obtenerFacturasByIdFactura(...);

    if (listFacturtasAsignadas != null && listFacturtasAsignadas.size() > 0) {
        for (int i=0; i<listFacturtasAsignadas.size(); i++) {
            // BUG: incrementa parcialidad por cada REGISTRO de detalle
            pagoComplementoFolioFacturaDetalle.setParcialidad(parcialidad);
            parcialidad++;  // ← aquí está el problema
        }
    }
    // El nuevo pago recibe el valor acumulado de parcialidad
}
```

**Corrección propuesta:** La parcialidad debería calcularse como el número de complementos distintos asignados al folio + 1, no como la suma de todos los registros de detalle.
