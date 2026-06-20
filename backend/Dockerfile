# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 使用阿里云 Maven 镜像加速
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn dependency:go-offline -s /root/.m2/settings.xml
COPY src ./src
RUN mvn clean package -DskipTests -s /root/.m2/settings.xml

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /app/uploads

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
