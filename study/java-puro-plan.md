# Plan De Estudio: Java Puro

Duracion sugerida: 6 semanas  
Objetivo: fortalecer lenguaje Java moderno (Java 21) usando el dominio actual del repo.

## Semana 1: Inmutabilidad, `record`, `Optional`

Objetivos:
- Usar `record` para modelar comandos/resultados internos.
- Reducir `null`-checks repetitivos con `Optional`.

Tareas:
1. Crear `CreateOrderCommand` como `record` en `common/services/orders`.
2. Introducir `OrderCreationResult` como `sealed interface` + implementaciones (`Success`, `ValidationError`).
3. Refactorizar 2-3 metodos de `OrderService` para evitar ramificaciones por `null` cuando sea viable.

Hecho cuando:
- Se elimina al menos un bloque grande de validacion imperativa redundante.
- Tests existentes siguen pasando.

## Semana 2: Streams y Collectors avanzados

Objetivos:
- Dominar `groupingBy`, `mapping`, `reducing`, `collectingAndThen`.

Tareas:
1. Crear un servicio de resumen de pedidos (por estado, moneda y cantidad).
2. Exponer metodos puros que retornen estructuras agregadas desde colecciones en memoria.
3. Escribir tests para casos borde (listas vacias, monedas mixtas, cantidades negativas no permitidas).

Hecho cuando:
- Tienes al menos 3 metodos de agregacion con tests.
- No hay loops imperativos donde `Stream` mejore claridad.

## Semana 3: Excepciones de dominio robustas

Objetivos:
- Estandarizar errores de negocio con contexto.

Tareas:
1. Crear una base `DomainException` con `errorCode` y `context`.
2. Migrar excepciones de orden a este esquema gradualmente.
3. Asegurar mensajes consistentes para UI/API.

Hecho cuando:
- Todas las excepciones nuevas incluyen `errorCode`.
- Se reduce duplicacion de clases excepcion triviales.

## Semana 4: `java.time` + `Clock` inyectable

Objetivos:
- Evitar `Instant.now()` directo en logica de negocio.

Tareas:
1. Introducir `Clock` en servicios donde se genera tiempo.
2. Reemplazar llamadas directas a `Instant.now()` en componentes candidatos.
3. Agregar tests deterministas usando `Clock.fixed(...)`.

Hecho cuando:
- No dependes del reloj del sistema en tests de negocio.

## Semana 5: Pattern matching y `switch` moderno

Objetivos:
- Usar `switch` y pattern matching para codigo mas expresivo y seguro.

Tareas:
1. Aplicar `switch` a mapeo de estados de orden o conversion de errores.
2. Reemplazar cadenas de `if/else` repetitivas en un flujo claro del dominio.

Hecho cuando:
- El flujo refactorizado tiene menos complejidad ciclomatica.

## Semana 6: Concurrencia con `CompletableFuture` (basico-medio)

Objetivos:
- Entender paralelismo controlado y combinacion de resultados.

Tareas:
1. Implementar una prueba de concepto de calculos paralelos de resumen (solo lectura).
2. Combinar resultados con `thenCombine`.
3. Medir tiempos basicos y comparar secuencial vs paralelo.

Hecho cuando:
- Tienes una implementacion paralela con fallback secuencial y tests.

## Checklist Final

1. Al menos 1 refactor grande de `OrderService` orientado a lenguaje.
2. 15+ tests nuevos (unitarios o property-based con jqwik).
3. Documentacion corta por semana en PR/commit message sobre lo aprendido.
