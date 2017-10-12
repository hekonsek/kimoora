FROM openjdk:8

MAINTAINER hekonsek@gmail.com

ADD maven/*.jar /app/kimoora-server-${project.version}.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/app/kimoora-server-${project.version}.jar"]