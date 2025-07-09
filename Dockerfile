FROM alpine:3.19

RUN apk add --no-cache libc6-compat

WORKDIR /app

COPY target/rinha2025 /app/rinha2025

RUN chmod +x /app/rinha2025

EXPOSE 8080

ENTRYPOINT ["./rinha2025"]