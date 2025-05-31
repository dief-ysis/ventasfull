# ventasfull

Estructura del Repositorio ├── usuario/ ├── inventario/ ├── carrito/ ├── script_creacion_tablas.sql ├── informe.pdf ├── README.md Microservicios

Microservicio de Usuario • Ruta base: /usuarios • CRUD completo de usuarios • Filtros por nombre o correo • Base de datos independiente
Microservicio de Inventario • Ruta base: /inventario • CRUD de productos • Filtros por categoría y stock • Base de datos independiente
Microservicio de Carrito • Ruta base: /carrito • CRUD de ítems del carrito • Validaciones cruzadas con usuarios e inventario • Base de datos independiente Tecnologías Utilizadas • Java 17 • Spring Boot 3 • Spring Data JPA • Maven • PostgreSQL / MySQL • Git / GitHub • Postman Requisitos Previos • JDK 17 o superior • Maven • PostgreSQL o MySQL • Visual Studio Code o IDE similar Instrucciones para Ejecutar Cada microservicio es un proyecto Spring Boot independiente. Para ejecutarlos:
Abrir Visual Studio Code o un IDE.
Abrir cada carpeta (usuario/, inventario/, carrito/) en ventanas independientes.
Ejecutar cada microservicio con el comando:
./mvnw spring-boot:run Pruebas • Se incluye una colección de Postman que valida el funcionamiento de todos los endpoints. • También se entrega un archivo Excel con al menos 5 ejemplos de cada endpoint: o Método HTTP o URL o JSON de entrada o JSON de salida GitHub y Control de Versiones • Ramas utilizadas: main y develop • Trabajo colaborativo en la rama develop • La versión final fue integrada en la rama main antes del 25 de mayo de 2025 Autor(es) • [David Fuentes 1] • [Allan Salinas Z.] Profesor Tomás Opazo — tom.opazo@profesor.duoc.cl
Este proyecto fue desarrollado como parte de la Evaluación Parcial N°2 del ramo DSY1103.
