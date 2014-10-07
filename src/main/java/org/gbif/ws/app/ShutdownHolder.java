package org.gbif.ws.app;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a context to run a Shutdownhook of the Jetty server and the running Java application.
 */
public class ShutdownHolder {

  private static final Logger LOG = LoggerFactory.getLogger(ShutdownHolder.class);

  //Server to be stopped
  private final Server server;

  /**
   * Utility method that stops a Jetty server.
   */
  public static void stopServer(Server server) {
    if (server.isStarted() || server.isRunning()) {
      try {
        LOG.info("Shutting down Jetty...");
        server.stop();
        LOG.info("Jetty has stopped.");
      } catch (Exception ex) {
        LOG.error("Error when stopping Jetty: " + ex.getMessage(), ex);
      }
    }
  }

  /**
   * Creates a Shutdown holder for the server parameter.
   */
  public ShutdownHolder(Server server) {
    this.server = server;
    //Registers the shutdown hook
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
  }

  /**
   * Executes the shutdown process.
   */
  public void shutdown() {
    stopServer(server);
  }
}
