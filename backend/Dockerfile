# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copier d'abord les fichiers de deps pour profiter du cache Docker
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Puis le code source (invalide le cache seulement ici, pas les deps)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root (bonne pratique + parfois requis)
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Réglages JVM critiques pour petite VM (1 Go RAM)
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=60.0", \
  "-XX:+UseSerialGC", \
  "-XX:TieredStopAtLevel=1", \
  "-Xss256k", \
  "-jar", "app.jar"]
