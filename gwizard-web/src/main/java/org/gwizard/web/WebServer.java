package org.gwizard.web;

import com.google.common.base.Preconditions;
import com.google.inject.servlet.GuiceFilter;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.gwizard.web.Scanner.Visitor;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EventListener;

/**
 * Simple Jetty-based embedded web server which configures itself from a bound WebConfig and serves the
 * GuiceFilter so you can manage web content with Guice ServletModules. Also clever enough to add any
 * EventListener objects found in the injector bindings.
 */
@Singleton
public class WebServer {

	private final WebConfig webConfig;
	private final EventListenerScanner eventListenerScanner;
	private final HandlerScanner handlerScanner;

	private Server server;

	@Inject
	public WebServer(WebConfig webConfig, EventListenerScanner eventListenerScanner, HandlerScanner handlerScanner) {
		this.webConfig = webConfig;
		this.eventListenerScanner = eventListenerScanner;
		this.handlerScanner = handlerScanner;
	}

	/**
	 * Start the web server. Does not block.
	 *
	 * @see Server#start()
	 */
	public void start() throws Exception {
		Preconditions.checkState(server == null, "Server already started");

		// Create the server.
		server = createServer(webConfig);

		// Create a servlet context and add the jersey servlet.
		final ServletContextHandler sch = createRootServletContextHandler();

		sch.addFilter(GuiceFilter.class, "/*", null);

		// Must add DefaultServlet for embedded Jetty. Failing to do this will cause 404 errors.
		sch.addServlet(DefaultServlet.class, "/");

		// This will add any registered ServletContextListeners or other misc servlet listeners
		// that have been bound. For example, the GuiceResteasyBootstrapServletContextListener
		// which gets bound by gwizard-rest.
		// Sigh no java8
		eventListenerScanner.accept(new Visitor<EventListener>() {
			@Override
			public void visit(EventListener listener) {
				sch.addEventListener(listener);
			}
		});

        final HandlerCollection handlers = new HandlerCollection();

		// the sch is currently the server handler, add it to the list
		handlers.addHandler(sch);

		// This will add any registered jetty Handlers that have been bound.
		handlerScanner.accept(new Visitor<Handler>() {
			@Override
			public void visit(Handler handler) {
				handlers.addHandler(handler);
			}
		});

		server.setHandler(handlers);

		// Start the server
		server.start();
	}

	/**
	 * Overrideable method to create the root ServletContextHandler. This can be used so that the Server can
	 * have a ServletContextHandler that will be able to handle Sessions for example.
	 * By default we create a bare-bones ServletContextHandler that is not set up to handle Sessions.
	 */
	protected ServletContextHandler createRootServletContextHandler() {
		return new ServletContextHandler(null, "/");
	}

	/**
	 * Overrideable method to create the initial jetty Server. We need to draw a lot more configuration parameters
	 * into WebConfig, but for now this gives users a hook to satisfy their needs. Bind a subclass to WebConfig,
	 * subclass this WebServer, and change behavior to whatever you want.
	 */
	protected Server createServer(WebConfig webConfig) {
        ThreadPool p = webConfig.getServerThreadPool();
        Server s = new Server(p == null ? new QueuedThreadPool() : p);
        ServerConnector connector = new ServerConnector(s, webConfig.getNumberOfAcceptors(), webConfig.getNumberOfSelectors());
        connector.setPort(webConfig.getPort());
        s.setConnectors(new Connector[] { connector });
        return s;
	}

	/**
	 * signal the web server to stop
	 */
	public void stop() throws Exception {
		Preconditions.checkState(server != null, "Server not started");
		server.stop();
	}
}
