### Instructions to build and run the application

## Technical requirements

- Java 21
- Node v20

## Build the backend app

1. navigate to /task-manager

2. open a terminal and run `mvn package -DskipTests`

## Run the backend app

1. navigate to /task-manager

2. open a terminal and run `java -jar target/task-manager-0.0.1-SNAPSHOT.jar`

## Build the frontend app

0. If the backend does not listen on port 8080 (e.g. when port 8080 is already in use), modify the webapp's `environment.ts` file to the correct api path.

1. navigate to /task-manager-client/task-manager

2. open a terminal and run `npm i`

## Run the frontend app

1. navigate to /task-manager-client/task-manager

2. open a terminal and run `npm start`

####  (( the documentation is in the 'documentation.md' file ))