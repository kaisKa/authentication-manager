FROM openjdk:11
VOLUME /tmp
EXPOSE 7777
ARG JAR_FILE=target/authentication_manager-v1.0.jar
ADD ${JAR_FILE} authentication-manager.jar
ENTRYPOINT ["java","-jar","/authentication-manager.jar"]
