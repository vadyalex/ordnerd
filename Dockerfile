FROM openjdk:11-jre-slim

WORKDIR /ordnerd

COPY target/ordnerd.jar /ordnerd/ordnerd.jar

ENV PORT 5000

EXPOSE 5000

CMD ["java", "-jar", "ordnerd.jar"]