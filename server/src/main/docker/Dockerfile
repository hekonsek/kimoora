FROM fedora:26

MAINTAINER hekonsek@gmail.com

RUN yum install -y https://download.docker.com/linux/fedora/25/x86_64/stable/Packages/docker-ce-17.06.0.ce-1.fc25.x86_64.rpm
RUN yum install -y java-1.8.0-openjdk

ADD maven/*.jar /app/kimoora-server-${project.version}.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/app/kimoora-server-${project.version}.jar"]