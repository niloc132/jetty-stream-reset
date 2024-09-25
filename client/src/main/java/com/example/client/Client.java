package com.example.client;

import org.eclipse.jetty.client.BufferingResponseListener;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()));
        client.start();
        Request post = client.POST("http://localhost:10000/connect");

        BufferingResponseListener listener = new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                LOG.info("Complete");
            }
        };

        post.send(listener);
        // wait three seconds, take what we get and get out
        Thread.sleep(3000);
        byte[] content = listener.getContent();
        System.out.println(new String(content, StandardCharsets.UTF_8));

        post.abort(new Exception());

        System.exit(0);
    }
}
