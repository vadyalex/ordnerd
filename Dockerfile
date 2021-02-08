FROM gcr.io/distroless/java:11

WORKDIR /ordnerd

COPY target/ordnerd.jar /ordnerd/ordnerd.jar

ENV PORT 5000

EXPOSE 5000

CMD ["ordnerd.jar"]