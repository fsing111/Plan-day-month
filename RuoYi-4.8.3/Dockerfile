FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
COPY ruoyi-common/pom.xml ruoyi-common/
COPY ruoyi-system/pom.xml ruoyi-system/
COPY ruoyi-framework/pom.xml ruoyi-framework/
COPY ruoyi-quartz/pom.xml ruoyi-quartz/
COPY ruoyi-generator/pom.xml ruoyi-generator/
COPY ruoyi-plan/pom.xml ruoyi-plan/
COPY ruoyi-achievement/pom.xml ruoyi-achievement/
COPY ruoyi-approval/pom.xml ruoyi-approval/
COPY ruoyi-notification/pom.xml ruoyi-notification/
COPY ruoyi-admin/pom.xml ruoyi-admin/

RUN mvn dependency:go-offline -B

COPY ruoyi-common/src ruoyi-common/src/
COPY ruoyi-system/src ruoyi-system/src/
COPY ruoyi-framework/src ruoyi-framework/src/
COPY ruoyi-quartz/src ruoyi-quartz/src/
COPY ruoyi-generator/src ruoyi-generator/src/
COPY ruoyi-plan/src ruoyi-plan/src/
COPY ruoyi-achievement/src ruoyi-achievement/src/
COPY ruoyi-approval/src ruoyi-approval/src/
COPY ruoyi-notification/src ruoyi-notification/src/
COPY ruoyi-admin/src ruoyi-admin/src/

RUN mvn package -DskipTests -B

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/ruoyi-admin/target/ruoyi-admin.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]
