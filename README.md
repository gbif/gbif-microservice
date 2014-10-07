gbif-microservice
=================

This project contains the basic functionality to wrap an existing web application into runnable jar file that embedded
a Jetty server that expose.

Build the project
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

*: marks the required parameters.
