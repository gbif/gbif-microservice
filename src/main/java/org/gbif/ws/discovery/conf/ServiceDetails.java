package org.gbif.ws.discovery.conf;

import com.google.common.base.Objects;

/**
 * This class contains the information that is published in the discovery service registry (Zookeeper).
 */
public class ServiceDetails {

  private static final String URL_FMT = "http://%s:%s/";

  // Maven settings
  private String groupId;
  private String artifactId;
  private String version;

  private ServiceConfiguration serviceConfiguration;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ServiceConfiguration getServiceConfiguration() {
    return serviceConfiguration;
  }

  public void setServiceConfiguration(ServiceConfiguration serviceConfiguration) {
    this.serviceConfiguration = serviceConfiguration;
  }

  public String getName() {
    return artifactId;
  }

  /**
   * The full service name contains the artifact and the version.
   */
  public String getFullName() {
    return artifactId + '-' + version;
  }

  public String getExternalUrl() {
    return String.format(URL_FMT,
                         serviceConfiguration.getHost(),
                         Objects.firstNonNull(serviceConfiguration.getExternalPort(),
                                              serviceConfiguration.getHttpPort()));
  }

}
