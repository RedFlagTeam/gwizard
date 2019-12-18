package org.gwizard.web;

import org.eclipse.jetty.util.thread.ThreadPool;

import lombok.Data;

/**
 * Standard configuration options for a GWizard app.
 */
@Data
public class WebConfig {
	/** */
	private int port = 8080;
	
	/**
	 * Thread pool used in Jetty server, if null the 
	 * default Jetty configuration will be used. 
	 * 
	 * <p>The server pool must be sized according to the
	 * number of acceptors and selectors, the algorithm
	 * being...
	 * 
	 * <pre>
	 *    numberOfAcceptors + numberOfSelectors + 1
	 * </pre>
	 * 
	 * ... where Jetty will default acceptors to 1, and selectors
	 * to the number of CPU processors / 2 (to a minimum of 1 and a 
	 * maximum of 4). With default values an 8 core machine will therefore
	 * require a thread pool with 6 as a minimum size.  
	 */
	private ThreadPool serverThreadPool;
	
	/**
	 * Number of acceptors. Use -1 for the Jetty default. 
	 */
	private int numberOfAcceptors = -1;
	
	/**
     * Number of selectors. Use -1 for the Jetty default, which is the
     * the number of CPU processors / 2 (to a minimum of 1 and a maximum of 4).
     */
	private int numberOfSelectors = -1;
}