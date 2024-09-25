package com.example.client;

import org.eclipse.jetty.client.BufferingResponseListener;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;

import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()));
        client.start();
        Request post = client.POST("http://localhost:10000/connect");
        post.body(new StringRequestContent("text/plain", "1234567890"));

        BufferingResponseListener listener = new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                System.out.println("Complete");
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
