package org.gbif.ws.app;

import org.gbif.jetty.ServerFactory;
import org.gbif.ws.discovery.conf.ServiceConfiguration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Throwables;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that hosts a web application using an embedded Jetty container.
 * The server instance is built using the ServerFactory class.
 * The configuration file specified in the field ServiceConfiguration.conf is stored in the environmental variable
 * app.conf
 * for later used by the components that configured the hosted application.
 */
public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  /**
   * Private constructor.
   */
  private Application(){
    //No instances of this class should be created
  }

  /**
   * Entry point to execute the application.
   * The accepted parameters can be found in the class ServiceConfiguration.
   */
  public static void main(String[] args) {
    LOG.info("Starting the Jetty server");
    try {
      final ServiceConfiguration configuration = parseConfiguration(args);
      final Server server = new ServerFactory().build(configuration);
      new ShutdownHolder(server);
      registerConfVariable(configuration.getConf());
      server.start();
      server.join();
      LOG.info("Jetty has been started");
    } catch(Exception ex){
      LOG.error("An error occurred starting Jetty",ex);
    }
  }

  /**
   * Parses the list of arguments into a configuration class.
   */
  private static ServiceConfiguration parseConfiguration(String[] args) {
    ServiceConfiguration configuration = new ServiceConfiguration();
    JCommander jCommander = new JCommander(configuration);
    try {
      jCommander.parse(args);
      return configuration;
    } catch (ParameterException ex) {
      jCommander.usage();
      Throwables.propagate(ex);
    }
    return configuration;
  }

  /**
   * Register the environment variable app.conf that stores the path to the configuration file.
   * The configuration file is later used for Guice modules.
   */
  private static void registerConfVariable(String confFile) {
    System.setProperty(ConfUtils.APP_CONF_ENV, confFile);
    LOG.info("Configuration file registered : " + confFile);
  }
}
