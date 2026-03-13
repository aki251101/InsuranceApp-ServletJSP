FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

FROM tomcat:10.1-jre17-temurin
ENV TZ=Asia/Tokyo
WORKDIR /usr/local/tomcat

# Remove sample webapps to keep image small and reduce attack surface.
RUN rm -rf webapps/*

COPY --from=builder /app/target/InsuranceApp.war webapps/InsuranceApp.war

EXPOSE 8080
