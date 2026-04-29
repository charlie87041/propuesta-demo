# POS Module - Diseno Arquitectonico Conceptual

> Documento de arquitectura para un modulo Point-of-Sale (POS) integrado al ecosistema
> Cookies Store. Basado en el analisis de Bagisto POS (Webkul), Square POS, y
> mejores practicas de la industria retail.

---

## 1. Vision General

### 1.1 Que es el POS

Un sistema que permite a un **operador de caja** (cajero) procesar ventas presenciales
en un punto fisico (tienda, evento, feria) asociado a un **Source** del inventario.
Cada Source que habilite POS se convierte en un punto de venta fisico con su propia
caja, operadores autorizados y stock local.

### 1.2 Relacion con el Sistema Existente

```
+------------------------------------------------------------------+
|                        application                                |
|                    (Composition Root)                              |
+------------------------------------------------------------------+
       |            |            |           |            |
  +--------+  +---------+  +--------+  +--------+  +----------+
  | common |  |  admin  |  |  cart  |  | order  |  | pos-     |
  |        |  | module  |  | module |  | module |  | module   |
  +--------+  +---------+  +--------+  +--------+  +----------+
       ^           ^                                     |
       |           |                                     |
       +-----------+-------------------------------------+
         Reutiliza entidades, repositorios y servicios
```

El `pos-module` es un **modulo Gradle independiente** que:
- Depende de `common` (entidades, repositorios, servicios compartidos)
- Tiene su propia capa de servicios POS-especificos
- Tiene sus propios controllers (REST API para frontend POS)
- Tiene sus propias entidades para conceptos exclusivos del POS
- Se integra al `application` como un modulo mas

### 1.3 Referencia: Bagisto POS vs Nuestra Implementacion

| Concepto Bagisto POS | Nuestra Adaptacion |
|---|---|
| POS como extension del store | POS como Gradle module dentro del monorepo |
| Canal de venta separado | Source con `posEnabled=true` |
| Outlet concept | Source = Outlet (ya existe) |
| Inventario por outlet | ProductSource (ya existe) |
| Cashier section | PosSession + PosDrawer entities |
| Hold Orders | PosHeldOrder entity |
| Offline mode | Service Worker + IndexedDB + sync queue |
| Customers / Offline Customers | Customer existente + Guest mode + POS-local cache |
| Products grid | API de catalogo filtrada por Source |
| Barcode scanning | Busqueda por SKU/barcode en el catalogo |

---

## 2. Configuracion del POS en Source (Admin)

### 2.1 Ampliacion de la Entidad Source

La entidad `Source` existente se enriquece con configuracion POS:

```
Source (existente)                PosSourceConfig (nueva)
+------------------+             +---------------------------+
| id               |  1 ----0..1 | id                        |
| code             |             | source_id (FK, unique)    |
| name             |             | pos_enabled               |
| description      |             | pos_terminal_name         |
| system_managed   |             | default_currency_code(FK) |
| active           |             | receipt_header            |
| created_at       |             | receipt_footer            |
| updated_at       |             | allow_guest_checkout      |
+------------------+             | auto_print_receipt        |
                                 | tax_inclusive_pricing      |
                                 | require_drawer_close      |
                                 | max_held_orders           |
                                 | offline_mode_enabled      |
                                 | created_at                |
                                 | updated_at                |
                                 +---------------------------+
```

**Decision de diseno**: Una entidad separada `PosSourceConfig` en vez de agregar
columnas a `Source`. Razon: el POS es una extension opcional; no todas las Sources
necesitan POS. Esto mantiene `Source` limpio y evita NULLs innecesarios.

### 2.2 Autorizacion de Operadores POS

```
PosOperator (nueva)
+---------------------------+
| id                        |
| source_id (FK)            |
| admin_user_id (FK)        |
| role: POS_CASHIER |       |
|       POS_SUPERVISOR |    |
|       POS_MANAGER         |
| pin_hash                  |
| active                    |
| assigned_by_admin_user_id |
| created_at                |
| updated_at                |
+---------------------------+
UK: (source_id, admin_user_id)
```

**Roles POS**:

| Rol | Permisos |
|-----|----------|
| `POS_CASHIER` | Crear ventas, buscar productos, asignar cliente, hold orders, cobrar |
| `POS_SUPERVISOR` | Todo de CASHIER + voids, devoluciones, descuentos manuales, apertura/cierre de caja |
| `POS_MANAGER` | Todo de SUPERVISOR + reportes, configuracion, gestion de operadores |

**Integracion con ABAC existente**: Los roles POS se mapean a `Abilities` dentro
del `Domain` correspondiente al Source. Cuando se asigna un `PosOperator`, se
generan automaticamente las `UserDomainAbility` necesarias.

### 2.3 Pantalla Admin de Configuracion POS

Ubicacion en admin-module: nueva seccion dentro de la gestion de Sources.

```
/admin/sources/{sourceCode}/pos-config       -> Ver/Editar config POS
/admin/sources/{sourceCode}/pos-operators    -> CRUD operadores POS
/admin/sources/{sourceCode}/pos-sessions     -> Historial de sesiones
/admin/sources/{sourceCode}/pos-reports      -> Reportes de ventas POS
```

