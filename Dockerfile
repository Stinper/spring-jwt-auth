FROM gradle:8.11-jdk21 AS build

ARG MODULE_FOLDER_NAME

WORKDIR /app
COPY . .

RUN gradle :${MODULE_FOLDER_NAME}:bootJar --no-daemon -x test
RUN java -Djarmode=layertools -jar ./${MODULE_FOLDER_NAME}/build/libs/*.jar extract --destination extracted


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]