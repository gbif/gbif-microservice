package org.gbif.ws.app;

/**
 * Shutdown hook that stops a Jetty server.
 */
public class ShutdownHook extends Thread {

  private final ShutdownHolder holder;

  /**
   * Creates an instance using ShutdownHolder as the parent thread/context.
   */
  public ShutdownHook(ShutdownHolder holder) {
    this.holder = holder;
  }

  /**
   * Executes the shutdown process.
   */
  @Override
  public void run() {
    holder.shutdown();
  }
}
