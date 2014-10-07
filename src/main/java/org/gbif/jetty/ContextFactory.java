package org.gbif.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Utility class to generate the default connectors of an application.
 */
public class ContextFactory {

  private static final String WEB_DIR = "src/main/webapp/";
  private static final String DESCRIPTOR_PATH = WEB_DIR + "WEB-INF/web.xml";
  private static final String ROOT_CONTEXT = "/";
  private static final String STOP_CONTEXT = "/stop";

  /**
   * Builds an WebAppContext that follows the standard Maven web application.
   * - The resource base points to  "src/main/webapp/"
   * - The application descriptor must be in "src/main/webapp/WEB-INF/web.xml"
   * - The root context is "/"
   * - The context will be attached to the application connector.
   */
  public static ContextHandler buildApplicationContext() {
    final WebAppContext root = new WebAppContext();
    root.setContextPath(ROOT_CONTEXT);
    root.setDescriptor(DESCRIPTOR_PATH);
    root.setResourceBase(WEB_DIR);
    root.setParentLoaderPriority(true);
    root.setConnectorNames(new String[] {HttpConnectorFactory.APP_CONNECTOR_NAME});
    return root;
  }

  /**
   * Creates a ContextHandler with a handler to stop the application gracefully.
   * The context will be attached to the admin connector and accessible at the context "/stop".
   */
  public static ContextHandler buildStopContext(Server server, String secret) {
    ContextHandler stopContext = new ContextHandler();
    stopContext.setContextPath(STOP_CONTEXT);
    stopContext.setHandler(new StopHandler(server, secret));
    stopContext.setConnectorNames(new String[] {HttpConnectorFactory.ADMIN_CONNECTOR_NAME});
    return stopContext;
  }

  /**
   * Default private constructor.
   * Utility classes don't expose constructors.
   */
  private ContextFactory() {
    //do nothing
  }

}