---

## 3. Modelo de Dominio del POS

### 3.1 Entidades Nuevas (pos-module)

```
PosSourceConfig          PosOperator           PosSession
+------------------+     +----------------+    +----------------------+
| config POS por   |     | operadores     |    | sesion de caja       |
| source           |     | autorizados    |    | (apertura a cierre)  |
+------------------+     +----------------+    +----------------------+
                                                       |
                              +------------------------+----------+
                              |                        |          |
                         PosDrawer              PosHeldOrder   PosPayment
                    +------------------+    +---------------+  +----------------+
                    | control de caja  |    | ordenes en    |  | pagos POS      |
                    | (opening/closing)|    | espera        |  | (cash, card,   |
                    +------------------+    +---------------+  |  mixed)        |
                                                               +----------------+
```

### 3.2 PosSession (Sesion de Caja)

Representa un turno de trabajo de un operador en un POS.

```
PosSession
+------------------------------------+
| id                                 |
| source_id (FK)                     |
| operator_id (FK -> PosOperator)    |
| status: OPEN | CLOSED | SUSPENDED  |
| opened_at                          |
| closed_at                          |
| closed_by_admin_user_id            |
| opening_remarks                    |
| closing_remarks                    |
| created_at                         |
| updated_at                         |
+------------------------------------+
IDX: (source_id, status)
IDX: (operator_id, opened_at)
```

### 3.3 PosDrawer (Caja Registradora)

Controla el dinero fisico en la caja. Cada sesion tiene exactamente un drawer.

```
PosDrawer
+---------------------------------------+
| id                                    |
| session_id (FK -> PosSession, unique) |
| opening_amount_minor                  |
| expected_amount_minor (calculado)     |
| actual_closing_amount_minor           |
| difference_minor (actual - expected)  |
| cash_sales_minor (acumulado)          |
| card_sales_minor (acumulado)          |
| other_sales_minor (acumulado)         |
| cash_refunds_minor (acumulado)        |
| currency_code (FK)                    |
| status: OPEN | COUNTED | CLOSED      |
| created_at                            |
| updated_at                            |
+---------------------------------------+
```

**Flujo del Drawer** (inspirado en Bagisto POS - captura "Cash Drawer"):

```
ABRIR CAJA
  Operador ingresa monto de apertura
  -> PosDrawer.opening_amount_minor = monto
  -> PosDrawer.status = OPEN
  -> PosSession.status = OPEN

DURANTE EL DIA
  Cada venta suma a cash_sales_minor / card_sales_minor / other_sales_minor
  Cada devolucion suma a cash_refunds_minor
  expected_amount_minor = opening + cash_sales - cash_refunds

CERRAR CAJA
  Operador cuenta dinero fisico -> actual_closing_amount_minor
  Sistema calcula difference_minor = actual - expected
  Operador ingresa remarks (obligatorio)
  -> PosDrawer.status = CLOSED
  -> PosSession.status = CLOSED
```

### 3.4 PosDrawerMovement (Movimientos Manuales de Caja)

Para entradas/salidas de efectivo que no son ventas (ej: cambio, retiro parcial).

```
PosDrawerMovement
+------------------------------------+
| id                                 |
| drawer_id (FK -> PosDrawer)        |
| type: CASH_IN | CASH_OUT           |
| amount_minor                       |
| reason                             |
| performed_by_admin_user_id         |
| created_at                         |
+------------------------------------+
IDX: (drawer_id, created_at)
```

### 3.5 PosHeldOrder (Ordenes en Espera)

Cuando el cajero necesita atender a otro cliente sin perder la orden actual.

```
PosHeldOrder
+------------------------------------+
| id                                 |
| session_id (FK -> PosSession)      |
| source_id (FK)                     |
| customer_id (FK, nullable)         |
| reference_code (unique, e.g.       |
|   "HOLD-2026-04-28-001")           |
| cart_snapshot (JSONB)              |
|   -> items[], customer info,       |
|      discounts, notes              |
| note                               |
| status: HELD | RESUMED | EXPIRED   |
|         | CANCELLED                 |
| held_at                            |
| resumed_at                         |
| expires_at                         |
| created_at                         |
| updated_at                         |
+------------------------------------+
IDX: (session_id, status)
IDX: (source_id, status)
```

**Diseno**: El carrito se serializa como JSONB snapshot. Al resumir, se rehidrata
y se validan precios y stock actuales. Si algo cambio, se notifica al operador.

### 3.6 PosPayment (Pagos POS)

Extiende el concepto de pago para soportar pagos mixtos (efectivo + tarjeta).

```
PosPayment
+------------------------------------+
| id                                 |
| order_id (FK -> Order)             |
| session_id (FK -> PosSession)      |
| method: CASH | CARD | TRANSFER |   |
|         MOBILE_PAY | OTHER         |
| amount_minor                       |
| tendered_minor (para cash)         |
| change_minor (para cash)           |
| reference_code (para card/transfer)|
| currency_code (FK)                 |
| created_at                         |
+------------------------------------+
IDX: (order_id)
IDX: (session_id, created_at)
```

