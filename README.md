## 5.5 Automating common application deployment

### 5.5.1 Overview

While there are numerous existing solutions for automating the deployment of an application in almost every programming language to almost every server technology, there seems to always be a reason why you can’t. Those reasons range from disallowing the usage of open source solutions, to the thing you are supposed to use being currently broken and won’t be fixed anytime soon. The result is that you are stuck having to automate within the confines of the current rules, and restrictions, and infrastructure.

The need for automation starts this decision point: do you wait for a new technology to be approved or for the existing process to be fixed, or do you go route of doing it yourself? The resulting decision is going to be a reflection based on how long both options will take, and if you have the skill to do it yourself. The second point of reflection I can answer for you, as you do have the skill to custom automate deployment because deployment is inherently easy and simple (unless you have made a mess of things).

 

Deployment consists of the following steps:

·   Build thing

·   Upload thing to server

·   Run command on server to shutdown old instance of thing

·   Run commands to backup old thing and move new thing into appropriate place

·   Run command to startup thing

·   Check to see if thing is running

 

That is a basic deployment, and if you can do it at the command-line, you can automate it. The objective of deployment automation is to further eliminate human error, and ultimately to move towards adopting a higher level on the Continuous Delivery Maturity Model. Continuous delivery (CD) is a software engineering approach in which teams produce software in short cycles, ensuring that the software can be reliably released at any time (https://en.wikipedia.org/wiki/Continuous_delivery). 

If you do not want to do some level of Continuous Delivery, including all of the important activities to ensure quality through static analysis, test automation, and others, then stop here. Delivering more broken stuff faster and more often, is going to cause you more problems. The recommendation is to take a step back and address known development process issues, otherwise as the creator of the automated deployment process you will find yourself constantly on support bridges at 3 AM.

The purpose of this project is to demonstrate how to automate the delivery of an executable Jar file to a remote server via SCP and SSH. This process covers the basics of automated deployment through a Gradle plugin, when all other options have failed or are unavailable. As long as you have access to the target server with the ability to run the application, it is possible to automate deployment.

### 5.5.2 Spring Boot

Spring Boot aims to make it easy to create Spring-powered, production-grade applications and services with minimum fuss. It takes an opinionated view of the Spring platform so that new and existing users can quickly get to the bits they need. You can use it to create stand-alone Java applications that can be started using ‘java -jar’ or more traditional WAR deployments.

\-    https://spring.io/blog/2013/08/06/spring-boot-simplifying-spring-for-everyon

 

For demonstration purposes, a Sprint Boot based Groovy application was created. That application runs on port 8080, and exposes /health endpoint that lets you know if the application is working correctly or not. The for the application is also Gradle-based and makes use of some Spring Boot specific plugins.

#### build.gradle

```groovy
buildscript {
  ext {
    springBootVersion = '1.5.12.RELEASE'
  }
  repositories {
	jcenter()
  }
  dependencies {
classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
  }
}

apply plugin: 'groovy'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

// GROOVY
group = 'com.blogspot.jvalentino.boot'
version = '0.0.1'
archivesBaseName = 'demo'
sourceCompatibility = 1.8

repositories {
    jcenter()
    mavenCentral()
}

configurations.all {
    exclude group: "ch.qos.logback", module: "logback-classic"
    exclude group: "org.slf4j", module: "log4j-over-slf4j"
}

dependencies {
    
    compile 'org.codehaus.groovy:groovy-all:2.4.12'
    compile 'org.springframework.boot:spring-boot-starter-web'
    compile('org.springframework.boot:spring-boot-starter-actuator')

    testCompile 'org.spockframework:spock-core:1.1-groovy-2.4'
    testCompile('org.springframework.boot:spring-boot-starter-test')
}


```

**Line 3: Spring Boot Version**

In Spring Boot projects, it is customary to declare the version of Spring Boot in use globally, so it can be referenced if needed. This value is also picked up by the dependency management plugin to know which version to pull with boot packages.

 

**Line 9: Spring Boot Gradle Plugins**

The classpath dependency on the Spring Boot Gradle plugins.

 

**Lines 26-29: Exclusions**

Other version logging libraries are transitive dependency, and SLF4j will specifically stop the application from starting when this is detected. This is why other logging technologies were excluded.

 

**Lines 34-38: Spring**

Spring libraries for compile and testing. The web library provides web server capabilities, while the actuator library provides the /health endpoint (and others).

 

#### src/main/groovy/com/blogspot/jvalentino/boot/demo/DemoApplication.groovy

```groovy
package com.blogspot.jvalentino.boot.demo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class DemoApplication {

	static void main(String[] args) {
		SpringApplication.run DemoApplication, args
	}
}


```

The only code in the application is a main class annotated to be a Spring Boot Application.

#### src/main/resources/application.properties

```properties
management.endpoint.shutdown.enabled=true
management.security.enabled=false
endpoints.health.sensitive=false
endpoints.beans.sensitive=false
endpoints.shutdown.enabled=true

```

To provide access to the /health endpoint and to allow the /shutdown endpoint to be used to shut down the app, several properties must be set on the Actuator.

#### Running it

```bash
$ java -jar demo-0.0.1.jar 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v1.5.12.RELEASE)

Apr 26, 2018 4:08:27 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service [Tomcat]
Apr 26, 2018 4:08:27 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet Engine: Apache Tomcat/8.5.29
Apr 26, 2018 4:08:27 PM org.apache.catalina.core.ApplicationContext log
INFO: Initializing Spring embedded WebApplicationContext

```

 

Running it from the command-line will show the Spring logo If successful, as well as logging output indicating the embedded Tomcat is being used for the web container. This only requires that the application JAR was built using “gradlew build”.

#### http://localhost:8080/health

```json
{  
   "status":"UP",
   "diskSpace":{  
      "status":"UP",
      "total":374953877504,
      "free":30237024256,
      "threshold":10485760
   }
}

```

Once the server is running, the /health endpoint from the Actuator will show the application status as UP, meaning everything is working.

### 5.5.3 Using the command-line

Now that we know how to both build and run the web service application, the next step is to figure out how to deploy it.

#### Directory structure

```bash
$ ssh testuser@localhost "mkdir -p /Users/testuser/demo/deploy || true"
```

You can’t deploy to a location on the file system that doesn’t exist, so a first recommended step is to remotely create the directory structure if needed. This SSH command does a mkdir that includes parent directories, then ignores any failures encountered.

 

#### SCP

```bash
$ scp build/libs/demo-0.0.1.jar testuser@localhost:/Users/testuser/demo/deploy
```

SCP is then used to transfer the built Spring Boot jar from the build/libs directory to the remote server.

#### Launching the jar remotely

```bash
$ ssh testuser@localhost "cd /Users/testuser/demo/deploy; nohup java -jar -Dspring.profiles.active=dev demo-*.jar >/dev/null 2>&1 &"
```

Launching the Jar remotely is more complicated, as you need it to be launched as another process. If you don’t, the session will remain open until the application is closed, and the process is suspended. The command “nohup” in combination with “2>&1 &” is used to execute the jar as a no process will suppressing all standard and error output.

#### Verify that it is running

```bash
$ ps -ef | grep java
    503 17443     1   0  4:21PM ??         0:16.06 /usr/bin/java -jar -Dspring.profiles.active=dev demo-0.0.1.jar

$ curl http://localhost:8080/health
{"status":"UP","diskSpace":{"status":"UP","total":374953877504,"free":30186467328,"threshold":10485760}}

```

The process can be verified is running using two methods. The first requires being on the remote system and doing a process grep to get the PID of the running process. The second method accesses the /health endpoint using curl to check the application status.

### 5.5.4 Creating a Gradle plugin for deployment automation

With the steps for building the application, deploying it to a remote server, running it, and making sure it is running known, we can now handle writing a Gradle plugin to automate these steps. 

 

#### Target Deployment Configuration

```yaml
dev:
 host: localhost
 username: testuser
 password: 'OsOeKK7]x!O7q21#n;jUeQ8S&BVoEYv'
 targetDir: /Users/testuser/demo
 deployFile: build/libs/demo-*.jar
 shutdownUrl: http://localhost:8080/shutdown
 healthUrl: http://localhost:8080/health
 startCommand: java -jar -Dspring.profiles.active=dev demo-*.jar

```

It is reommended to externlize the deployment specifics, so it can be applied to more than one application. That confguration will need to contain the file to transfer, the command to start it up, and the endpoints for inspecting health and shutting it down. Note that credential it is reocmmended to use s keyfile instead of a password, and to externlize those credentials securely as session specific envitornment variables, depending on capabilities.

​      It is also recommended to make the confiuration suport mutliple environments. The reaosn is that you may be using different settings for diferent environments. The above configuration happens to be on YAML format, and allows the definition of any number of environment, with each containing different values.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/CustomDeployPlugin.groovy

```groovy
class CustomDeployPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create 'deploy', DeployExt
        project.task('load', type:LoadConfigTask)
        project.task('prepare', type:PrepareTask, dependsOn:'load')
        project.task('upload', type:UploadTask, dependsOn:'prepare')
        project.task('shutdown', type:ShutdownTask, dependsOn:'upload')
        project.task('move', type:MoveTask, dependsOn:'shutdown')
        project.task('startup', type:StartupTask, dependsOn:'move')
    }
}

```

The plugin was separated into task for each discrete operation, with each task being dependent on the previous:

·   **load** – Handles loading the configuration for the specific environment

·   **prepare** – Handles creating the remote directory structure if it does not already exist

·   **upload** – Handles upload the artifact to the remote server

·   **shutdown** – Handles shutting down the existing application, if it is running, using the /shutdown endpoint

·   **move** – Handles backing up the current artifact, and moving the uploaded one into the deployment directory

·   **startup** – Handles starting the application, and checking to make sure it comes up correctly



#### src/main/groovy/com/blogspot/jvalenitno/gradle/service/RemoteService.groovy

```groovy
class UrlService {

    UrlService instance = this
    
    String post(String urlString) {
        String text = null
        println "  \$ POST ${urlString}"
        URL baseUrl = new URL(urlString)
        String queryString = ''
        HttpURLConnection connection = baseUrl.openConnection()
        try {
            connection.with {
                doOutput = true
                requestMethod = 'POST'
                outputStream.withWriter { 
                    writer -> writer << queryString }
                text = content.text?.trim()
                println "  > ${text}"
            }
        } catch (e) {
            println "  > ${e.message}"
        }
        text
    }

```

**Lines 16-35: HTTP/HTTPS POST**

The purpose of this method is to make a POST request to the given URL, with the intention of this being used to invoke POST http://localhost:8080/shutdown to cleanly shutdown the Sprint Boot application.

 

```groovy
    Health getHealth(String url) {
        Health result = Health.NOT_RUNNING
        try {
            println "  \$ GET ${url}"
            String jsonText = new URL(url).text
            println "  > ${jsonText}"
            Map json = new JsonSlurper().parseText(jsonText)
            result = json.status == 'UP' ? Health.UP : Health.DOWN
        } catch (e) {
            println "  > ${e.message}"
        }

        result
    }

```

**Lines 37-50: Health determination**

This method is used for making an GET request to a /health URL, and deciding if the resulting application is UP, DOWN, or NOT_RUNNING.

```groovy
    void waitUntilHealth(Health starting, Health ending, long timeout,
            long sleepInterval, String url) throws TimeoutException {
        Health health = starting

        long currentWaitTime = 0

        while (health != ending) {
            println '- Checking health status...'
            health = instance.getHealth(url)
            println "- Result is Health.${health}"
            if (health != ending) {
                if (currentWaitTime > timeout) {
                    throw new TimeoutException("The desired response " + 
                        "could not be reached in ${timeout} ms")
                }
                currentWaitTime += sleepInterval
                instance.doSleep(sleepInterval)
            }
        }
    }
    
    void doSleep(long time) {
        sleep(time)
    }

```

**Lines 52-75: Waiting for /health state**

​      Once the application is shutdown, proceeding will first need to wait for the application if still running to no longer running. When the application is started up, processing will then need to wait for the application to be UP. Both these needs were abstracted into a single method, that continues to query the /health endpoint until the desired state is found. Also, if the state is not reached within a given amount of time to throw an exception, as we don’t want this to get stuck on a failure.

​      Sleeping was also moved into another method to use the strategy of self-reference mocking to better handle it in testing. This is because we don’t want the test to take N-seconds to run; we just want to know that the sleep method was called with the expected value the expected number of times.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/service/UrlService.groovy

```groovy
class RemoteService {

    Session generateSession(String username, String password,
            String host) {
        JSch jsch = new JSch()
        Session session = jsch.getSession(
                username, host, 22)
        session.password = password
        session.setConfig('StrictHostKeyChecking', 'no')
        session
    }

    String execute(String command, Session session) {
        ChannelExec channelExec = session.openChannel('exec')

        InputStream is = channelExec.inputStream

        println "  \$ ${command}"
        channelExec.command = command
        channelExec.connect()

        String text = is.text.trim()
        if (text != null && text != '') {
            println "  > ${text}"
        }

        channelExec.disconnect()

        text
    }

    void upload(File file, String toRemoteFile, Session session) {
        ChannelSftp sftp = session.openChannel('sftp')
        sftp.connect()
        file.withInputStream { 
            istream -> sftp.put(istream, toRemoteFile) 
        }
        sftp.disconnect()
    }
}

```

**Lines 15-23: Session generation**

The same as the SSH Example, session generation was abstracted into its own method.

 

**Lines 25-43: Remote command execution**

The same as the SSH Example, remote command execution is handled by opening a channel on the session, disconnecting, and returning the output if present.

 

**Lines 44-51: Upload**

The same library used for SSH can also be used for SFTP by opening an “sftp” channel. That channel then supports writing the file input stream to it to accomplish the upload.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/task/LoadConfigTask.groovy

```groovy
class LoadConfigTask extends DefaultTask {

    LoadConfigTask instance = this

    @TaskAction
    void perform() {
        Project p = instance.project

        DeployExt ext = p.extensions.findByType(DeployExt)

        ext.env = p.properties.env
        if (ext.env == null) {
            throw new GradleException('-Penv must be given')
        }

        println "- Loading for ${ext.env} from ${ext.config}"
        Yaml yaml = new Yaml()
        Map map = yaml.load(new FileInputStream(new File(ext.config)))

        Map envMap = map[ext.env]
        ext.username = envMap.username
        ext.password = envMap.password
        ext.host = envMap.host
        ext.targetDir = envMap.targetDir
        ext.deployFile = envMap.deployFile
        ext.shutdownUrl = envMap.shutdownUrl
        ext.healthUrl = envMap.healthUrl
        ext.startCommand = envMap.startCommand
    }
}

```

This task requires that the environment for the application be given as a properties, preferably as a command-line -P property, which is then used to pull from the YAML configuration file.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/task/PrepareTask.groovy

```groovy
class PrepareTask extends DefaultTask {

    PrepareTask instance = this
    RemoteService remoteService = new RemoteService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println "- Connecting using ${ext.username}@${ext.host}"
        ext.session = remoteService.generateSession(
                ext.username, ext.password, ext.host)
        ext.session.connect()

        remoteService.execute("mkdir -p ${ext.targetDir} || true", 
            ext.session)
        remoteService.execute("mkdir -p ${ext.targetDir}/backup || true",
             ext.session)
        remoteService.execute("mkdir -p ${ext.targetDir}/upload || true", 
            ext.session)
        remoteService.execute("mkdir -p ${ext.targetDir}/deploy || true",
             ext.session)
    }

```

This task prepares the file system by remotely creating the application directory, and subdirectories for backup, deployment, and upload if needed.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/task/UploadTask.groovy

```groovy
class UploadTask extends DefaultTask {

    UploadTask instance = this
    RemoteService remoteService = new RemoteService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        String deployFileName = ext.deployFile.split('/').last()
        String deployFromDir = ext.deployFile.replace(
            "/${deployFileName}", '')

        println "- Searching ${deployFromDir} for ${deployFileName}"

        FileTree fileTree = p.fileTree(deployFromDir) { 
            include deployFileName 
        }
        List<File> results = []
        fileTree.files.each { File f ->
            println "  * Found: ${f.name}"
            results.add(f)
        }

        if (results.size() != 1) {
            throw new GradleException(
                'There must be only 1 file match for upload')
        }

        File file = results.first()
        String targetFile = "${ext.targetDir}/upload/${file.name}"
        println "- Uploading ${file.name} to ${targetFile}"
        remoteService.upload(file, targetFile, ext.session)
    }
}

```

This task handles using the **FileTree** to find the jar file to be deployed in the build directory. Upon location of a single matching file, the **RemoteService** is used to handle uploading that file to the remote server.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/task/ShutdownTask.groovy

```groovy
class ShutdownTask extends DefaultTask {

    ShutdownTask instance = this
    UrlService urlService = new UrlService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println '- Attempting to nicely shutdown the app'
        urlService.post(ext.shutdownUrl)

        urlService.waitUntilHealth(
                Health.UP, Health.NOT_RUNNING,
                10_000L, 1_000L, ext.healthUrl)
    }

```

This task uses the **UrlService** to make a POST to the Spring Boot /shutdown endpoint. It then waits to continue for the application to not be running. This handles both cases where the application is not running, and is already running.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/task/MoveTask.groovy

```groovy
class MoveTask extends DefaultTask {

    MoveTask instance = this
    RemoteService remoteService = new RemoteService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println '- Deleting any current backups'
        remoteService.execute(
                "rm -f ${ext.targetDir}/backup/*.*", 
                ext.session)

        println '- Backing up the current deployment'
        remoteService.execute(
                "mv ${ext.targetDir}/deploy/*.* ${ext.targetDir}/backup", 
                ext.session)

        println '- Putting the current version in the deploy directory'
        remoteService.execute(
                "mv ${ext.targetDir}/upload/*.* ${ext.targetDir}/deploy", 
                ext.session)
    }
}

```

This task handles deleting any current backup files, moving the current deployment into backup, and then moving the uploaded Jar into deploy.

#### src/main/groovy/com/blogspot/jvalenitno/gradle/task/StartupTask.groovy

```groovy
class StartupTask extends DefaultTask {

    StartupTask instance = this
    RemoteService remoteService = new RemoteService()
    UrlService urlService = new UrlService()

    @TaskAction
    void perform() {
        Project p = instance.project
        DeployExt ext = p.extensions.findByType(DeployExt)

        println '- Starting up application'

        String command = "cd ${ext.targetDir}/deploy; "
        command += "nohup ${ext.startCommand} >/dev/null 2>&1 &"

        remoteService.execute(command, ext.session)

        urlService.waitUntilHealth(
                Health.NOT_RUNNING, Health.UP,
                30_000L, 5_000L, ext.healthUrl)
    }
}

```

This task handles running the command to startup the application within the deploy directory and does not in a way that starts it as a new process and suppresses the output. It then waits for the application /health endpoint to show as UP, or times out and fails the process.

### 5.5.5 Manually testing the Gradle plugin

#### Building the Jar

```bash
plugin-tests/local$ gradlew clean build

BUILD SUCCESSFUL

```

A clean build at the command-line will produce the Spring Boot jar under build/libs.

 

#### Deploying the Jar

```bash
plugin-tests/local$ ./gradlew --stacktrace -Penv=dev startup

> Task :load 
- Loading configuration for dev from deploy.yml

```

Specying the environment with the **startup** task will start the process. Starting with the **load** task, which pulls the values from deploy.yml for the **dev** environment.

 

```bash
> Task :prepare 
- Connecting using testuser@localhost
  $ mkdir -p /Users/testuser/demo || true
  $ mkdir -p /Users/testuser/demo/backup || true
  $ mkdir -p /Users/testuser/demo/upload || true
  $ mkdir -p /Users/testuser/demo/deploy || true

```

The **prepare** task handles creating the directory structure on the remote server if needed.

 

```bash
> Task :upload 
- Searching build/libs for demo-*.jar
  * Found: demo-0.0.1.jar
- Uploading demo-0.0.1.jar to /Users/testuser/demo/upload/demo-0.0.1.jar

```

The **upload** task uses SFTP to transfer the Spring Boot jar to the upload directory on the remote server.

```bash
> Task :shutdown 
- Attempting to nicely shutdown the app
  $ POST http://localhost:8080/shutdown
  > {"message":"Shutting down, bye..."}
- Checking health status...
  $ GET http://localhost:8080/health
  > {"status":"UP","diskSpace":{"status":"UP","total":374953877504,"free":29932273664,"threshold":10485760}}
- Result is Health.UP
- Checking health status...
  $ GET http://localhost:8080/health
  > Connection refused
- Result is Health.NOT_RUNNING

```

The **shutdown** task makes a POST to the /shutdown endpoint, and then waits for the application to no longer be running.

```bash
> Task :move 
- Deleting any current backups
  $ rm -f /Users/testuser/demo/backup/*.*
- Backing up the current deployment
  $ mv /Users/testuser/demo/deploy/*.* /Users/testuser/demo/backup
- Putting the current version in the deploy directory
  $ mv /Users/testuser/demo/upload/*.* /Users/testuser/demo/deploy

```

The **move** task handles transferring the assets from deploy into backup, and then upload unto deploy.

```bash
> Task :startup 
- Starting up application
  $ cd /Users/testuser/demo/deploy; nohup java -jar -Dspring.profiles.active=dev demo-*.jar >/dev/null 2>&1 &
- Checking health status...
  $ GET http://localhost:8080/health
  > Connection refused
- Result is Health.NOT_RUNNING
- Checking health status...
  $ GET http://localhost:8080/health
  > Connection refused
- Result is Health.NOT_RUNNING
- Checking health status...
  $ GET http://localhost:8080/health
  > {"status":"UP","diskSpace":{"status":"UP","total":374953877504,"free":30062567424,"threshold":10485760}}
- Result is Health.UP


BUILD SUCCESSFUL

```

The **startup** task runs the command to startup the application in a new process and waits for the /health endpoint to respond.

### 5.5.6 Unit Testing

The details of how to accomplish the unit testing of this functionality is covered primarily in the SSH and Command-Line examples.

#### src/test/groovy/com/blogspot/jvalenitno/gradle/service/UrlServiceTestSpec.groovy

```bash
class UrlServiceTestSpec extends Specification {

    UrlService service
    
    def setup() {
        service = new UrlService()
        service.instance = Mock(UrlService)
        
    }
    
    void "test post"() {
        given:
        String urlString = "http://foo"
        
        and:
        HttpURLConnection conn = Mock(HttpURLConnection)
        URL.class.metaClass.openConnection = {
            conn
        }
        OutputStream os = Mock(OutputStream)
        
        when:
        String text = service.post(urlString)
        
        then:
        1 * conn.setDoOutput(true)
        1 * conn.setRequestMethod('POST')
        1 * conn.getOutputStream() >> os
        1 * conn.content >> ['text':'blah']
        
        and:
        text == 'blah'
    }

```

**Lines 12-16: Standard setup**

The self-reference mock is used to handle mocking the sleep without actually making the application wait. Another approach would have been to wrap the sleep in another class.

 

**Lines 18-40: Testing POST**

Just as in the HTTP Example, metaprogramming is used to override the default behavior or the URL class, as it cannot be mocked. The **openConnection** method is overridden to return a mocked instance of a **HttpURLConnection**, so it can later be used to assert its POST mode and mock the return result.

```groovy
    @Unroll
    void "Test getHealth for #h"() {
        given:
        String url = 'http://blah'
        
        and:
        URL.class.metaClass.getText = {
            json   
        }
        
        when:
        Health result = service.getHealth(url)
        
        then:
        result == h
        
        where:
        json                    || h
        '{ "status":"UP" }'     || Health.UP
        '{ "status":"DOWN" }'   || Health.DOWN
        '{ "st'                 || Health.NOT_RUNNING
    }

```

**Lines 43-63: Testing health**

The strategy for URL metaprogramming is used to drive the text returned by URLs, to assert the behavior in the cases of up, down, and not running.

```groovy
  void "test waitUntilHealth"() {
        given:
        Health starting = Health.NOT_RUNNING
        Health ending = Health.UP
        long timeout = 3_000L
        long sleepInterval = 1_000L
        String url = "'http://foo"
        
        when:
        service.waitUntilHealth(
            starting, ending, timeout, sleepInterval, url)
        
        then:
        1 * service.instance.getHealth(url) >> Health.NOT_RUNNING
        1 * service.instance.doSleep(sleepInterval)
        
        and:
        1 * service.instance.getHealth(url) >> Health.UP
    }

```

**Lines 65-83: Wait until**

​      This test relies on the self-reference mock to handle sleeping, without requiring the execution to actually pause. It specifically returns the result of NOT_RUNNING, which causes one more iteration to execution after a sleep. When the health of UP is returned, the method terminates.

