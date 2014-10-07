package org.gbif.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

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
  //Used for the admin connector only
  private ThreadPool threadPool;
  //The same algorithm used by the jetty SelectorChannelConnector
  private int maxThreadPoolSize = Math.max(1, (Runtime.getRuntime().availableProcessors() + 3) / 4);

  /**
   * Creates an instance of the admin connector.
   * The default values for this instance are:
   * - name:  admin
   * - port: 8081
   * - threadPool: QueuedThreadPool(maxThreadPoolSize)
   */
  public static HttpConnectorFactory admin() {
    final HttpConnectorFactory httpConnectorFactory = new HttpConnectorFactory();
    httpConnectorFactory.port = 8081;  //default admin port
    httpConnectorFactory.name = ADMIN_CONNECTOR_NAME;
    httpConnectorFactory.threadPool = new QueuedThreadPool(httpConnectorFactory.maxThreadPoolSize);
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
  public static HttpConnectorFactory application() {
    final HttpConnectorFactory httpConnectorFactory = new HttpConnectorFactory();
    httpConnectorFactory.name = APP_CONNECTOR_NAME;
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

  public ThreadPool getThreadPool() {
    return threadPool;
  }

  public void setThreadPool(ThreadPool threadPool) {
    this.threadPool = threadPool;
  }

  public int getMaxThreadPoolSize() {
    return maxThreadPoolSize;
  }

  public void setMaxThreadPoolSize(int maxThreadPoolSize) {
    this.maxThreadPoolSize = maxThreadPoolSize;
  }

  /**
   * Builds an instance of a Connector using the values provider by the HttpConnectorFactory instance.
   */
  public Connector build() {
    final SelectChannelConnector httpConnector = new SelectChannelConnector();
    httpConnector.setPort(port);
    httpConnector.setMaxIdleTime(maxRequestHeaderSize);
    httpConnector.setRequestHeaderSize(idleTimeout);
    httpConnector.setName(name);
    if (threadPool != null) { //thread pools are set for the admin connector only
      httpConnector.setThreadPool(threadPool);
    }
    return httpConnector;
  }

}
