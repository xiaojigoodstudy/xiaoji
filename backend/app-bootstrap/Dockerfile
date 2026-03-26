FROM eclipse-temurin:8-jre
WORKDIR /app

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
ENV SPRING_PROFILES_ACTIVE="mysql"

ARG APP_JAR=backend/app-bootstrap/target/app-bootstrap-0.1.0-SNAPSHOT-exec.jar
COPY ${APP_JAR} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
