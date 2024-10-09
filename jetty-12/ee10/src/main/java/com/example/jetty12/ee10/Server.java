package com.example.jetty12.ee10;

import com.example.servlet.AsyncStreamingServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        org.eclipse.jetty.server.Server s = new org.eclipse.jetty.server.Server();
        HttpConfiguration httpConfiguration = new HttpConfiguration();

        HTTP2CServerConnectionFactory factory = new HTTP2CServerConnectionFactory(httpConfiguration);
        ServerConnector sc = new ServerConnector(s, factory);
        sc.setPort(10000);

        s.setConnectors(new Connector[] { sc });

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        AsyncStreamingServlet servlet = new AsyncStreamingServlet();
        context.addServlet(new ServletHolder(servlet), "/connect");
        s.setHandler(context);

//        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//        executorService.scheduleAtFixedRate(() -> {
//            try {
//                servlet.sayHi();
//            } catch (Exception e) {
//                // log and continue
//                LOG.warn("Error writing in exec service", e);
//            }
//        }, 1, 1, TimeUnit.SECONDS);

        s.start();
        s.join();
    }
}
