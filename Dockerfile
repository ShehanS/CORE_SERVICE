
FROM parrotstream/ubuntu-java:latest

MAINTAINER Shehan Shalinda <shehan.salinda92@gmail.com>

WORKDIR /app

COPY core_service-1.0-SNAPSHOT .

COPY core_service-1.0-SNAPSHOT/conf/application.conf .

ENTRYPOINT ["/app/core_service-1.0-SNAPSHOT/bin/core_service","-Dconfig.file=/app/application.conf","-Dhttp.port=9000","-J-Xms256M","-J-Xmx1G"]