**Pagos mixtos**: Una orden POS puede tener multiples `PosPayment`. Ejemplo:
- Cliente paga $50 en efectivo + $30 con tarjeta = 2 registros PosPayment.

### 3.7 PosOfflineSyncQueue (Cola de Sincronizacion Offline)

```
PosOfflineSyncQueue
+------------------------------------+
| id                                 |
| source_id (FK)                     |
| operator_id (FK)                   |
| event_type: ORDER_CREATE |         |
|   CUSTOMER_CREATE | DRAWER_UPDATE  |
| payload (JSONB)                    |
| status: PENDING | SYNCED | FAILED  |
|         | CONFLICT                  |
| client_timestamp                   |
| synced_at                          |
| error_message                      |
| retry_count                        |
| created_at                         |
+------------------------------------+
IDX: (source_id, status, created_at)
```

---

## 4. Integracion con Entidades Existentes

### 4.1 Ampliacion de Order

La entidad `Order` ya soporta ventas POS sin modificarlos:

| Campo existente | Uso en POS |
|---|---|
| `guest = true` | Venta a guest (sin cliente registrado) |
| `customer` (nullable) | Cliente seleccionado en POS |
| `customer_email` | Email del cliente o null para guest |
| `status` | `PENDING` -> `PROCESSING` -> `DELIVERED` (inmediato para POS) |
| `shipping_total_minor = 0` | Ventas POS no tienen envio |

**Nuevo campo sugerido en Order** (o en `additional` JSONB):

```
order_channel: VARCHAR(20) -> 'WEB' | 'POS' | 'API'
pos_session_id: FK -> PosSession (nullable)
```

Alternativa conservadora: usar el campo `additional` (JSONB) ya existente en
`OrderItem` para anotar `{"channel": "POS", "pos_session_id": 42}`.

**Recomendacion**: Agregar `order_channel` como columna real en `Order` y
`pos_session_id` como FK nullable. Estos son campos estructurales que se
necesitaran en queries y reportes.

### 4.2 Ventas Guest en POS

Flujo basado en el analisis de Bagisto POS:

```
1. Cajero abre venta -> por defecto es GUEST
2. (Opcional) Cajero busca/selecciona cliente existente
3. (Opcional) Cajero crea cliente nuevo rapido (nombre + email)
4. Si no selecciono cliente -> Order.guest = true, customer = null
5. Si selecciono cliente -> Order.guest = false, customer = ref
```

Se necesita un "Walk-in Customer" o "Guest" como opcion por defecto. No se
requiere email para ventas POS guest (a diferencia del e-commerce web).

### 4.3 Integracion con Inventario (StockMovement)

Las ventas POS generan movimientos de stock automaticamente:

```
Venta POS completada
  -> StockMovementService.record(
       source = POS source,
       product = item vendido,
       type = SALE,
       quantityDelta = -cantidadVendida,
       referenceType = "POS_ORDER",
       referenceCode = order.incrementId
     )

Devolucion POS
  -> StockMovementService.record(
       type = RETURN,
       quantityDelta = +cantidadDevuelta,
       referenceType = "POS_RETURN",
       referenceCode = order.incrementId
     )
```

El `StockMovementService` existente ya soporta esto sin modificaciones.
Las alertas de stock (LOW_STOCK, OUT_OF_STOCK) se disparan automaticamente.

### 4.4 Integracion con ProductSource

El grid de productos del POS se alimenta de `ProductSource` filtrado por el
Source activo:

```sql
SELECT p.*, ps.stock_quantity, ps.status, pr.amount_minor
FROM products p
JOIN product_sources ps ON ps.product_id = p.id
JOIN prices pr ON pr.id = ps.price_id
WHERE ps.source_id = :posSourceId
  AND ps.status = 'ACTIVE'
  AND ps.stock_quantity > 0
ORDER BY p.name
```

---

## 5. API REST del POS Module

### 5.1 Estructura de Endpoints

Todos bajo el prefijo `/api/pos/sources/{sourceCode}/`.

#### Autenticacion y Sesion

```
POST   /api/pos/auth/login                  -> Login PIN o credenciales
POST   /api/pos/auth/logout                 -> Cerrar sesion POS

POST   /api/pos/sources/{code}/sessions              -> Abrir sesion
GET    /api/pos/sources/{code}/sessions/current       -> Sesion actual
POST   /api/pos/sources/{code}/sessions/current/close -> Cerrar sesion
```

#### Drawer (Caja)

```
POST   /api/pos/sources/{code}/drawer/open   -> Abrir caja (monto apertura)
GET    /api/pos/sources/{code}/drawer/status  -> Estado actual caja
POST   /api/pos/sources/{code}/drawer/close   -> Cerrar caja (conteo final)
POST   /api/pos/sources/{code}/drawer/cash-in  -> Entrada manual de efectivo
POST   /api/pos/sources/{code}/drawer/cash-out -> Retiro manual de efectivo
```

#### Catalogo POS

```
GET    /api/pos/sources/{code}/products              -> Grid de productos
GET    /api/pos/sources/{code}/products/search        -> Busqueda texto/barcode
GET    /api/pos/sources/{code}/products/{id}          -> Detalle producto
GET    /api/pos/sources/{code}/categories             -> Categorias con conteo
```

