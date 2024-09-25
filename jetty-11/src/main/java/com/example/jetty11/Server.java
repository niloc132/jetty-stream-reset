package com.example.jetty11;

import com.example.servlet.AsyncStreamingServlet;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
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

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                servlet.sayHi();
            } catch (Exception e) {
                // log and continue
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);

        s.start();
        s.join();
    }
}
