# ---- Etapa 1: Construcción (Build Stage) ----
# Usamos una imagen oficial de Maven con Java 17 para compilar nuestro proyecto.
# 'AS build' le da un nombre a esta etapa para que podamos referirnos a ella más tarde.
FROM maven:3.8-openjdk-17 AS build

# Establecemos el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copiamos solo el pom.xml primero para aprovechar la caché de Docker.
# Si las dependencias no cambian, Docker no las volverá a descargar.
COPY pom.xml .

# Descargamos todas las dependencias definidas en el pom.xml.
RUN mvn dependency:go-offline

# Ahora copiamos el resto del código fuente de nuestro proyecto.
COPY src ./src

# Ejecutamos el comando de Maven para compilar y empaquetar la aplicación en un archivo .jar.
# '-DskipTests' acelera el proceso al saltarse la ejecución de las pruebas unitarias.
RUN mvn clean install -DskipTests


# ---- Etapa 2: Ejecución (Package Stage) ----
# Usamos una imagen de Java 17 mucho más ligera ("slim") para ejecutar nuestra aplicación.
# Esto hace que nuestra imagen final sea mucho más pequeña.
FROM openjdk:17-jdk-slim

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos el archivo .jar que se generó en la etapa 'build'.
# Asegúrate de que el nombre del .jar coincida con el de tu proyecto.
# Puedes encontrar el nombre en la carpeta 'target' después de compilar.
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto que nuestra aplicación usa (el que definimos en application.properties).
# Render usará esta información para mapear el puerto correctamente.
EXPOSE 3600

# Este es el comando que se ejecutará para iniciar la aplicación cuando el contenedor arranque.
ENTRYPOINT ["java", "-jar", "app.jar"]
