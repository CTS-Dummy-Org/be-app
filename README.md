# quickstart-springboot-documentdb-helloworld-api

Services related to DocumentDB HelloWorld Api.

## Prerequisites

Before going further, ensure you have the following installed:

- JDK 17
  - Setup:
    - Open JDK: https://jdk.java.net/17/
    - (For deployment, we prefer Open JDK.)

```
java -version
openjdk version "17.0.2" 2022-01-18
OpenJDK Runtime Environment (build 17.0.2+8-86)
OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)
```

- Gradle
  - Reference: https://spring.io/guides/gs/gradle/

```
gradle --version
------------------------------------------------------------
Gradle 7.2
------------------------------------------------------------

Build time:   2021-08-17 09:59:03 UTC
Revision:     a773786b58bb28710e3dc96c4d1a7063628952ad

Kotlin:       1.5.21
Groovy:       3.0.8
Ant:          Apache Ant(TM) version 1.10.9 compiled on September 27 2020
JVM:          17.0.2 (Oracle Corporation 17.0.2+8-86)
OS:           Windows 10 10.0 amd64
```

- git
  - Setup: https://help.github.com/en/articles/set-up-git

```
git --version
git version 2.33.0.windows.1
```

Notes: The git version is not critical, just try to use a recent version.

- MongoDB

```
db version v4.4.2
Build Info: {
    "version": "4.4.2",
    "gitVersion": "15e73dc5738d2278b688f8929aee605fe4279b0e",
    "modules": [],
    "allocator": "tcmalloc",
    "environment": {
        "distmod": "windows",
        "distarch": "x86_64",
        "target_arch": "x86_64"
    }
}
```

- MongoDB Compass

```
Version 1.32.6
```

## Initial Setup

## Gradle Installation

Gradle has multiple options for installation.

- **First Best Option**

  The first option should be to use the gradle wrapper. (https://docs.gradle.org/current/userguide/gradle_wrapper.html)

  This embeds a version of gradle in the project.

  ```
  If your existing Gradle-based build uses the Gradle Wrapper, you can easily upgrade by running the wrapper task, specifying the desired Gradle version:

  $ ./gradlew wrapper --gradle-version=5.3.1 --distribution-type=bin
  ```

- **Second Option**

  It's handy to install gradle system wide as well - this is a personal preference as the wrapper can handle everything, sometimes it's nice to have another (latest) version ever present on the command line.

  Installing Gradle: https://gradle.org/install/

  Short Version:

  ![image](https://user-images.githubusercontent.com/91141626/165732554-0d2e8c7f-8d91-43e0-ac97-60cd316aff1a.png)

## Gradle Configuration
In this project, we specify Maven Central directly in the `build.gradle` file.

Here’s how you can do it:

```
repositories {
    mavenCentral()
}
```

This configuration will allow Gradle to download dependencies directly from Maven Central, which is a free and widely used repository for Java and other open-source libraries.

With this setup, Gradle should be able to pull artifacts directly from Maven Central.

## Intellij Checkstyle Setup

https://cognizantproducts.atlassian.net/wiki/spaces/DAM/pages/6881349/Intellij+Setup

## Environment Variables

## Values for Env Variables

Values can be downloaded from asset details page in **Quickstart Application**

## Setup for Env in Intellij

Refer **.env.example** file to see all the environment variables you need to setup.

- Step 1: Click on Add Configuration...
  ![image](https://user-images.githubusercontent.com/91141626/165715870-9444c11e-c54b-40fc-9623-39eb8f65f3d2.png)

- Step 2: Click on Add new run Configuration and select gradle
  ![image](https://user-images.githubusercontent.com/91141626/165716072-17252734-cae3-4fb4-a417-685b2a0070e4.png)

- Step 3: In Tasks and Argument write bootRun and click the document symbol
  ![image](https://user-images.githubusercontent.com/91141626/165715526-29146b37-d07d-4690-871a-52f4b1b32685.png)

- Step 4: Click the plus symbol and Add the required environments here from .env.example
  ![image](https://user-images.githubusercontent.com/91141626/165716441-5ff7ee49-c399-4eda-a9bd-81422af004ff.png)

## Build Features

Steps to build and run the application

Run the test cases:

```
./gradlew test
```

Run the checkstyle check:

```
./gradlew checkstyleMain

./gradlew checkstyleTest
```

Build your artifacts:

```
./gradlew assemble
```

assemble will run the build and any tasks that have been linked except test.

Run your project:

```
Step1: ./gradlew bootRun
```

bootRun will result in gradle launching your project.

To run code quality checks:

```
./gradlew test check
```

Using the test task will run your unit tests. The check task will run checkstyle, spotbugs, and jacoco tasks.

Report output can be seen at:

```
build/reports/checkstyle/main.html
build/reports/checkstyle/test.html
build/reports/jacoco/test/html/index.html
build/reports/spotbugs/main/spotbugs.html
build/reports/spotbugs/test/spotbugs.html
```

## Code style

- You will need to install the IntelliJ Idea plugin `CheckStyle-IDEA`
- After the IntelliJ IDEA restart you need to install the configuration file from `config/checkstyle/checkstyle.xml`
- Replace the `SuppressionFilter` section's value in the checkstyle.xml as below

  ```
  value="${config_loc}/suppressions.xml

  ```

- Go to IntelliJ IDEA -> Preferences -> Other Settings -> Checkstyle
- Under `Configuration file` click the `+` and fill in the form as follows:

  - `Description` is `Cognizant Checks`
  - In the "Use a local Checkstyle file" section, select the provided config file:
    ```
    config/checkstyle/checkstyle.xml
    ```
  - Click the checkbox `Store relative to project location`
  - Click `Next` and the configuration file will be validated
  - Add config_loc as  
    `rootDir/config/checkstyle`
  - Click `Finish`

- Checkstyle Intellij Shortcuts
  ```
  Reformat Code - ctrl+alt+l
  organise / rearrange import -   ctrl+alt+o
  ```
- Auto Import

```
  Settings → Editor → General → Auto Import
```

## Did it work?

- Go to http://localhost:8090/actuator/health

```
{"status":"UP"}
```

## Run Checkmarx, BlackDuck Scan & Sonar Scan

Branch starting with the name /sast, /oss and /sonar will perform Checkmarx,BlackDuck Scan and Sonar Scan. To perform the scans changes need to be made in .circleci/config.yml.

- Start the branch name with 'sast' to initiate Checkmarx Scan.

```
- /sast\/.*/          # eg: sast/1, sast/prod
```

- Start the branch name with 'oss' to initiate BlackDuck Scan.

```
- /oss\/.*/           # eg: oss/1, oss/prod
```

- Start the branch name with 'sonar' to initiate Sonar Scan.

```
- /sonar\/.*/          # eg: sonar/1, sonar/prod
```
