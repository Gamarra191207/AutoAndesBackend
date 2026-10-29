# 🚗 AutoAndes API REST (Backend)

Backend REST empresarial desarrollado con **Spring Boot 3.4 / Java 21**, conectado a **Oracle Database 23ai** y protegido con **Spring Security, JWT y RBAC**.

---

## 🛠️ Tecnologías Empleadas
- **Java 21 LTS**
- **Spring Boot 3.4.x / 3.3.4**
- **Spring Data JPA & Hibernate**
- **Spring Security 6/7**
- **JJWT 0.12.6** (Tokens compactos HMAC-SHA)
- **Oracle JDBC Driver (`ojdbc11`)**
- **SpringDoc OpenAPI 3 / Swagger UI**
- **Lombok**

---

## 👥 Usuarios de Prueba y Credenciales

Todos los usuarios tienen la contraseña: **`Auto2026*`**

| Usuario | Contraseña | Rol Asignado | Estado | Permisos Asignados |
| :--- | :--- | :--- | :--- | :--- |
| **`admin`** | `Auto2026*` | `ADMIN` | Activo | Todos los 8 permisos del sistema |
| **`asesor`** | `Auto2026*` | `ASESOR` | Activo | `SERVICIO_LEER`, `VEHICULO_LEER`, `VEHICULO_ESCRIBIR`, `ORDEN_LEER`, `ORDEN_REGISTRAR` |
| **`jefe`** | `Auto2026*` | `JEFE_TALLER` | Activo | `SERVICIO_LEER`, `SERVICIO_ESCRIBIR`, `VEHICULO_LEER`, `ORDEN_LEER`, `ORDEN_ANULAR`, `REPORTE_VER` |
| **`inactivo`**| `Auto2026*` | `ASESOR` | Inactivo | Rechazado en login (401 genérico) |

---

## 📋 Reglas de Negocio Implementadas

- **`RN-01` (Servicios):** El código es único (409). Un servicio utilizado en órdenes no se elimina físicamente (DELETE responde 409) y se ofrece `PATCH /servicios/{id}/estado` para darlo de baja lógica.
- **`RN-02` (Vehículos):** La placa es única con formato `ABC-123` (409) y el cliente debe existir (404). Un vehículo con órdenes no se puede eliminar físicamente (409).
- **`RN-03` (Órdenes):** Entre 1 y 10 detalles, sin repetir el mismo servicio (400), cantidad de 1 a 5. Todos los servicios deben estar activos (409).
- **`RN-04` (Kilometraje):** El kilometraje no puede ser menor que el de la última orden REGISTRADA del mismo vehículo (409 con el último kilometraje en el mensaje).
- **`RN-05` (Anulación):** Solo se anula una orden REGISTRADA y en el mismo día de su registro; motivo obligatorio de 10 a 200 caracteres (400 o 409).

---

## 🚀 Instrucciones de Ejecución

1. Asegurarse de que Oracle Database esté levantado en el puerto `1522` (`FREEPDB1`).
2. Compilar y ejecutar con Maven Wrapper:
```bash
./mvnw clean spring-boot:run
```
3. Acceder a la documentación Swagger:
```text
http://localhost:8080/swagger-ui.html
```
