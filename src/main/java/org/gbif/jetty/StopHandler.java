package org.gbif.jetty;

import org.gbif.ws.app.ShutdownHolder;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the event of stopping the Jetty server.
 */
public class StopHandler extends HandlerWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(StopHandler.class);
  private static final String SECRET_PARAM = "secret";

  //Jetty server to be stopped
  private final Server server;

  //Secret to stop the server
  private final String secret;

  /**
   * Creates an instance of the StopHandler for the 'server' parameter using the stop 'secret'.
   */
  public StopHandler(Server server, String secret) {
    this.server = server;
    this.secret = secret;
  }

  /**
   * Initiates the stop process.
   * Any unforeseen error returns a INTERNAL_SERVER_ERROR.
   */
  @Override
  public void handle(
    String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response
  ) throws IOException, ServletException {
    try {
      if (target.equals(ContextFactory.STOP_CONTEXT)) {
        stopServer(request, response);
      } else {
        super.handle(target,baseRequest,request,response);
      }
    } catch (Exception ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Stops the server using an HttpRequest.
   * From the request object the "secret" parameters is obtained, if it matches against the secret the server is
   * stopped.
   * If the secret parameter is null or invalid, and HttpStatus.FORBIDDEN_403 is returned in the HttpResponse.
   * The stop process is performed in separate thread to avoid race conditions with the actual Http call.
   */
  private void stopServer(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //Validates the secret
    if (request.getParameterMap().containsKey(SECRET_PARAM) && secret.equals(request.getParameter(SECRET_PARAM))) {
      LOG.info("Stopping Jetty");
      response.setStatus(HttpStatus.ACCEPTED_202);
      response.flushBuffer();
      try { // Stops the server in a new thread to guarantee the execution of the current response
        new Thread() {
          @Override
          public void run() {
            ShutdownHolder.stopServer(server);
            System.exit(0);
          }
        }.start();
      } catch (Exception ex) {
        LOG.error("Unable to stop Jetty", ex);
        throw Throwables.propagate(ex);
      }
    } else { //invalid secret parameter
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
