package org.gbif.ws.discovery.lifecycle;

import org.gbif.ws.discovery.conf.ServiceConfiguration;
import org.gbif.ws.discovery.conf.ServiceDetails;
import org.gbif.ws.discovery.utils.MavenUtils;

import java.io.IOException;

import com.google.common.base.Throwables;
import com.google.common.io.Closer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.maven.project.MavenProject;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that handles the registration process of the executing application into the Zookeeper discovery service.
 */
public class DiscoveryLifeCycle implements LifeCycle.Listener {

  private static final Logger LOG = LoggerFactory.getLogger(DiscoveryLifeCycle.class);
  private final ServiceConfiguration configuration;
  private ServiceDiscovery<ServiceDetails> discovery;
  private CuratorFramework curatorClient;
  private ServiceInstance<ServiceDetails> serviceInstance;
  // Keeps references to the closable elements: curatorClient and discovery.
  private Closer closer = Closer.create();

  /**
   * Creates an instance using the fields zkPath and zkHost of the configuration class.
   */
  public DiscoveryLifeCycle(ServiceConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Required services, curator client and discovery services, are instantiated while  the application is starting.
   */
  @Override
  public void lifeCycleStarting(LifeCycle event) {
    curatorClient = curator(configuration);
    LOG.info("Curator client started");
    discovery = discovery(configuration);
    LOG.info("Discovery service created");
  }

  /**
   * Once the application is started the service is registered in the discovery services.
   */
  @Override
  public void lifeCycleStarted(LifeCycle event) {
    registerService(configuration);
    LOG.info("Service registered");

  }

  /**
   * In case of application failure the service is unregistered.
   */
  @Override
  public void lifeCycleFailure(LifeCycle event, Throwable cause) {
    unRegisterService();
  }

  /**
   * While the application is stopping the service is unregistered.
   */
  @Override
  public void lifeCycleStopping(LifeCycle event) {
    unRegisterService();
  }

  /**
   * Once the application is stopped: do nothing.
   * The service must be unregistered in the event lifeCycleStopping.
   */
  @Override
  public void lifeCycleStopped(LifeCycle event) {
    LOG.info("Discovery services have been stopped");
  }

  public void unRegisterService() {
    try {
      if (discovery != null && serviceInstance != null) {
        discovery.unregisterService(serviceInstance);
        LOG.info("Service instance has been unregistered");
      }
      closer.close();
      LOG.info("All the resources haven been closed");
    } catch (Exception ex) {
      LOG.error("Error unregistering services", ex);
      Throwables.propagate(ex);
    }
  }

  /**
   * Registers a new service instance in Zookeeper using the ServiceDiscovery
   */
  public void registerService(ServiceConfiguration configuration) {
    try {
      serviceInstance = serviceInstance(configuration);
      discovery.registerService(serviceInstance);
    } catch (Exception e) {
      LOG.error("Error registering the service", e);
      Throwables.propagate(e);
    }
  }

  /**
   * Builds a new instance of a ServiceDetails class.
   * Populates the artifact attributes using the Maven pom.xml file.
   */
  public ServiceDetails serviceDetails(ServiceConfiguration configuration) throws IOException {
    MavenProject mavenProject = MavenUtils.getMavenProject();
    ServiceDetails serviceDetails = new ServiceDetails();
    serviceDetails.setServiceConfiguration(configuration);
    serviceDetails.setArtifactId(mavenProject.getArtifactId());
    serviceDetails.setGroupId(mavenProject.getGroupId());
    serviceDetails.setVersion(mavenProject.getVersion());
    return serviceDetails;
  }

  /**
   * Builds a new instance of a CuratorFramework client.
   */
  public CuratorFramework curator(ServiceConfiguration configuration) {
    CuratorFramework curator = CuratorFrameworkFactory.builder()
      .connectString(configuration.getZkHost())
      .namespace(configuration.getZkPath())
      .retryPolicy(new ExponentialBackoffRetry(1000, 3))
      .build();
    curator.start();
    return closer.register(curator);
  }

  /**
   * Builds a new instance of a ServiceDiscovery.
   */
  protected ServiceDiscovery<ServiceDetails> discovery(ServiceConfiguration configuration) {
    JsonInstanceSerializer<ServiceDetails> serializer =
      new JsonInstanceSerializer<ServiceDetails>(ServiceDetails.class);
    return closer.register(ServiceDiscoveryBuilder.builder(ServiceDetails.class)
                             .client(curatorClient)
                             .basePath("/")
                             .serializer(serializer)
                             .build());
  }

  /**
   * Builds a new instance of a ServiceInstance.
   */
  private ServiceInstance<ServiceDetails> serviceInstance(ServiceConfiguration configuration) {
    try {
      final ServiceDetails serviceDetails = serviceDetails(configuration);
      return ServiceInstance.<ServiceDetails>builder()
        .name(serviceDetails.getName())
        .payload(serviceDetails)
        .port(configuration.getHttpPort())
        .uriSpec(new UriSpec(serviceDetails.getExternalUrl()))
        .build();
    } catch (IOException e) {
      LOG.error("Error creating a service instance", e);
      Throwables.propagate(e);
    } catch (Exception e) {
      LOG.error("Error creating a service instance", e);
      Throwables.propagate(e);
    }
    throw new IllegalStateException("Service instance couldn't be created");
  }
}
