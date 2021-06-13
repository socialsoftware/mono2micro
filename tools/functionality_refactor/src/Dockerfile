# Build Stage
FROM golang:1.14-buster AS build-stage

RUN apt-get update && apt-get install -y ca-certificates

COPY . /app

WORKDIR /app

RUN make build

# Final Stage
FROM debian:buster-slim

RUN apt-get update && apt-get install -y ca-certificates

WORKDIR /app

COPY --from=build-stage /app/bin/ /app/
RUN chmod +x /app/

CMD ["/app/web_service"]
