FROM sbtscala/scala-sbt:17.0.2_1.6.2_3.1.3 AS builder
COPY . /app
WORKDIR /app
RUN sbt clean compile assembly

FROM gcr.io/distroless/java17-debian11 AS dist
COPY --from=builder /app/target/scala-3.1.3/clearscore-fat.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-cp", "app.jar", "Main"]