#### Ordenes POS

```
POST   /api/pos/sources/{code}/orders                 -> Crear orden POS
GET    /api/pos/sources/{code}/orders                 -> Historial ordenes POS
GET    /api/pos/sources/{code}/orders/{id}            -> Detalle orden
POST   /api/pos/sources/{code}/orders/{id}/return     -> Devolucion
POST   /api/pos/sources/{code}/orders/{id}/receipt    -> Generar recibo
```

#### Held Orders (Ordenes en Espera)

```
POST   /api/pos/sources/{code}/held-orders            -> Hold current cart
GET    /api/pos/sources/{code}/held-orders             -> Lista orders en espera
POST   /api/pos/sources/{code}/held-orders/{id}/resume -> Resumir orden
DELETE /api/pos/sources/{code}/held-orders/{id}        -> Cancelar held order
```

#### Clientes POS

```
GET    /api/pos/sources/{code}/customers/search       -> Buscar cliente
POST   /api/pos/sources/{code}/customers/quick-create -> Crear cliente rapido
```

#### Sincronizacion Offline

```
POST   /api/pos/sources/{code}/sync/push              -> Enviar cola offline
GET    /api/pos/sources/{code}/sync/status             -> Estado de sync
GET    /api/pos/sources/{code}/sync/catalog-snapshot   -> Snapshot catalogo para cache
```

#### Reportes

```
GET    /api/pos/sources/{code}/reports/today           -> Resumen del dia
GET    /api/pos/sources/{code}/reports/session/{id}    -> Resumen por sesion
GET    /api/pos/sources/{code}/reports/sales            -> Ventas con filtros
GET    /api/pos/sources/{code}/reports/products         -> Top productos
```

---

## 6. Modo Offline

### 6.1 Estrategia

El modo offline permite operar el POS sin conexion a internet. Esto es critico
en escenarios reales (fallas de red, eventos al aire libre, etc.).

### 6.2 Arquitectura Offline

```
+------------------+          +-------------------+
|  Frontend POS    |          |  Backend API      |
|  (Browser/PWA)   |          |  (Spring Boot)    |
+------------------+          +-------------------+
| Service Worker   |  <--->   | /api/pos/sync/*   |
| IndexedDB        |  HTTP    |                   |
| Offline Queue    |  when    |                   |
| Cached Catalog   |  online  |                   |
+------------------+          +-------------------+
```

### 6.3 Que se cachea localmente

| Dato | Estrategia de cache | Frecuencia de sync |
|------|---------------------|--------------------|
| Catalogo de productos | Snapshot completo al abrir sesion | Cada apertura + cada 15 min |
| Categorias | Snapshot completo | Cada apertura |
| Clientes frecuentes | Ultimos 100 clientes del source | Cada apertura + incremental |
| Precios | Incluidos en snapshot de catalogo | Con catalogo |
| Stock levels | Incluidos en snapshot | Optimista (decrementar local) |

### 6.4 Que se genera offline y se sincroniza despues

1. **Ordenes offline**: Se crean completas en IndexedDB con un `tempId` (UUID).
   Al reconectar, se envian al backend en orden cronologico.
2. **Clientes offline**: Creacion rapida de clientes que se sincronizan despues.
   Se marcan como `offlineCreated = true` hasta que el backend los confirme.
3. **Movimientos de drawer**: Cash-in/cash-out se encolan.

### 6.5 Resolucion de Conflictos

| Conflicto | Estrategia |
|-----------|------------|
| Producto eliminado entre cache y sync | Marcar item en orden como "producto no disponible", mantener la venta |
| Precio cambio entre cache y sync | Mantener precio de venta original (precio al momento de la venta) |
| Stock insuficiente al sincronizar | Permitir stock negativo temporalmente, generar alerta OVERCOMMIT |
| Cliente duplicado (offline create) | Merge por email, mantener el registro del servidor |

### 6.6 Indicador de Estado

El icono de WiFi que se ve en Bagisto POS indica:
- **Verde/lleno**: Conectado y sincronizado
- **Amarillo/parcial**: Conectado, sincronizacion pendiente
- **Rojo/vacio**: Sin conexion, modo offline activo

---

## 7. Estructura del Modulo Gradle

### 7.1 Arbol de Paquetes

