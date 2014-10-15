package org.gbif.jetty;

import java.net.URISyntaxException;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Utility class to generate the default connectors of an application.
 */
public class ContextFactory {

  private static final String WEB_DIR = "webapp";
  private static final String DESCRIPTOR_PATH = "/WEB-INF/web.xml";
  public static final String ROOT_CONTEXT = "/";
  public static final String STOP_CONTEXT = "/stop";

  /**
   * Builds an WebAppContext that follows the standard Maven web application.
   * - The resource base points to  "src/main/webapp/"
   * - The application descriptor must be in "src/main/webapp/WEB-INF/web.xml"
   * - The root context is "/"
   * - The context will be attached to the application connector.
   */
  public static ContextHandler buildApplicationContext() {
    try {
      //the webapp directory must be loaded from inside the jar file
      final String resourceBase = ContextFactory.class.getClassLoader().getResource(WEB_DIR).toURI().toString();
      final WebAppContext root = new WebAppContext();
      root.setContextPath(ROOT_CONTEXT);
      root.setDescriptor(resourceBase + DESCRIPTOR_PATH);
      root.setResourceBase(resourceBase);
      root.setParentLoaderPriority(true);
      root.setConnectorNames(new String[] {HttpConnectorFactory.APP_CONNECTOR_NAME});
      return root;
    } catch (URISyntaxException ex){
      Throwables.propagate(ex);
      throw new RuntimeException(ex); //to make javac happy
    }
  }

  /**
   * Creates a ContextHandler with a handler to stop the application gracefully.
   * The context will be attached to the admin connector and accessible at the context "/stop".
   */
  public static ContextHandler buildAdminContext(Server server, String secret) {
    ContextHandler adminContext = new ContextHandler();
    adminContext.setContextPath(ROOT_CONTEXT);
    adminContext.setHandler(new StopHandler(server, secret));
    adminContext.setConnectorNames(new String[] {HttpConnectorFactory.ADMIN_CONNECTOR_NAME});
    return adminContext;
  }

  /**
   * Default private constructor.
   * Utility classes don't expose constructors.
   */
  private ContextFactory() {
    //do nothing
  }

}
