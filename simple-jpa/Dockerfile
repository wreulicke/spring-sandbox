FROM openjdk:8-jdk-slim

WORKDIR /app
VOLUME /tmp
EXPOSE 8080
ARG jar_file
ADD ${jar_file} app.jar

ENV JAVA_OPTS -Xms512m -Xmx1g

CMD java -Djava.security.egd=file:/dev/./urandom -jar \
  -XX:+PrintGCDateStamps -verbose:gc -XX:+PrintGCDetails -Xloggc:/app/gc_%t_pid%p.log \
  -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/app/ \
  -XX:ErrorFile=/var/app/hs_err_pid%p.log \
  # -Dlogging.config=/logback-$ENVIRONMENT_TYPE.xml \
  -Dfile.encoding=UTF-8 \
  -Dsun.net.inetaddr.ttl=60 \
  /app/app.jar