```
pos-module/
  build.gradle.kts
  src/
    main/
      java/com/cookiesstore/pos/
        config/
          PosModuleConfiguration.java
          PosSecurityConfig.java
        domain/
          PosSourceConfig.java
          PosOperator.java
          PosOperatorRole.java          (enum)
          PosSession.java
          PosSessionStatus.java         (enum)
          PosDrawer.java
          PosDrawerStatus.java          (enum)
          PosDrawerMovement.java
          PosDrawerMovementType.java    (enum)
          PosHeldOrder.java
          PosHeldOrderStatus.java       (enum)
          PosPayment.java
          PosPaymentMethod.java         (enum)
          PosOfflineSyncQueue.java
          PosOfflineSyncStatus.java     (enum)
          PosOfflineSyncEventType.java  (enum)
        repository/
          PosSourceConfigRepository.java
          PosOperatorRepository.java
          PosSessionRepository.java
          PosDrawerRepository.java
          PosDrawerMovementRepository.java
          PosHeldOrderRepository.java
          PosPaymentRepository.java
          PosOfflineSyncQueueRepository.java
        service/
          PosSessionService.java
          PosDrawerService.java
          PosSaleService.java
          PosHeldOrderService.java
          PosPaymentService.java
          PosCatalogService.java
          PosCustomerService.java
          PosOfflineSyncService.java
          PosReportService.java
        service/exception/
          PosSessionAlreadyOpenException.java
          PosDrawerNotOpenException.java
          PosInsufficientCashException.java
          PosOperatorNotAuthorizedException.java
          PosHeldOrderExpiredException.java
          PosOfflineSyncConflictException.java
          PosPaymentShortfallException.java
        web/
          controller/
            PosAuthController.java
            PosSessionController.java
            PosDrawerController.java
            PosCatalogController.java
            PosOrderController.java
            PosHeldOrderController.java
            PosCustomerController.java
            PosSyncController.java
            PosReportController.java
          dto/
            request/
              OpenSessionRequest.java
              CloseSessionRequest.java
              OpenDrawerRequest.java
              CloseDrawerRequest.java
              CashMovementRequest.java
              CreatePosOrderRequest.java
              PosOrderItemRequest.java
              PosPaymentRequest.java
              HoldOrderRequest.java
              QuickCreateCustomerRequest.java
              OfflineSyncPushRequest.java
            response/
              PosSessionResponse.java
              PosDrawerStatusResponse.java
              PosCatalogProductResponse.java
              PosOrderResponse.java
              PosHeldOrderResponse.java
              PosDailySummaryResponse.java
              PosSyncStatusResponse.java
          advice/
            PosExceptionHandler.java
      resources/
        messages_pos.properties
    test/
      java/com/cookiesstore/pos/
        service/
          PosSessionServiceTest.java
          PosDrawerServiceTest.java
          PosSaleServiceTest.java
          PosHeldOrderServiceTest.java
          ...
        web/
          PosOrderControllerTest.java
          ...
```

### 7.2 build.gradle.kts

```kotlin
plugins {
    id("java-library")
}

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql")
}
```

### 7.3 Registro en settings.gradle.kts

```kotlin
include(
    ":common",
    ":catalog-module",
    ":cart-module",
    ":order-module",
    ":payment-module",
    ":admin-module",
    ":pos-module",       // <- NUEVO
    ":application"
)
```

### 7.4 Dependencia en application/build.gradle.kts

```kotlin
dependencies {
    implementation(project(":common"))
    implementation(project(":admin-module"))
    implementation(project(":pos-module"))   // <- NUEVO
    // ... otros modulos
}
```

---

## 8. Flujos Principales

### 8.1 Flujo Completo de Venta POS

```
Operador llega a la tienda
    |
    v
[1. LOGIN] -> POST /api/pos/auth/login
    |          (email + password o PIN)
    v
[2. ABRIR SESION] -> POST /api/pos/sources/{code}/sessions
    |                 (valida PosOperator activo para este source)
    v
[3. ABRIR CAJA] -> POST /api/pos/sources/{code}/drawer/open
    |               Body: { "openingAmount": 500.00, "remarks": "Turno matutino" }
    v
[4. GRID DE PRODUCTOS] -> GET /api/pos/sources/{code}/products
    |                      (filtra por ProductSource activos del source)
    v
[5. SELECCIONAR CLIENTE (opcional)]
    |  -> GET /api/pos/sources/{code}/customers/search?q=...
    |  -> o dejar como Guest
    v
[6. AGREGAR ITEMS AL CARRITO] (gestionado en frontend)
    |  Producto seleccionado -> validar stock local
    |  Cantidad ajustada -> calcular subtotales
    |  Descuento aplicado -> recalcular
    v
[7. PAGO]
    |  -> Cajero indica metodo(s) de pago
    |  -> Si cash: registrar monto entregado, calcular cambio
    |  -> Si tarjeta: registrar referencia
    |  -> Si mixto: multiples PosPayment
    v
[8. CREAR ORDEN] -> POST /api/pos/sources/{code}/orders
    |  Body: {
    |    "customerId": 42 | null,
    |    "guest": true | false,
    |    "items": [
    |      { "productId": 1, "quantity": 2, "unitPriceMinor": 1400 }
    |    ],
    |    "payments": [
    |      { "method": "CASH", "amountMinor": 5000, "tenderedMinor": 5000 },
    |      { "method": "CARD", "amountMinor": 800, "referenceCode": "TX-123" }
    |    ],
    |    "discountMinor": 0,
    |    "note": "..."
    |  }
    |
    |  Backend:
    |   a) Crear Order (status=PROCESSING, channel=POS)
    |   b) Crear OrderItems con source_id
    |   c) Crear PosPayment records
    |   d) StockMovementService.record() por cada item (type=SALE)
    |   e) Actualizar PosDrawer totales
    |   f) Marcar Order status=DELIVERED (entrega inmediata)
    v
[9. RECIBO] -> POST /api/pos/sources/{code}/orders/{id}/receipt
    |           (genera PDF/texto para impresora termica)
    v
[10. SIGUIENTE VENTA] -> volver a paso 4
    ...
    v
[11. CERRAR CAJA] -> POST /api/pos/sources/{code}/drawer/close
    |   Body: { "actualClosingAmount": 1523.50, "remarks": "Todo cuadra" }
    |   -> Calcula diferencia
    |   -> Reporta discrepancias
    v
[12. CERRAR SESION] -> POST /api/pos/sources/{code}/sessions/current/close
```

