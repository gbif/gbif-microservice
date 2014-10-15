package org.gbif.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Factory class that provides instances of the Connectors used by an application.
 * Contains factory methods for the default connectors: application and admin.
 */
public class HttpConnectorFactory {

  public static final String APP_CONNECTOR_NAME = "application";
  public static final String ADMIN_CONNECTOR_NAME = "admin";
  private int maxRequestHeaderSize = 8192; //8 Kilobytes
  private int idleTimeout = 30000; //30 seconds
  //Http port
  private int port = 8080;  //default http port
  //Connector name
  private String name;

  //Jetty server
  private Server server;

  /**
   * Creates an instance of the admin connector.
   * The default values for this instance are:
   * - name:  admin
   * - port: 8081
   * - threadPool: QueuedThreadPool(maxThreadPoolSize)
   */
  public static HttpConnectorFactory admin(Server server) {
    final HttpConnectorFactory httpConnectorFactory = new HttpConnectorFactory();
    httpConnectorFactory.port = 8081;  //default admin port
    httpConnectorFactory.name = ADMIN_CONNECTOR_NAME;
    httpConnectorFactory.server = server;
    return httpConnectorFactory;
  }

  /**
   * Creates an instance of the application connector.
   * The instance returned is a SelectorChannelConnector which by default has a threadPool implementation.
   * The default values for this instance are:
   * - name:  application
   * - port: 8080
   * - threadPool: none
   */
  public static HttpConnectorFactory application(Server server) {
    final HttpConnectorFactory httpConnectorFactory = new HttpConnectorFactory();
    httpConnectorFactory.name = APP_CONNECTOR_NAME;
    httpConnectorFactory.server = server;
    return httpConnectorFactory;
  }

  public int getMaxRequestHeaderSize() {
    return maxRequestHeaderSize;
  }

  public void setMaxRequestHeaderSize(int maxRequestHeaderSize) {
    this.maxRequestHeaderSize = maxRequestHeaderSize;
  }

  public int getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(int idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  /**
   * Builds an instance of a Connector using the values provider by the HttpConnectorFactory instance.
   */
  public Connector build() {
    final ServerConnector httpConnector = new ServerConnector(server);
    httpConnector.setPort(port);
    httpConnector.setIdleTimeout(idleTimeout);
    httpConnector.setName(name);
    return httpConnector;
  }

}
