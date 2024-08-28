FROM openjdk:8-jdk-alpine
RUN mkdir -p /root/.ssh

COPY template-manager-services-${project.version}.jar /opt/ericsson/template_manager/template-manager-services-${project.version}.jar

ENV JAVA_OPTS="-Xmx2g -verbose:gc -Xloggc:/logs/gc.log"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/ericsson/template_manager/template-manager-services-${project.version}.jar  --spring.profiles.active=container" ]