### 8.2 Flujo de Hold / Resume Order

```
Cajero esta atendiendo a Cliente A
    |
    v
Cliente A dice "espere, olvide algo"
    |
    v
[HOLD] -> POST /api/pos/sources/{code}/held-orders
    |      Body: { "cartSnapshot": {...items...}, "note": "Cliente fue a buscar leche" }
    |      -> Serializa carrito a JSONB
    |      -> Status = HELD
    |      -> Genera reference_code
    v
Cajero atiende a Cliente B (nueva venta normal)
    |
    v
Cliente A regresa
    |
    v
[RESUME] -> POST /api/pos/sources/{code}/held-orders/{id}/resume
    |        -> Rehidrata carrito
    |        -> Valida precios actuales (alerta si cambiaron)
    |        -> Valida stock actual (alerta si insuficiente)
    |        -> Devuelve carrito al frontend
    v
Cajero completa la venta normalmente
```

### 8.3 Flujo de Devolucion POS

```
Cliente trae producto a devolver
    |
    v
[BUSCAR ORDEN] -> GET /api/pos/sources/{code}/orders?search=15
    |              -> Muestra detalle de orden #15
    v
[DEVOLUCION] -> POST /api/pos/sources/{code}/orders/15/return
    |   Body: {
    |     "items": [
    |       { "orderItemId": 101, "quantity": 1, "reason": "Producto defectuoso" }
    |     ],
    |     "refundMethod": "CASH" | "ORIGINAL_METHOD" | "STORE_CREDIT"
    |   }
    |
    |   Backend:
    |   a) Validar orden pertenece a este source
    |   b) Validar cantidades (no devolver mas de lo comprado)
    |   c) Crear OrderStatusHistory (REFUNDED partial/full)
    |   d) StockMovementService.record(type=RETURN, quantityDelta=+N)
    |   e) Si refund es CASH -> actualizar PosDrawer.cash_refunds_minor
    |   f) Crear PosPayment negativo como registro
    v
Cajero entrega reembolso
```

---

## 9. Seguridad

### 9.1 Autenticacion POS

Dos modos de autenticacion:

1. **Credenciales completas**: email + password (para abrir sesion)
2. **PIN rapido**: PIN de 4-6 digitos (para operaciones dentro de la sesion)

El PIN se hashea con BCrypt y se almacena en `PosOperator.pin_hash`.
Operaciones que requieren autorizacion supervisor (voids, devoluciones) solicitan
el PIN del supervisor.

### 9.2 Seguridad de Endpoints

```java
// Todos los endpoints POS requieren:
// 1. AdminUser autenticado (JWT existente)
// 2. PosOperator activo para el source
// 3. PosSession abierta (excepto login y open session)
// 4. Rol POS adecuado para la operacion

@PreAuthorize("hasAbility('pos:operate') and isActivePosOperator(#sourceCode)")
```

### 9.3 Auditoria

Toda operacion POS queda registrada:
- `PosSession` registra quien opero y cuando
- `PosDrawer` registra todos los montos
- `PosDrawerMovement` registra entradas/salidas manuales
- `PosPayment` registra cada pago
- `AdminSourceStockMovement` (existente) registra cada movimiento de stock
- `OrderStatusHistory` (existente) registra cambios de estado

---

## 10. Admin Module: Extensiones Necesarias

### 10.1 Nuevos Controllers en admin-module

```
AdminPosConfigController.java        -> CRUD config POS por source
AdminPosOperatorController.java      -> CRUD operadores POS
AdminPosSessionHistoryController.java -> Consulta sesiones pasadas
AdminPosReportController.java         -> Reportes consolidados
```

### 10.2 Nuevos Servicios en admin-module

```
AdminPosConfigService.java           -> Habilitar/deshabilitar POS en source
AdminPosOperatorService.java         -> Asignar/revocar operadores
```

### 10.3 Vistas Admin (Thymeleaf)

```
templates/admin/sources/_pos-config-tab.html    -> Tab en detalle de source
templates/admin/sources/pos-operators.html       -> Lista de operadores
templates/admin/sources/pos-operator-form.html   -> Form crear/editar operador
templates/admin/sources/pos-sessions.html        -> Historial sesiones
templates/admin/reports/pos-daily.html           -> Reporte diario POS
```

---

## 11. Plan de Implementacion por Milestones

### Milestone P1: Fundacion POS (Minimo Viable)

