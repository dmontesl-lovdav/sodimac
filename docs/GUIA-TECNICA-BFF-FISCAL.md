# Guia Tecnica-Funcional: BFF Fiscal y Fiscal-API

## Indice

1. [Introduccion](#1-introduccion)
2. [Arquitectura General](#2-arquitectura-general)
3. [OpenAPI y GCP Cloud Endpoints](#3-openapi-y-gcp-cloud-endpoints)
4. [BFF Fiscal (Backend For Frontend)](#4-bff-fiscal-backend-for-frontend)
5. [Fiscal-API (Backend)](#5-fiscal-api-backend)
6. [Flujo Completo de una Peticion](#6-flujo-completo-de-una-peticion)
7. [Ejemplo Paso a Paso: Registro de Factura](#7-ejemplo-paso-a-paso-registro-de-factura)
8. [Endpoints Principales](#8-endpoints-principales)
9. [Base de Datos](#9-base-de-datos)
10. [Configuracion por Ambiente](#10-configuracion-por-ambiente)
11. [Desarrollo Local](#11-desarrollo-local)

---

## 1. Introduccion

El sistema fiscal esta compuesto por dos componentes principales:

| Componente | Tecnologia | Puerto | Funcion |
|------------|------------|--------|---------|
| **BFF Fiscal** | Node.js/Express | 3000 (dev) / 8080 (k8s) | Proxy transparente + validacion JWT |
| **Fiscal-API** | Java 17/Spring Boot 3.4 | 8082 | Logica de negocio + persistencia |

### Que es un BFF?

**BFF (Backend For Frontend)** es un patron arquitectonico donde se crea un backend especifico para cada tipo de cliente (web, mobile, etc.). En nuestro caso:

- El BFF actua como **proxy transparente** entre el frontend y el backend
- **No contiene logica de negocio**, solo redirige peticiones
- Valida tokens JWT de KeyCloak (opcional, configurable)
- Maneja headers, timeouts y payloads grandes (hasta 66MB para XMLs)

---

## 2. Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FLUJO DE PETICIONES                             │
└─────────────────────────────────────────────────────────────────────────────┘

                                ┌──────────────────┐
                                │  GCP CLOUD       │
                                │  ENDPOINTS       │
                                │  (openapi.yaml)  │
                                │  - Validacion    │
                                │  - Rate limiting │
                                │  - Metricas      │
                                └────────┬─────────┘
                                         │ Configura
                                         ▼
┌──────────────┐     HTTPS      ┌──────────────────┐     HTTP      ┌──────────────┐
│   FRONTEND   │ ──────────────>│   NGINX INGRESS  │ ─────────────>│  BFF FISCAL  │
│   (Angular)  │                │   (Kubernetes)   │               │  (Node.js)   │
└──────────────┘                └──────────────────┘               └──────┬───────┘
                                                                          │
                                        URL Rewrite:                      │ HTTP
                                        /ppsomx/fiscal/* → /*             │
                                                                          ▼
                                                                   ┌──────────────┐
                                                                   │  FISCAL-API  │
                                                                   │ (Spring Boot)│
                                                                   └──────┬───────┘
                                                                          │
                                                                          │ JDBC
                                                                          ▼
                                                                   ┌──────────────┐
                                                                   │  PostgreSQL  │
                                                                   │tenant_fiscal │
                                                                   └──────────────┘
```

### Ejemplo de URL

```
CLIENTE SOLICITA:
https://dev.vendor.fbusinesscenter.com/ppsomx/fiscal/invoices/search

NGINX INGRESS reescribe a:
http://bff-fiscal-service:8080/invoices/search

BFF redirige a:
http://fiscal-api-service:8082/invoices/search

FISCAL-API procesa y responde
```

---

## 3. OpenAPI y GCP Cloud Endpoints

### 3.1 Que es OpenAPI?

**OpenAPI** (antes conocido como Swagger) es una especificacion estandar para describir APIs REST. En nuestro proyecto, el archivo `openapi.yaml` cumple dos funciones criticas:

1. **Documentacion**: Define todos los endpoints, parametros, respuestas y modelos de datos
2. **Configuracion de GCP Cloud Endpoints**: Se despliega en Google Cloud para habilitar el API Gateway

### 3.2 Ubicacion del Archivo

```
C:\workspace-fbc\backend\mrch.bff.somx.ppsomx.fiscal\cloud-endpoint\openapi.yaml
```

### 3.3 Estructura del openapi.yaml

```yaml
swagger: "2.0"                    # Version de OpenAPI (Swagger 2.0 para GCP)
info:
  title: Fiscal API
  version: 2.0.0
  description: API BFF para gestion de comprobantes fiscales mexicanos (CFDI)

host: ${DOMAIN_OPENAPI}           # Variable de entorno (ej: dev.vendor.fbusinesscenter.com)
schemes:
  - https

paths:
  /health:                        # Definicion de cada endpoint
    get:
      summary: Health check
      responses:
        "200":
          description: Servicio funcionando

  /invoices/search:
    post:
      summary: Busqueda avanzada de facturas
      consumes:
        - application/json
      produces:
        - application/json
      parameters:
        - name: Authorization
          in: header
          required: true
          type: string
        - name: body
          in: body
          schema:
            $ref: '#/definitions/InvoiceSearchDto'
      responses:
        "200":
          schema:
            $ref: '#/definitions/PageInvoiceDto'

definitions:                      # Modelos de datos (DTOs)
  InvoiceSearchDto:
    type: object
    properties:
      rfcEmisor:
        type: string
      fechaInicio:
        type: string
        format: date

securityDefinitions:              # Configuracion de seguridad
  bearerAuth:
    type: apiKey
    in: header
    name: Authorization
  keycloak:                       # Configuracion JWT de KeyCloak
    type: oauth2
    x-google-issuer: ${KEYCLOAK}/auth/realms/corp
    x-google-jwks_uri: ${JWKS_URL}
    x-google-audiences: vendor-backend

x-google-endpoints:               # Configuracion especifica de GCP
  - allowCors: true
    name: ${DOMAIN_OPENAPI}
```

### 3.4 Despliegue en GCP Cloud Endpoints

Cuando se hace deploy del BFF, el archivo `openapi.yaml` se despliega en GCP Cloud Endpoints:

```bash
# Comando de deploy (ejecutado por CI/CD)
gcloud endpoints services deploy ./cloud-endpoint/openapi.yaml --project=$PROJECT_ID
```

**Que hace este deploy?**

1. **Registra el servicio** en GCP Cloud Endpoints
2. **Valida la especificacion** OpenAPI (errores de sintaxis, tipos no soportados)
3. **Configura el API Gateway** con las rutas definidas
4. **Habilita metricas** y logging en GCP Console

### 3.5 Funciones de GCP Cloud Endpoints

| Funcion | Descripcion |
|---------|-------------|
| **API Gateway** | Punto de entrada unico para todas las peticiones |
| **Validacion de JWT** | Verifica tokens contra KeyCloak usando JWKS |
| **Rate Limiting** | Limita peticiones por cliente/IP |
| **Metricas** | Latencia, errores, throughput en GCP Console |
| **Logging** | Registra todas las peticiones en Cloud Logging |
| **CORS** | Configuracion de Cross-Origin Resource Sharing |

### 3.6 Variables de Entorno en openapi.yaml

El archivo usa variables que se sustituyen durante el deploy:

| Variable | Desarrollo | Produccion |
|----------|------------|------------|
| `${DOMAIN_OPENAPI}` | dev.vendor.fbusinesscenter.com | vendor.fbusinesscenter.com |
| `${KEYCLOAK}` | https://keycloak-dev.example.com | https://keycloak.example.com |
| `${JWKS_URL}` | URL del certificado publico JWT | URL del certificado publico JWT |

### 3.7 Limitaciones de GCP Cloud Endpoints

GCP Cloud Endpoints tiene algunas restricciones que debemos respetar:

| Restriccion | Solucion |
|-------------|----------|
| No soporta `type: file` | Usar `type: string` con `format: binary` |
| Solo Swagger 2.0 | No usar OpenAPI 3.0 |
| Tamaño maximo de spec | Dividir en multiples archivos si es necesario |

**Ejemplo de correccion (type: file):**

```yaml
# INCORRECTO (causa error en deploy)
responses:
  "200":
    schema:
      type: file

# CORRECTO
responses:
  "200":
    schema:
      type: string
      format: binary
```

### 3.8 Flujo de Validacion JWT con Cloud Endpoints

```
┌──────────┐     ┌─────────────────┐     ┌──────────────┐     ┌─────────────┐
│ Cliente  │────>│ Cloud Endpoints │────>│   KeyCloak   │     │   BFF       │
│          │     │ (API Gateway)   │     │   (JWKS)     │     │   Fiscal    │
└──────────┘     └────────┬────────┘     └──────────────┘     └─────────────┘
                          │                      │
     1. Request con       │  2. Obtiene JWKS     │
        Bearer Token      │     (certificado)    │
                          │<─────────────────────│
                          │
                          │  3. Valida firma JWT
                          │
                          │  4. Si valido, pasa request al BFF
                          │─────────────────────────────────────>│
                          │
                          │  5. Si invalido, retorna 401
                          │<─────────────────────────────────────│
```

### 3.9 Sincronizacion OpenAPI ↔ Fiscal-API

**IMPORTANTE:** El archivo `openapi.yaml` debe estar sincronizado con los endpoints reales del `fiscal-api`. Si agregas un nuevo endpoint en Java, debes:

1. Agregar el endpoint en `fiscal-api` (Controller)
2. Agregar la definicion en `openapi.yaml`
3. Hacer deploy del BFF para actualizar Cloud Endpoints

```
fiscal-api (Java)                    openapi.yaml
─────────────────                    ────────────────
@PostMapping("/invoices/search") ──> /invoices/search:
                                       post:
                                         ...
```

---

## 4. BFF Fiscal (Backend For Frontend)

### 4.1 Ubicacion del Proyecto

```
C:\workspace-fbc\backend\mrch.bff.somx.ppsomx.fiscal\
```

### 4.2 Estructura del Proyecto

```
mrch.bff.somx.ppsomx.fiscal/
├── src/
│   └── App.js                    # Aplicacion principal (unico archivo de codigo)
├── cloud-endpoint/
│   └── openapi.yaml              # Especificacion OpenAPI para GCP Endpoints
├── kustomization/                # Configuracion Kubernetes
│   ├── base/                     # Templates base
│   ├── development/              # Config desarrollo
│   ├── uat/                      # Config UAT
│   └── production/               # Config produccion
├── package.json                  # Dependencias Node.js
├── Dockerfile                    # Imagen Docker
└── .env                          # Variables de entorno local
```

### 4.3 Codigo Principal (App.js)

El BFF es extremadamente simple - solo 80 lineas de codigo:

```javascript
// 1. CONFIGURACION
const remoteUrl = process.env.REMOTE_URL || 'http://localhost:8082';
const localPort = process.env.LOCAL_PORT || '3000';
const localContext = process.env.LOCAL_CONTEXT || '/';
const healthPath = process.env.HEALTH_PATH || '/health';

// 2. PROXY HTTP TRANSPARENTE
const remoteResolver = proxy(remoteUrl, {
  parseReqBody: false,  // No parsea body, lo pasa intacto

  // Reescribe path: quita el contexto local
  proxyReqPathResolver: (request) => {
    const targetPath = request.originalUrl.replace(localContext, "");
    return targetPath.startsWith("/") ? targetPath : "/" + targetPath;
  }
});

// 3. HEALTH CHECK (sin autenticacion)
localService.get('/health', (req, res) => {
  res.json({ message: "healthy" });
});

// 4. PROXY PARA TODO LO DEMAS
localService.use(localContext, remoteResolver);
```

### 4.4 Variables de Entorno

```bash
# .env (desarrollo local)
REMOTE_URL=http://localhost:8082    # URL del fiscal-api
LOCAL_PORT=3001                      # Puerto del BFF
LOCAL_CONTEXT=/                      # Contexto (raiz)
HEALTH_PATH=/health                  # Path de health check
AUTH_PUBLIC_KEY=                     # Certificado KeyCloak (vacio = sin validacion)
```

### 4.5 Funciones del BFF

| Funcion | Descripcion |
|---------|-------------|
| **Proxy HTTP** | Redirige todas las peticiones al fiscal-api |
| **Validacion JWT** | Valida tokens de KeyCloak (opcional, deshabilitado en dev) |
| **Health Check** | Endpoint `/health` sin autenticacion para Kubernetes |
| **Manejo de Payloads** | Soporta hasta 66MB para XMLs fiscales |

---

## 5. Fiscal-API (Backend)

### 5.1 Ubicacion del Proyecto

```
C:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\
```

### 5.2 Estructura del Proyecto

```
fiscal-api/
├── src/main/java/com/sodimac/fiscal/api/
│   ├── controller/           # 20 controladores REST
│   │   ├── InvoiceController.java
│   │   ├── FiscalXmlProcessorController.java
│   │   ├── PaymentController.java
│   │   └── ...
│   ├── service/              # 34 servicios
│   │   ├── impl/
│   │   │   ├── InvoiceServiceImpl.java
│   │   │   ├── FiscalXmlTransformerServiceImpl.java
│   │   │   └── ...
│   │   └── interfaces/
│   ├── repository/           # 20 repositorios JPA
│   ├── model/
│   │   ├── entity/           # 21 entidades JPA
│   │   ├── dto/              # DTOs de transferencia
│   │   └── enums/            # Enumeraciones
│   ├── mapper/               # MapStruct mappers
│   ├── config/               # Configuracion Spring
│   └── exception/            # Manejo de errores
├── src/main/resources/
│   ├── application.properties
│   ├── xsd/                  # Esquemas XSD del SAT
│   └── xsl/                  # Transformaciones para PDF
└── pom.xml
```

### 5.3 Tecnologias

| Tecnologia | Version | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.4.2 | Framework web |
| Spring Data JPA | 3.4.x | ORM/Persistencia |
| PostgreSQL | 15+ | Base de datos |
| JAXB | 4.0 | Parsing XML (XSD → Java) |
| MapStruct | 1.5.5 | Mapping Entity ↔ DTO |
| Apache FOP | 2.9 | Generacion PDF |

### 5.4 Capas de la Aplicacion

```
┌─────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                        │
│  InvoiceController, PaymentController, etc.                  │
│  - Recibe peticiones HTTP                                    │
│  - Valida parametros de entrada                              │
│  - Delega al Service                                         │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                          │
│  InvoiceServiceImpl, FiscalXmlTransformerServiceImpl, etc.  │
│  - Contiene logica de negocio                                │
│  - Coordina operaciones                                      │
│  - Usa Mappers para convertir Entity ↔ DTO                   │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                        │
│  InvoiceRepository, PaymentRepository, etc.                  │
│  - Extiende JpaRepository                                    │
│  - Queries personalizadas con @Query                         │
│  - Acceso a PostgreSQL                                       │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATABASE                              │
│  PostgreSQL - Schema: tenant_fiscal                          │
│  Tablas: invoice, payments, issuer, receiver, addendum, etc.│
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Flujo Completo de una Peticion

### Diagrama de Secuencia

```
┌────────┐     ┌─────────┐     ┌─────────┐     ┌───────────┐     ┌────────────┐
│Frontend│     │  NGINX  │     │   BFF   │     │Fiscal-API │     │ PostgreSQL │
└───┬────┘     └────┬────┘     └────┬────┘     └─────┬─────┘     └──────┬─────┘
    │               │               │                │                  │
    │ POST /ppsomx/fiscal/invoices/search            │                  │
    │──────────────>│               │                │                  │
    │               │               │                │                  │
    │               │ Rewrite URL   │                │                  │
    │               │ POST /invoices/search          │                  │
    │               │──────────────>│                │                  │
    │               │               │                │                  │
    │               │               │ Valida JWT     │                  │
    │               │               │ (opcional)     │                  │
    │               │               │                │                  │
    │               │               │ Proxy request  │                  │
    │               │               │ POST /invoices/search             │
    │               │               │───────────────>│                  │
    │               │               │                │                  │
    │               │               │                │ Controller       │
    │               │               │                │ recibe request   │
    │               │               │                │                  │
    │               │               │                │ Service          │
    │               │               │                │ procesa logica   │
    │               │               │                │                  │
    │               │               │                │ Repository       │
    │               │               │                │ SELECT * FROM... │
    │               │               │                │─────────────────>│
    │               │               │                │                  │
    │               │               │                │<─────────────────│
    │               │               │                │   ResultSet      │
    │               │               │                │                  │
    │               │               │<───────────────│                  │
    │               │               │  JSON Response │                  │
    │               │<──────────────│                │                  │
    │<──────────────│               │                │                  │
    │ 200 OK + JSON │               │                │                  │
```

---

## 7. Ejemplo Paso a Paso: Registro de Factura

Este ejemplo muestra el flujo completo del endpoint `POST /invoices/register` (STM-337).

### 7.1 Request del Cliente

```http
POST https://dev.vendor.fbusinesscenter.com/ppsomx/fiscal/invoices/register
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="xmlFile"; filename="factura.xml"
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8"?>
<cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4"
                  Version="4.0"
                  Serie="A"
                  Folio="12345"
                  Fecha="2024-01-15T10:30:00"
                  TipoDeComprobante="I"
                  Total="1160.00"
                  Moneda="MXN">
  <cfdi:Emisor Rfc="AAA010101AAA" Nombre="Empresa Emisora" RegimenFiscal="601"/>
  <cfdi:Receptor Rfc="BBB020202BBB" Nombre="Empresa Receptora" UsoCFDI="G03"/>
  <cfdi:Conceptos>
    <cfdi:Concepto ClaveProdServ="84111506" Cantidad="1" Descripcion="Servicio" ValorUnitario="1000.00" Importe="1000.00"/>
  </cfdi:Conceptos>
  <cfdi:Complemento>
    <tfd:TimbreFiscalDigital UUID="550e8400-e29b-41d4-a716-446655440000" .../>
  </cfdi:Complemento>
</cfdi:Comprobante>
--boundary--
```

### 7.2 Paso por NGINX Ingress

```yaml
# Configuracion del Ingress
spec:
  rules:
    - host: dev.vendor.fbusinesscenter.com
      http:
        paths:
          - path: /ppsomx/fiscal/(.*)
            pathType: Prefix
            backend:
              service:
                name: bff-fiscal-service
                port: 8080
```

**Transformacion:**
- URL entrada: `/ppsomx/fiscal/invoices/register`
- URL salida: `/invoices/register`

### 7.3 Paso por BFF Fiscal

```javascript
// App.js - El BFF recibe la peticion y la redirige
const remoteResolver = proxy('http://fiscal-api-service:8082', {
  parseReqBody: false,
  proxyReqPathResolver: (request) => {
    // /invoices/register → /invoices/register (sin cambios porque LOCAL_CONTEXT=/)
    return request.originalUrl;
  }
});
```

**Acciones del BFF:**
1. Recibe peticion en puerto 8080
2. Valida JWT (si AUTH_PUBLIC_KEY esta configurado)
3. Redirige a `http://fiscal-api-service:8082/invoices/register`
4. Retorna respuesta al cliente

### 7.4 Procesamiento en Fiscal-API

#### 7.4.1 Controller (InvoiceController.java)

```java
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/register")
    public ResponseEntity<InvoiceRegistrationResponse> registerInvoice(
            @RequestParam("xmlFile") MultipartFile xmlFile) {

        // Delega al servicio
        InvoiceRegistrationResponse response = invoiceService.registerInvoice(xmlFile);

        return ResponseEntity.ok(response);
    }
}
```

#### 7.4.2 Service (InvoiceServiceImpl.java)

```java
@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private IssuerRepository issuerRepository;

    @Autowired
    private XmlDocumentTypeDetector typeDetector;

    @Autowired
    private FiscalXmlTransformerService xmlTransformer;

    @Override
    @Transactional
    public InvoiceRegistrationResponse registerInvoice(MultipartFile xmlFile) {

        // 1. VALIDAR ARCHIVO
        if (xmlFile.isEmpty()) {
            throw new FiscalException("ERR001", "Archivo vacio");
        }

        // 2. LEER CONTENIDO XML
        String xmlContent = new String(xmlFile.getBytes(), StandardCharsets.UTF_8);

        // 3. DETECTAR TIPO DE DOCUMENTO
        TipoDocumentoFiscal tipo = typeDetector.detectType(xmlContent);
        // Resultado: FACTURA, NOTA_CREDITO, COMPLEMENTO_PAGO, etc.

        // 4. PARSEAR XML A OBJETOS JAVA (JAXB)
        FiscalXmlResponse parsedXml = xmlTransformer.transform(xmlContent);

        // 5. VALIDAR RFC RECEPTOR AUTORIZADO
        String rfcReceptor = parsedXml.getReceptor().getRfc();
        if (!authorizedReceiverRepository.existsByRfc(rfcReceptor)) {
            throw new FiscalException("BUS2001", "RFC receptor no autorizado");
        }

        // 6. BUSCAR O CREAR EMISOR
        IssuerEntity issuer = issuerRepository.findByRfc(parsedXml.getEmisor().getRfc())
            .orElseGet(() -> {
                IssuerEntity newIssuer = new IssuerEntity();
                newIssuer.setRfc(parsedXml.getEmisor().getRfc());
                newIssuer.setName(parsedXml.getEmisor().getNombre());
                newIssuer.setTaxRegime(parsedXml.getEmisor().getRegimenFiscal());
                return issuerRepository.save(newIssuer);
            });

        // 7. BUSCAR O CREAR RECEPTOR
        ReceiverEntity receiver = receiverRepository.findByRfc(rfcReceptor)
            .orElseGet(() -> {
                ReceiverEntity newReceiver = new ReceiverEntity();
                newReceiver.setRfc(rfcReceptor);
                newReceiver.setName(parsedXml.getReceptor().getNombre());
                return receiverRepository.save(newReceiver);
            });

        // 8. CREAR ENTIDAD INVOICE
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setFiscalUuid(parsedXml.getTimbreFiscalDigital().getUuid());
        invoice.setSeries(parsedXml.getComprobante().getSerie());
        invoice.setFolio(parsedXml.getComprobante().getFolio());
        invoice.setIssueDate(parsedXml.getComprobante().getFecha());
        invoice.setTotal(parsedXml.getComprobante().getTotal());
        invoice.setSubtotal(parsedXml.getComprobante().getSubTotal());
        invoice.setCurrency(parsedXml.getComprobante().getMoneda());
        invoice.setDocumentType(tipo == TipoDocumentoFiscal.FACTURA ? "I" : "E");
        invoice.setXmlContent(xmlContent);  // Guarda XML completo
        invoice.setIssuer(issuer);
        invoice.setReceiver(receiver);
        invoice.setStatus(1);  // Activo

        // 9. PERSISTIR EN BASE DE DATOS
        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        // 10. RETORNAR RESPUESTA
        return InvoiceRegistrationResponse.builder()
            .code("BUS1001")
            .message("Factura registrada exitosamente")
            .uuid(savedInvoice.getInvoiceUuid().toString())
            .build();
    }
}
```

#### 7.4.3 Repository (InvoiceRepository.java)

```java
@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    Optional<InvoiceEntity> findByFiscalUuid(String fiscalUuid);

    @Query("SELECT i FROM InvoiceEntity i WHERE i.issuer.rfc = :rfc")
    List<InvoiceEntity> findByIssuerRfc(@Param("rfc") String rfc);

    Page<InvoiceEntity> findByReceiverRfc(String rfc, Pageable pageable);
}
```

#### 7.4.4 Entity (InvoiceEntity.java)

```java
@Entity
@Table(name = "invoice", schema = "tenant_fiscal")
@EntityListeners(AuditingEntityListener.class)
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "invoice_uuid")
    private UUID invoiceUuid;

    @Column(name = "fiscal_uuid")
    private String fiscalUuid;  // UUID del SAT (TimbreFiscalDigital)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_uuid")
    private IssuerEntity issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid")
    private ReceiverEntity receiver;

    @Column(name = "series")
    private String series;

    @Column(name = "folio")
    private String folio;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "total", precision = 18, scale = 6)
    private BigDecimal total;

    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;

    @Column(name = "document_type")
    private String documentType;  // I=Factura, E=Nota Credito

    @Column(name = "status")
    private Integer status;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;
}
```

### 7.5 Response al Cliente

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
    "code": "BUS1001",
    "message": "Factura registrada exitosamente",
    "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "data": {
        "fiscalUuid": "550e8400-e29b-41d4-a716-446655440000",
        "series": "A",
        "folio": "12345",
        "issuerRfc": "AAA010101AAA",
        "receiverRfc": "BBB020202BBB",
        "total": 1160.00,
        "issueDate": "2024-01-15T10:30:00"
    }
}
```

---

## 8. Endpoints Principales

### 8.1 Procesamiento XML

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/fiscal/xml/process` | Procesar XML como string |
| POST | `/fiscal/xml/process/file` | Procesar archivo XML |
| POST | `/fiscal/xml/detect` | Detectar tipo de documento |
| POST | `/fiscal/xml/validate` | Validar documento fiscal |

### 8.2 Facturas (Invoices)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/invoices` | Listar facturas paginadas |
| POST | `/invoices/register` | Registrar factura/NC (STM-337) |
| PUT | `/invoices` | Actualizar factura (STM-339) |
| POST | `/invoices/search` | Busqueda avanzada (STM-338) |
| POST | `/invoices/download/xml` | Descarga masiva XML (STM-396) |
| POST | `/invoices/download/pdf` | Descarga masiva PDF (STM-396) |
| POST | `/invoices/export/csv` | Exportar a CSV (STM-396) |

### 8.3 Pagos

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/payments` | Listar pagos paginados |
| GET | `/payments/{uuid}` | Obtener pago por UUID |
| POST | `/fiscal/complementos-pago/registrar` | Registrar complemento de pago |

### 8.4 Catalogos

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/authorized-receivers` | Receptores autorizados |
| GET | `/issuers` | Emisores registrados |
| GET | `/receivers` | Receptores registrados |
| GET | `/pac-catalog` | PACs disponibles |
| GET | `/version-catalog` | Versiones CFDI soportadas |

### 8.5 Health Check

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/health` | Estado del servicio |

---

## 9. Base de Datos

### 9.1 Schema

```
Base de datos: b2b_portal (desarrollo) / userapp (produccion)
Schema: tenant_fiscal
```

### 9.2 Tablas Principales

```sql
-- FACTURAS Y NOTAS DE CREDITO
CREATE TABLE tenant_fiscal.invoice (
    invoice_uuid    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiscal_uuid     VARCHAR(36),      -- UUID del SAT (TimbreFiscalDigital)
    issuer_uuid     UUID REFERENCES issuer(issuer_uuid),
    receiver_uuid   UUID REFERENCES receiver(receiver_uuid),
    series          VARCHAR(25),
    folio           VARCHAR(40),
    version         VARCHAR(10),
    issue_date      TIMESTAMP,
    subtotal        DECIMAL(18,6),
    discount        DECIMAL(18,6),
    total           DECIMAL(18,6),
    currency        VARCHAR(3),
    exchange_rate   DECIMAL(18,6),
    payment_method  VARCHAR(3),
    payment_form    VARCHAR(2),
    document_type   VARCHAR(2),       -- I=Factura, E=Nota Credito
    xml_content     TEXT,             -- XML completo
    status          INTEGER,
    created_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(100)
);

-- EMISORES
CREATE TABLE tenant_fiscal.issuer (
    issuer_uuid     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rfc             VARCHAR(13) NOT NULL,
    name            VARCHAR(254),
    tax_regime      VARCHAR(3),
    created_at      TIMESTAMP,
    created_by      VARCHAR(100)
);

-- RECEPTORES
CREATE TABLE tenant_fiscal.receiver (
    receiver_uuid   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rfc             VARCHAR(13) NOT NULL,
    name            VARCHAR(254),
    tax_regime      VARCHAR(3),
    created_at      TIMESTAMP,
    created_by      VARCHAR(100)
);

-- COMPLEMENTOS DE PAGO
CREATE TABLE tenant_fiscal.payments (
    payments_uuid   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiscal_uuid     VARCHAR(36),
    issuer_uuid     UUID REFERENCES issuer(issuer_uuid),
    receiver_uuid   UUID REFERENCES receiver(receiver_uuid),
    version         VARCHAR(10),
    payment_date    TIMESTAMP,
    series          VARCHAR(25),
    folio           VARCHAR(40),
    xml_content     TEXT,
    status          INTEGER,
    created_at      TIMESTAMP,
    created_by      VARCHAR(100)
);

-- ADDENDAS
CREATE TABLE tenant_fiscal.addendum (
    addendum_uuid           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_uuid            UUID REFERENCES invoice(invoice_uuid),
    payments_uuid           UUID REFERENCES payments(payments_uuid),
    supplier_number         VARCHAR(50),
    reception_number        VARCHAR(50),
    purchase_order_number   VARCHAR(50),
    shipping_guide_number   VARCHAR(50),
    addendum_content        TEXT,
    supplier_type           VARCHAR(50),
    addenda_type            VARCHAR(50),
    created_at              TIMESTAMP,
    created_by              VARCHAR(100)
);

-- RECEPTORES AUTORIZADOS (CATALOGO)
CREATE TABLE tenant_fiscal.authorized_receiver_catalog (
    id              BIGSERIAL PRIMARY KEY,
    rfc             VARCHAR(13) NOT NULL,
    business_name   VARCHAR(254),
    status          INTEGER DEFAULT 1,
    created_at      TIMESTAMP,
    created_by      VARCHAR(100)
);
```

---

## 10. Configuracion por Ambiente

### 10.1 Desarrollo Local

**BFF (.env):**
```bash
REMOTE_URL=http://localhost:8082
LOCAL_PORT=3001
LOCAL_CONTEXT=/
HEALTH_PATH=/health
AUTH_PUBLIC_KEY=
```

**Fiscal-API (application-dev.properties):**
```properties
server.port=8082
spring.datasource.url=jdbc:postgresql://localhost:5434/b2b_portal?currentSchema=tenant_fiscal
spring.datasource.username=postgres
spring.datasource.password=postgres
security.enabled=false
```

### 10.2 Desarrollo (GCP)

**URL Base:** `https://dev.vendor.fbusinesscenter.com/ppsomx/fiscal/`

**Variables:**
```bash
REMOTE_URL=http://fiscal-api-service:8082
LOCAL_PORT=8080
AUTH_PUBLIC_KEY=<certificado-keycloak-dev-base64>
```

### 10.3 UAT

**URL Base:** `https://uat.vendor.fbusinesscenter.com/ppsomx/fiscal/`

### 10.4 Produccion

**URL Base:** `https://vendor.fbusinesscenter.com/ppsomx/fiscal/`

---

## 11. Desarrollo Local

### 11.1 Requisitos

- Java 17
- Node.js 18+
- PostgreSQL 15+ (o Docker)
- Maven 3.9+

### 11.2 Levantar Base de Datos (Docker)

```bash
docker run -d \
  --name postgres-fiscal \
  -e POSTGRES_DB=b2b_portal \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5434:5432 \
  postgres:15
```

### 11.3 Levantar Fiscal-API

```bash
cd C:\workspace-fbc\backend\mrch.backend.somx.fiscal-api

# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

El servicio estara disponible en `http://localhost:8082`

### 11.4 Levantar BFF

```bash
cd C:\workspace-fbc\backend\mrch.bff.somx.ppsomx.fiscal

# Instalar dependencias
npm install

# Ejecutar
npm start
```

El BFF estara disponible en `http://localhost:3001`

### 11.5 Probar Endpoints

```bash
# Health check directo al fiscal-api
curl http://localhost:8082/health

# Health check a traves del BFF
curl http://localhost:3001/health

# Listar facturas (a traves del BFF)
curl http://localhost:3001/invoices

# Buscar facturas
curl -X POST http://localhost:3001/invoices/search \
  -H "Content-Type: application/json" \
  -d '{"rfcEmisor": "AAA010101AAA", "page": 0, "size": 10}'
```

---

## Resumen

| Aspecto | BFF Fiscal | Fiscal-API |
|---------|------------|------------|
| **Tecnologia** | Node.js/Express | Java 17/Spring Boot |
| **Puerto** | 3001 (dev) / 8080 (k8s) | 8082 |
| **Funcion** | Proxy + JWT | Logica de negocio |
| **Base de datos** | No usa | PostgreSQL |
| **Codigo** | ~80 lineas | ~50,000 lineas |
| **Complejidad** | Minima | Alta |

El BFF es simplemente un **paso intermedio** que redirige peticiones. Toda la logica de negocio, validaciones, procesamiento XML y persistencia ocurre en el **Fiscal-API**.
