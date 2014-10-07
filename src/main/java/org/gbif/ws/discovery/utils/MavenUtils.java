package org.gbif.ws.discovery.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.base.Throwables;
import com.google.common.io.Closer;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to extract information from Maven projects.
 */
public class MavenUtils {

  private static final String POM = "pom.xml";
  private static final String ERROR_MSG = "Error reading the maven project";

  private static final Logger LOG = LoggerFactory.getLogger(MavenUtils.class);

  /**
   * Reads the pom.xml file and returns a MavenProjects instance.
   */
  public static MavenProject getMavenProject() throws IOException {
    Closer closer = Closer.create();
    try {
      MavenXpp3Reader mavenReader = new MavenXpp3Reader();
      Model model = mavenReader.read(closer.register(MavenUtils.class.getResourceAsStream(getPOMPath())));
      return new MavenProject(model);
    } catch (FileNotFoundException e) {
      LOG.error(ERROR_MSG, e);
      Throwables.propagate(e);
    } catch (IOException e) {
      LOG.error(ERROR_MSG, e);
      Throwables.propagate(e);
    } catch (XmlPullParserException e) {
      LOG.error(ERROR_MSG, e);
      Throwables.propagate(e);
    } finally {
      closer.close();
    }
    throw new IllegalStateException("An error occurred reading the pom.xml file");
  }

  /**
   * Gets the path to the pom.xml file that is inside the jar file.
   */
  private static String getPOMPath() throws IOException {
    Closer closer = Closer.create();
    String jarPath = MavenUtils.class.getResource("").getPath().replaceAll("file:", "");
    jarPath = jarPath.replaceAll("!/" + MavenUtils.class.getPackage().getName().replaceAll("\\.", "/") + '/', "");
    try {
      JarFile jarFile = closer.register(new JarFile(jarPath));
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();
        if (jarEntry.getName().endsWith(POM)) {
          return '/' + jarEntry.getName();
        }
      }
    } finally {
      closer.close();
    }
    throw new IllegalStateException("Error reading pom.xml file");
  }

  /**
   * Private constructor.
   * Utility classes must have a private constructor.
   */
  private MavenUtils() {
    // empty private constructor
  }
}