| Tarea | Descripcion |
|-------|-------------|
| P1.1 | Crear `pos-module` en Gradle, registrar en settings.gradle.kts |
| P1.2 | Entidades: `PosSourceConfig`, `PosOperator`, `PosOperatorRole` |
| P1.3 | Entidades: `PosSession`, `PosDrawer`, `PosDrawerStatus` |
| P1.4 | Repositorios JPA para todas las entidades P1 |
| P1.5 | Admin: `AdminPosConfigService` + controller para habilitar POS en Source |
| P1.6 | Admin: `AdminPosOperatorService` + controller para gestionar operadores |
| P1.7 | Tests unitarios e integracion para P1.2-P1.6 |

### Milestone P2: Sesion y Caja

| Tarea | Descripcion |
|-------|-------------|
| P2.1 | `PosSessionService` (abrir, cerrar, suspender sesion) |
| P2.2 | `PosDrawerService` (abrir, cerrar, movimientos manuales) |
| P2.3 | Entidad `PosDrawerMovement` + repository |
| P2.4 | Controllers: `PosSessionController`, `PosDrawerController` |
| P2.5 | Autenticacion POS (login con credenciales, PIN para operaciones) |
| P2.6 | Tests completos Milestone P2 |

### Milestone P3: Catalogo y Ventas

| Tarea | Descripcion |
|-------|-------------|
| P3.1 | `PosCatalogService` (productos por source, busqueda, categorias) |
| P3.2 | `PosSaleService` (crear orden POS, calcular totales) |
| P3.3 | Entidad `PosPayment` + `PosPaymentMethod` |
| P3.4 | `PosPaymentService` (procesar pagos mixtos, cambio) |
| P3.5 | Integracion con `StockMovementService` (decrementar stock en venta) |
| P3.6 | Integracion con `OrderService` (crear Order con channel=POS) |
| P3.7 | Agregar `order_channel` y `pos_session_id` a `Order` (migracion) |
| P3.8 | Controllers: `PosCatalogController`, `PosOrderController` |
| P3.9 | Tests completos Milestone P3 |

### Milestone P4: Hold Orders y Devoluciones

| Tarea | Descripcion |
|-------|-------------|
| P4.1 | Entidad `PosHeldOrder` + repository |
| P4.2 | `PosHeldOrderService` (hold, resume, expire, cancel) |
| P4.3 | Controllers: `PosHeldOrderController` |
| P4.4 | Flujo de devolucion POS en `PosSaleService` |
| P4.5 | Actualizacion de drawer en devoluciones cash |
| P4.6 | Tests completos Milestone P4 |

### Milestone P5: Reportes y Recibos

| Tarea | Descripcion |
|-------|-------------|
| P5.1 | `PosReportService` (resumen diario, por sesion, top productos) |
| P5.2 | Generacion de recibos (texto plano para impresora termica) |
| P5.3 | Controllers: `PosReportController` |
| P5.4 | Admin: Vistas Thymeleaf para reportes POS |
| P5.5 | Tests completos Milestone P5 |

### Milestone P6: Modo Offline

| Tarea | Descripcion |
|-------|-------------|
| P6.1 | Entidad `PosOfflineSyncQueue` + repository |
| P6.2 | `PosOfflineSyncService` (push, pull, conflict resolution) |
| P6.3 | Endpoint snapshot de catalogo para cache local |
| P6.4 | Controller: `PosSyncController` |
| P6.5 | Tests de sync y conflictos |
| P6.6 | Documentacion de estructura IndexedDB para frontend |

### Milestone P7: Frontend POS (separado)

> Nota: El frontend POS seria una aplicacion SPA separada (React/Vue/Svelte)
> que se comunica con la API REST del pos-module. Puede ser un proyecto Gradle
> separado o un repositorio independiente. La arquitectura PWA (Service Worker)
> habilita el modo offline.

| Tarea | Descripcion |
|-------|-------------|
| P7.1 | Scaffold proyecto frontend PWA |
| P7.2 | Pantalla de login POS |
| P7.3 | Grid de productos con categorias y busqueda |
| P7.4 | Carrito lateral con calculo de totales |
| P7.5 | Pantalla de cobro (cash, card, mixto) |
| P7.6 | Pantalla de cajero (drawer open/close) |
| P7.7 | Pantalla de hold orders |
| P7.8 | Historial de ordenes y detalle |
| P7.9 | Pantalla de clientes y busqueda |
| P7.10 | Modo offline con Service Worker + IndexedDB |
| P7.11 | Impresion de recibos |

---

## 12. Diagrama Entidad-Relacion Completo

```
                                      ENTIDADES EXISTENTES (common)
    +--------+     +---------------+     +---------+     +----------+
    | Source  |<--->| ProductSource |<--->| Product |<--->| Category |
    +--------+     +---------------+     +---------+     +----------+
        |                |                     |
        |                |                     |
        v                v                     v
    +------------------+  +-----------+   +----------+
    | StockMovement    |  | Price     |   | OrderItem|
    | (audit trail)    |  +-----------+   +----------+
    +------------------+                       |
                                               v
    +----------+     +-----------+         +-------+
    | Customer |<--->| Address   |<------->| Order |
    +----------+     +-----------+         +-------+
                                               ^
                                               |
                        ENTIDADES POS (pos-module)
                                               |
    +------------------+     +-------------+   |
    | PosSourceConfig  |     | PosOperator |   |
    | (1:1 con Source) |     +-------------+   |
    +------------------+          |            |
                                  v            |
                            +------------+     |
                            | PosSession |     |
                            +------------+     |
                              |       |        |
                    +---------+       +----+   |
                    v                      v   |
              +-----------+        +-------------+
              | PosDrawer |        | PosHeldOrder |
              +-----------+        +-------------+
                    |
                    v
            +-------------------+
            | PosDrawerMovement |
            +-------------------+

                            +------------+
                            | PosPayment |----> Order
                            +------------+----> PosSession
```

