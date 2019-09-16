FROM java:8-jdk-alpine

COPY ./target/chatty-1.0-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch chatty-1.0-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar", "chatty-1.0-SNAPSHOT.jar"]

EXPOSE 8090