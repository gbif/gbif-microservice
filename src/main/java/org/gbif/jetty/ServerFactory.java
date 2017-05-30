package org.gbif.jetty;

import org.gbif.discovery.conf.ServiceConfiguration;
import org.gbif.ws.discovery.lifecycle.DiscoveryLifeCycle;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * Utility class to generate instances of Jetty Servers.
 * The default values for the instance are:
 * -  gracefulShutdown period: 1 second.
 * - stopAtShutdown: true.
 */
public class ServerFactory {

  //Graceful shutdown timeout
  private int gracefulShutdown = 1000; //1 second

  //Shutdown the server when it's stopped
  private boolean stopAtShutdown = true;

  public int getGracefulShutdown() {
    return gracefulShutdown;
  }

  public void setGracefulShutdown(int gracefulShutdown) {
    this.gracefulShutdown = gracefulShutdown;
  }

  public boolean isStopAtShutdown() {
    return stopAtShutdown;
  }

  public void setStopAtShutdown(boolean stopAtShutdown) {
    this.stopAtShutdown = stopAtShutdown;
  }

  /**
   * Builds a Jetty Server instance using the configuration class.
   * From the configuration class the following fields are used:
   * - stopSecret: stop password used by the StopHandler.
   * - Http connectors definition: application and admin
   * - Contexts: default web application and stop context.
   * If the configuration instance isDiscoverable registers a DiscoveryLifeCycle that handles the discovery process.
   */
  public Server build(ServiceConfiguration configuration) {
    Server server = new Server();
    server.setConnectors(buildConnectors(configuration,server));
    server.setStopTimeout(gracefulShutdown);
    server.setHandler(buildContexts(server, configuration.getStopSecret()));
    if (configuration.isDiscoverable()) { //Register the discovery lifecycle
      server.addLifeCycleListener(new DiscoveryLifeCycle(configuration));
    }
    return server;
  }

  /**
   * Builds the admin and application connectors.
   */
  private static Connector[] buildConnectors(ServiceConfiguration configuration, Server server) {
    final HttpConnectorFactory appConnectorFactory = HttpConnectorFactory.application(server);
    appConnectorFactory.setPort(configuration.getHttpPort());
    final HttpConnectorFactory adminConnectorFactory = HttpConnectorFactory.admin(server);
    adminConnectorFactory.setPort(configuration.getHttpAdminPort());
    return new Connector[] {appConnectorFactory.build(), adminConnectorFactory.build()};
  }

  /**
   * Builds the web application and stop contexts.
   */
  private static ContextHandlerCollection buildContexts(Server server, String secret) {
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] {ContextFactory.buildApplicationContext(),
      ContextFactory.buildAdminContext(server, secret)});
    return contexts;
  }
}