---

## 13. Consideraciones Tecnicas

### 13.1 Performance

- **Catalogo POS**: Cache con Redis (1-5 min TTL) para el grid de productos
  por source. Invalidar al recibir movimientos de stock.
- **Busqueda**: Indice en `Product.sku` y `Product.name` (ya existente).
  Considerar full-text search con PostgreSQL `tsvector` para busqueda rapida.
- **Transacciones**: Las ventas POS deben ser atomicas. Usar `@Transactional`
  con isolation `READ_COMMITTED` (default de PostgreSQL).

### 13.2 Concurrencia

- **Stock**: Pessimistic lock en `ProductSource` al momento de la venta
  (igual que `StockMovementService` existente).
- **Drawer**: Solo una sesion activa por source a la vez.
  Constraint: `UNIQUE(source_id) WHERE status = 'OPEN'` (partial unique index).
- **Held Orders**: Expiracion automatica despues de `max_held_orders` o timeout
  configurable.

### 13.3 Escalabilidad

- Cada Source es un POS independiente.
- Multiples Sources pueden operar simultaneamente sin interferencia.
- El modelo soporta cadenas de tiendas: N Sources = N puntos de venta.

---

## 14. Glosario

| Termino | Definicion |
|---------|-----------|
| Source | Ubicacion fisica o logica de inventario (tienda, almacen) |
| POS | Point of Sale - punto de venta presencial |
| Drawer | Caja registradora fisica (el contenedor de dinero) |
| Session | Turno de trabajo de un operador (de apertura a cierre de caja) |
| Held Order | Orden pausada temporalmente para atender otro cliente |
| Offline Mode | Capacidad de operar sin conexion a internet |
| Sync Queue | Cola de operaciones realizadas offline pendientes de sincronizar |
| PIN | Numero de identificacion personal para operaciones rapidas |
| Tender | Monto entregado por el cliente (puede ser mayor al total) |
| Change | Cambio/vuelto (tender - total de la orden) |
| Void | Anulacion de una venta completa |

---

## Apendice A: Comparativa con Bagisto POS

| Feature Bagisto POS | Estado en nuestro diseno | Notas |
|---|---|---|
| Product grid con categorias | Diseado (P3.1) | PosCatalogService |
| Search + barcode scan | Diseado (P3.1) | Busqueda por SKU/nombre |
| Customer select/create | Diseado (P3.2) | Guest por defecto, seleccion opcional |
| Offline Customers tab | Diseado (P6) | PosOfflineSyncQueue |
| Cash Drawer management | Diseado (P2) | PosDrawer + PosDrawerMovement |
| Today's Sale | Diseado (P5.1) | Reporte diario |
| Sale History | Diseado (P3.8) | Historial por source |
| Hold Orders | Diseado (P4) | PosHeldOrder con JSONB snapshot |
| Orders On Hold tab | Diseado (P4.3) | Tab en frontend |
| Offline Orders tab | Diseado (P6) | Sync queue |
| Return + Print Invoice | Diseado (P4.4, P5.2) | Devolucion + recibos |
| Multiple payment methods | Diseado (P3.3) | PosPayment soporta mixto |
| Settings section | Diseado (P1.5) | PosSourceConfig en Admin |
| Reports section | Diseado (P5) | Multiples reportes |
| Full-screen mode | Frontend (P7) | CSS fullscreen API |
| Dark mode | Frontend (P7) | Theme toggle |
| WiFi/offline indicator | Frontend (P7.10) | Navigator.onLine + visual indicator |

## Apendice B: Lo que ya existe y se reutiliza

| Componente existente | Uso en POS |
|---|---|
| `Source` entity | Cada Source con POS habilitado es un punto de venta |
| `ProductSource` | Stock y precios por Source para el grid POS |
| `Product`, `Category` | Catalogo de productos tal cual |
| `Order`, `OrderItem` | Las ventas POS crean Orders normales |
| `Customer` | Seleccion opcional de cliente en POS |
| `StockMovementService` | Decrementar stock al vender, incrementar al devolver |
| `AdminUser` | Los operadores POS son AdminUsers con rol adicional |
| `Domain + Ability + Permission` (ABAC) | Control de acceso granular por Source |
| `Currency`, `Price` | Multi-currency ya soportado |
| `AdminSourceStockMovement` | Audit trail completo de movimientos |
| `AdminSourceAlert` | Alertas automaticas de stock bajo |
| `GlobalExceptionHandler`, `ApiResponse` | Manejo de errores consistente |
