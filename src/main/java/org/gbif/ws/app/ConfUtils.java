package org.gbif.ws.app;

import com.google.common.io.Resources;

/**
 * Utility class that holds configurations constants and methods used by Web applications.
 */
public class ConfUtils {

  /**
   * Private constructor.
   * Can't crate instances of this utility class.
   */
  private ConfUtils(){
    //do nothing
  }

  /**
   * Default environment variables that holds the path to the configuration file used by the embedded application.
   */
  public static final String APP_CONF_ENV = "app.conf";

  /**
   * Gets the path to the configuration file of the current Web application.
   * Returns the value of the environment variable APP_CONF_ENV, if any; otherwise returns
   * absolute path to the file name  @parameter defaultAppConfFile.
   */
  public static final String getAppConfFile(String defaultAppConfFile) {
    if(System.getProperties().containsKey(APP_CONF_ENV)){
      return System.getProperty(APP_CONF_ENV);
    }
    return Resources.getResource(defaultAppConfFile).getFile();
  }

}
