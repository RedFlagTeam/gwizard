package org.gwizard.web;

import java.util.Random;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.Ignore;
import org.junit.Test;

/*
 * Test is ignored as it opens a random port on the host
 * computer -> bad form. 
 */
@Ignore
public class WebServerTest {

    @Test
    public void shouldStartWithCustomThreadPool() throws Exception {
        WebConfig config = createTestConfig();
        WebServer ws = new WebServer(null, null, null);
        Server s = ws.createServer(config);
        try {
            s.start();
        } finally {
            s.stop();
        }
    }
    
    @Test
    public void shouldStartWithoutCustomThreadPool() throws Exception {
        WebConfig config = new WebConfig();
        randomPort(config);
        WebServer ws = new WebServer(null, null, null);
        Server s = ws.createServer(config);
        try {
            s.start();
        } finally {
            s.stop();
        }
    }

    private WebConfig createTestConfig() {
        WebConfig c = new WebConfig();
        randomPort(c);
        c.setServerThreadPool(new QueuedThreadPool(4, 4));
        c.setNumberOfSelectors(2);
        return c;
    }

    private void randomPort(WebConfig c) {
        c.setPort(new Random().nextInt(1000) + 20000);
    }
}
