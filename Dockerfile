FROM m.daocloud.io/docker.io/node:20-alpine AS frontend-builder

WORKDIR /workspace/admin-web

COPY admin-web/package*.json ./
RUN npm ci --no-audit --no-fund

COPY admin-web/ ./
RUN npm run build

FROM m.daocloud.io/docker.io/maven:3.9.9-eclipse-temurin-17 AS backend-builder

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
COPY --from=frontend-builder /workspace/admin-web/dist ./src/main/resources/static
RUN mvn -B -DskipTests package

FROM m.daocloud.io/docker.io/eclipse-temurin:17-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS=""

RUN apk add --no-cache \
        ripgrep \
        sed \
        coreutils \
    && addgroup -S bear \
    && adduser -S bear -G bear

COPY --from=backend-builder /workspace/target/bear-mcp-single.jar /app/bear-mcp-single.jar

USER bear

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/bear-mcp-single.jar"]
