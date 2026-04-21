# Plan De Estudio: Spring

Duracion sugerida: 6 semanas  
Objetivo: profundizar en Spring Boot y su ecosistema sobre la base actual del proyecto.

## Semana 1: Eventos de dominio en Spring

Objetivos:
- Desacoplar acciones secundarias del flujo principal.

Tareas:
1. Publicar `OrderCreatedEvent` desde `OrderService`.
2. Crear listener para auditoria de orden.
3. Crear listener para notificacion simulada (log o persistencia simple).

Hecho cuando:
- Crear una orden dispara listeners sin ensuciar `OrderService`.

## Semana 2: Caching con Redis

Objetivos:
- Introducir caching de lectura y estrategia de invalidacion.

Tareas:
1. Habilitar cache de Spring (`@EnableCaching`).
2. Aplicar `@Cacheable` en listados frecuentes (productos/categorias).
3. Aplicar `@CacheEvict` en operaciones create/update/delete.

Hecho cuando:
- Demuestras hit/miss en logs o metricas.
- No quedan datos stale tras updates.

## Semana 3: Seguridad avanzada (`@PreAuthorize`)

Objetivos:
- Reforzar control de acceso declarativo.

Tareas:
1. Mapear permisos a authorities en autenticacion.
2. Agregar `@PreAuthorize` en endpoints sensibles del admin.
3. Mantener compatibilidad con modelo de dominio por `domainCode`.

Hecho cuando:
- Tests de autorizacion cubren al menos 3 escenarios: permitido, denegado, dominio incorrecto.

## Semana 4: API REST de pedidos en `order-module`

Objetivos:
- Practicar modularizacion real y boundaries.

Tareas:
1. Crear controlador REST de pedidos en `order-module`.
2. Exponer endpoints CRUD minimo para ordenes (o create/get/update estado).
3. Mantener `common` como shared-kernel (entidades/dto base).

Hecho cuando:
- El modulo `order-module` deja de ser placeholder y sirve endpoints reales.

## Semana 5: JPA performance (`EntityGraph`, N+1)

Objetivos:
- Entender y corregir consultas ineficientes.

Tareas:
1. Detectar N+1 en consultas de orden con items/direcciones.
2. Introducir `@EntityGraph` o queries especificas con `join fetch`.
3. Comparar antes/despues con logs SQL y conteo de queries.

Hecho cuando:
- Reduces numero de queries en al menos un caso critico.

## Semana 6: Observabilidad (Actuator + metricas)

Objetivos:
- Operar y diagnosticar la aplicacion mejor.

Tareas:
1. Exponer endpoints Actuator necesarios (health, metrics).
2. Agregar metricas custom en casos clave (`order.create`, tiempo de servicio).
3. Incorporar correlation-id en logs de request.

Hecho cuando:
- Puedes diagnosticar salud y tiempos sin entrar al debugger.

## Semana 7: Scheduler / Jobs (`@Scheduled`)

Objetivos:
- Aprender tareas programadas y procesamiento batch basico en Spring.

Tareas:
1. Habilitar scheduling con `@EnableScheduling`.
2. Crear un job nocturno para cerrar ordenes viejas en `PENDING` (segun regla de antiguedad).
3. Como alternativa o complemento, crear job para recalculo de stock de seguridad.
4. Agregar logs/metricas para verificar ejecucion y resultados del job.

Hecho cuando:
- El job corre automaticamente en horario definido.
- Queda cubierto con test de integracion o test de servicio con `Clock` controlado.

## Checklist Final

1. 1 flujo con eventos funcionando en produccion local.
2. 1 capa de cache con invalidacion correcta.
3. 1 modulo funcional nuevo (`order-module`) con endpoints propios.
4. 1 job programado funcional para mantenimiento operativo.
5. 10+ pruebas nuevas orientadas a seguridad/integracion.
