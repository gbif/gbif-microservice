gbif-microservice
=================

This project contains the basic functionality to wrap an existing web application into runnable jar file that embedded
a Jetty server that expose.

Building the project
=================
Execute "mvn clean package -U". A Maven profile is not needed.

Executing an application
=========================
An application that uses this project can be executed using the following named parameters:
  * -conf
       Path to the configuration file
    -containerName
       Container name, intended to store the Linux container name
    -externalAdminPort
       External admin port that maps the default httpPort, must be used then the
       Http is not visible out of the server, e.g: Linux containers.
    -externalPort
       External port that maps the default httpPort, must be used then the Http
       is not visible out of the server, e.g: Linux containers.
  * -host
       Application server or control host that runs the service instance
  * -httpAdminPort
       Http administration port
       Default: 0
  * -httpPort
       Http port
       Default: 0
  * -stopSecret
       Secret/password to stop the server
  * -timestamp
       Timestamp that identifies this service instance
    -zkHost
       Zookeeper ensemble to store the discover service information
    -zkPath
       Zookeeper path to store the discovery service information

* required parameters.

Using this project
==================
To enable this project in a web application follow the procedure described below:
 
1. Modifications to the project pom.xml file:
     1.1 Change the packaging to <packaging>jar</packaging>  and remove the maven-war-plugin.
     1.2 Add the following properties:
         <properties>
           <maven-shade-plugin.version>2.3</maven-shade-plugin.version>
           <gbif-microservice.version>0.1-SNAPSHOT</gbif-microservice.version>
         </properties>      
      1.3 Add the maven shade plugin, exclude any properties file that don't need in the output jar file (see comment below in the example):
         <plugin>
           <groupId>org.apache.maven.plugins</groupId>
           <artifactId>maven-shade-plugin</artifactId>
           <version>${maven-shade-plugin.version}</version>
           <executions>
             <execution>
               <phase>package</phase>
               <goals>
                 <goal>shade</goal>
               </goals>
               <configuration>
                 <createDependencyReducedPom>true</createDependencyReducedPom>
                 <transformers>
                   <transformer
                     implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
                   <transformer
                     implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                     <mainClass>org.gbif.ws.app.Application</mainClass>
                   </transformer>
                 </transformers>
                 <filters>
                   <filter>
                     <artifact>org.jruby:jruby-complete</artifact>
                     <excludes>
                       <exclude>org/joda/**</exclude>
                     </excludes>
                   </filter>
                   <filter>
                     <artifact>*:*</artifact>
                     <excludes>
                       <exclude>META-INF/*.SF</exclude>
                       <exclude>META-INF/*.DSA</exclude>
                       <exclude>META-INF/*.RSA</exclude>
                     </excludes>
                   </filter>
                   <filter>
                     <!--Exclude configuration file that are not required in the output jar file -->
                     <artifact>org.gbif.occurrence:occurrence-ws</artifact>
                     <excludes>
                       <exclude>occurrence.properties</exclude>
                     </excludes>
                   </filter>
                 </filters>
               </configuration>
             </execution>
           </executions>
         </plugin>
         
      1.4 Add the gbif-microservice dependency:
          <dependency>
            <groupId>org.gbif</groupId>
            <artifactId>gbif-microservice</artifactId>
            <version>${gbif-microservice.version}</version>
          </dependency>
      1.5 Modify the servlet api scope: some projects use the servlet api with 'provided' scope, that should be changed to 'compile':
          <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <scope>compile</scope>
          </dependency>
      1.6 Verify if the dependency 'gbif-common' is required (i.e. 'dependency:analyze' reports it as a used undeclared dependency):
          <dependency>
            <groupId>org.gbif</groupId>
            <artifactId>gbif-commons</artifactId>
            <scope>latest version!</scope>
          </dependency>
          Note: this dependency is used to load properties file from an absolute path.
         
 2. Modify the GbifListener subclass:
    2.1 Add a variable that keeps the default configuration file name, that variables is created by the org.gbif.ws.app.Application class:       
    2.2 Use this variable in the constructor of GbifListener subclass.
    Optionally the utility class ConfUtils can be used to achieve the steps described above, e.g.:
        
    public class TestWsModule extends GbifServletListener {

      public ChecklistBankWsModule() throws IOException {
        super(PropertiesUtil.readFromFile(ConfUtils.getAppConfFile("myapp.properties")), "org.gbif.ws", ...);
      }

 3. If the ws-client of the project that is being modified contains a reference to this project with the classifier 'classes', remove it:
    <dependency>
      <groupId>org.gbif</groupId>
      <artifactId>server-ws</artifactId>
      <version>${project.version}</version>
      <classifier>classes</classifier>
      <scope>test</scope>
    </dependency>
    Must be changed to:
    <dependency>
      <groupId>org.gbif</groupId>
      <artifactId>server-ws</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>
