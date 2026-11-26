> **Caso de estudio: migración MySQL → PostgreSQL — Curso Integrador I (UTP).** Copia del proyecto BikeCare usada para documentar la migración de MySQL a PostgreSQL y el despliegue de una aplicación Spring Boot con Thymeleaf.
>
> | Componente | Servicio | URL |
> |---|---|---|
> | Aplicación (Spring Boot + Thymeleaf, Docker) | Render | https://bikecare-app.onrender.com |
> | Base de datos (PostgreSQL) | Neon | tablas creadas por Hibernate + `database/datos_iniciales_postgres.sql` |
>
> Cambios de la migración (un commit cada uno): driver `org.postgresql:postgresql`, `application.properties` con URL `jdbc:postgresql` y variables de entorno (sin `MySQLDialect`), datos iniciales en sintaxis PostgreSQL (`crear-admin.sql` sin `FROM DUAL`) y `Dockerfile`.

BikeCare 

Plataforma web desarrollada en Java con Spring Boot, JPA/Hibernate, Thymeleaf y MySQL para ciclistas. Permite gestionar bicicletas, monitorear el desgaste de componentes mecánicos por kilometraje, registrar rutas y administrar métodos de pago.

1. Requisitos Previos

-Java Development Kit (JDK): Versión 17 o superior.
-SQL Server: Versión 8.0 o superior.
-IDE recomendado: Visual Studio Code o IntelliJ IDEA.
-Git: Versión 2.30 o superior.


2. Configuración de Base de Datos

1. Abre tu gestor de MySQL (Workbench, DBeaver o terminal).
2. Ejecuta el script de creación y datos iniciales ubicado en:  database/schema_sprint1.sql
