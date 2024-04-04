FROM gradle:7.4.1-jdk17 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle assemble --no-daemon

#For importing Amazon RDS keystores to JDK cacerts.
FROM openjdk:17.0.2 as sslcerts
RUN mkdir -p /opt/cert
COPY --from=build /home/gradle/src/config/scripts/import-rds-cert.sh /opt/cert/import-rds-cert.sh
RUN chmod 777 /opt/cert/import-rds-cert.sh
RUN /opt/cert/import-rds-cert.sh

FROM openjdk:17.0.2

ENV APP_NAME quickstart-springboot-template

# create the app directory
RUN mkdir -p /opt/$APP_NAME

# set the working directory
WORKDIR /opt/$APP_NAME

COPY --from=build /home/gradle/src/build/libs/quickstart-springboot-template-0.0.1-SNAPSHOT.jar /opt/$APP_NAME/quickstart-springboot-template.jar

#For importing Amazon RDS keystores to JDK cacerts.
COPY --from=sslcerts /etc/pki/ca-trust/extracted/java/cacerts /etc/pki/ca-trust/extracted/java/cacerts


EXPOSE 8080

ENTRYPOINT ["java","-jar","quickstart-springboot-template.jar"]
