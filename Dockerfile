FROM node:lts-alpine3.14 AS frontend
WORKDIR /app
COPY ./frontend /app
RUN npm i -g pnpm && pnpm install --prod --frozen-lockfile && pnpm run build

FROM clojure:openjdk-18-lein-2.9.8-alpine AS build
ENV PORT=80
ENV IS_PRODUCTION=true
ENV MANIFEST_PATH=/app/resources/public/manifest.json
WORKDIR /app
COPY ./backend/ /app
COPY --from=frontend /app/dist /app/resources/public
RUN lein with-profile prod uberjar

FROM openjdk:18-jdk-alpine3.14 AS deploy
ENV PORT=80
ENV IS_PRODUCTION=true
ENV MANIFEST_PATH=/app/resources/public/manifest.json
ENV DB_FILE=/db/whether.sqlite
RUN apk update && apk --no-cache upgrade && \
  apk add --no-cache --update s6 tzdata && rm -rf /var/cache/apk/*
WORKDIR /app

COPY ./scripts/cron.sh /app/cron.sh
COPY ./scripts/start.sh /app/start.sh
COPY /scripts/crontabs /etc/crontabs/root
RUN chmod 744 /app/start.sh && \
    chmod 744 /app/cron.sh
COPY --from=frontend /app/dist /app/resources/public
COPY --from=build /app/target/whether-0.1.0-standalone.jar /app/whether.jar

ENTRYPOINT ["./start.sh"]
CMD ["java", "-jar", "whether.jar"] 