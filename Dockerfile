
FROM parrotstream/ubuntu-java:latest

MAINTAINER Shehan Shalinda <shehan.salinda92@gmail.com>

WORKDIR /app

COPY / .

COPY /conf/application.conf .

ENTRYPOINT ["/app/bin/core_service","-Dconfig.file=/app/application.conf","-Dhttp.port=9000","-J-Xms256M","-J-Xmx1G"]
