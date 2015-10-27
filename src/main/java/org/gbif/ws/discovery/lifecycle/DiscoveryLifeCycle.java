package org.gbif.ws.discovery.lifecycle;

import org.gbif.ws.discovery.conf.ServiceConfiguration;
import org.gbif.ws.discovery.conf.ServiceDetails;
import org.gbif.ws.discovery.conf.ServiceStatus;
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
 * Listener that handles the registration process of the executing application into the Zookeeper discoveryService service.
 */
public class DiscoveryLifeCycle implements LifeCycle.Listener {

  private static final Logger LOG = LoggerFactory.getLogger(DiscoveryLifeCycle.class);
  private final ServiceConfiguration configuration;
  private ServiceDiscovery<ServiceDetails> discoveryService;
  private CuratorFramework curatorClient;
  private ServiceInstance<ServiceDetails> serviceInstance;
  // Keeps references to the closable elements: curatorClient and discoveryService.
  private final Closer closer = Closer.create();

  /**
   * Creates an instance using the fields zkPath and zkHost of the configuration class.
   */
  public DiscoveryLifeCycle(ServiceConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Required services, curator client and discoveryService services, are instantiated while  the application is starting.
   */
  @Override
  public void lifeCycleStarting(LifeCycle event) {
    curatorClient = curator(configuration);
    LOG.info("Curator client started");
    discoveryService = discovery();
    LOG.info("Discovery service created");
    LOG.info("Registering service");
    registerService(configuration);
    LOG.info("Service registered {}", serviceInstance.toString());
  }

  /**
   * Once the application is started the service is registered in the discoveryService services.
   */
  @Override
  public void lifeCycleStarted(LifeCycle event) {
    updateServiceStatus(ServiceStatus.RUNNING);
    LOG.info("Service registered");
  }

  /**
   * In case of application failure the service is unregistered.
   */
  @Override
  public void lifeCycleFailure(LifeCycle event, Throwable cause) {
    updateServiceStatus(ServiceStatus.FAILED);
    unRegisterService();
  }

  /**
   * While the application is stopping the service is unregistered.
   */
  @Override
  public void lifeCycleStopping(LifeCycle event) {
    updateServiceStatus(ServiceStatus.STOPPING);
  }

  /**
   * Once the application is stopped: do nothing.
   * The service must be unregistered in the event lifeCycleStopping.
   */
  @Override
  public void lifeCycleStopped(LifeCycle event) {
    updateServiceStatus(ServiceStatus.STOPPED);
    unRegisterService();
    LOG.info("Discovery services have been stopped");
  }

  public void unRegisterService() {
    try {
      if (discoveryService != null && serviceInstance != null) {
        discoveryService.unregisterService(serviceInstance);
        LOG.info("Service instance has been unregistered");
      }
      closer.close();
      LOG.info("All the resources haven been closed");
    } catch (Exception ex) {
      LOG.error("Error unregistering services", ex);
      throw Throwables.propagate(ex);
    }
  }

  /**
   * Registers a new service instance in Zookeeper using the ServiceDiscovery.
   */
  public void registerService(ServiceConfiguration configuration) {
    try {
      serviceInstance = registerServiceInstance(configuration, ServiceStatus.STARTING);
      discoveryService.registerService(serviceInstance);
    } catch (Exception e) {
      LOG.error("Error registering the service", e);
      throw Throwables.propagate(e);
    }
  }

  /**
   * Updates the service instance status in Zookeeper.
   */
  public void updateServiceStatus(ServiceStatus serviceStatus) {
    try {
      if(serviceInstance != null) {
        serviceInstance.getPayload().setStatus(serviceStatus);
        discoveryService.updateService(serviceInstance);
      }
    } catch (Exception e) {
      LOG.error("Error updating service status", e);
      throw Throwables.propagate(e);
    }
  }

  /**
   * Builds a new instance of a ServiceDetails class.
   * Populates the artifact attributes using the Maven pom.xml file.
   */
  public static ServiceDetails serviceDetails(ServiceConfiguration configuration) throws IOException {
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
  protected ServiceDiscovery<ServiceDetails> discovery() {
    JsonInstanceSerializer<ServiceDetails> serializer =
      new JsonInstanceSerializer<ServiceDetails>(ServiceDetails.class);
    return closer.register(ServiceDiscoveryBuilder.builder(ServiceDetails.class)
                             .client(curatorClient)
                             .basePath("/")
                             .serializer(serializer)
                             .build());
  }

  /**
   * Registers a new instance of a ServiceInstance in a specific status.
   */
  private static ServiceInstance<ServiceDetails> registerServiceInstance(ServiceConfiguration configuration, ServiceStatus serviceStatus) {
    try {
      ServiceDetails serviceDetails = serviceDetails(configuration);
      serviceDetails.setStatus(serviceStatus);
      return ServiceInstance.<ServiceDetails>builder()
        .name(serviceDetails.getName())
        .payload(serviceDetails)
        .port(configuration.getHttpPort())
        .uriSpec(new UriSpec(serviceDetails.getExternalUrl()))
        .build();
    } catch (Exception e) {
      LOG.error("Error creating a service instance", e);
      throw Throwables.propagate(e);
    }
  }
}
