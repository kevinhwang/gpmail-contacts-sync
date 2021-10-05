FROM amazoncorretto:17-alpine as builder

RUN apk --no-cache add binutils

WORKDIR /src

COPY . .

RUN ./gradlew build

WORKDIR /src/build/libs

RUN jdeps \
      -q \
      -R \
      --ignore-missing-deps \
      --print-module-deps \
      --multi-release=17 \
      -cp dependencies/* \
      *.jar \
      > modules.txt

RUN jlink \
      --no-header-files \
      --no-man-pages \
      --strip-debug \
      --add-modules="jdk.crypto.ec,$(cat modules.txt)" \
      --compress=2 \
      --output=/opt/java

FROM alpine

USER nobody:nobody

COPY --from=builder --chown=nobody:nobody /opt/java /opt/java

WORKDIR /app

COPY --from=builder --chown=nobody:nobody /src/build/libs/*.jar app.jar

ENTRYPOINT ["/opt/java/bin/java",  "-jar", "/app/app.jar"]

EXPOSE 8080
