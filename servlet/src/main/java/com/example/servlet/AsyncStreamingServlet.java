package com.example.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncStreamingServlet extends HttpServlet {
    private final List<ClientConnection> connections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger nextId = new AtomicInteger(0);

    private final class ClientConnection {
        private final AsyncContext ctx;
        private final ServletInputStream in;
        private final ServletOutputStream out;

        private ClientConnection(int id, AsyncContext ctx) throws IOException {
            this.ctx = ctx;
            in = ctx.getRequest().getInputStream();
            out = ctx.getResponse().getOutputStream();
            ctx.addListener(new AsyncListener() {
                @Override
                public void onComplete(AsyncEvent asyncEvent) throws IOException {
                    System.out.println("Complete #" + id);
                }

                @Override
                public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                    System.out.println("Timeout #" + id);
                }

                @Override
                public void onError(AsyncEvent asyncEvent) throws IOException {
                    System.out.println("Error #" + id);
                    connections.remove(ClientConnection.this);
                    ctx.complete();
                }

                @Override
                public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
                    System.out.println("Start #" + id);
                }
            });
            in.setReadListener(new ReadListener() {
                @Override
                public void onDataAvailable() throws IOException {
                    byte[] bytes = in.readAllBytes();
                    System.out.println("DataAvailable #" + id + ", " + bytes.length + "bytes");
                }

                @Override
                public void onAllDataRead() throws IOException {
                    System.out.println("AllDataRead #" + id);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Read Error #" + id);
                    connections.remove(ClientConnection.this);
                    ctx.complete();
                }
            });

            out.setWriteListener(new WriteListener() {
                @Override
                public void onWritePossible() throws IOException {
                    System.out.println("WritePossible #" + id);
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Write Error #" + id);
                    connections.remove(ClientConnection.this);
                    ctx.complete();
                }
            });
        }

        public void close() {
            ctx.complete();
        }

        public void println(String msg) {
            try {
                if (out.isReady()) {
                    System.out.println(msg);
                    out.println(msg);
                    if (out.isReady()) {
                        out.flush();
                    }
                } // else normally we'd enqueue
            } catch (IOException e) {
                connections.remove(ClientConnection.this);
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncCtx = req.startAsync(req, resp);
        asyncCtx.setTimeout(10_000);

        final int id = nextId.getAndIncrement();

        System.out.println("New request #" + id);

        sayHi();

        ClientConnection c = new ClientConnection(id, asyncCtx);
        connections.add(c);
    }

    public void sayHi() {
        for (ClientConnection client : connections) {
            client.println("Hello");
        }
    }
}
