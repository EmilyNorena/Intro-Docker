package com.mycompany.httpserver;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;

public class WebServer {
    private static final int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080"));;
    private ServerSocket serverSocket;
    private boolean running = true;
    private FileHandler fileHandler;
    private ApiHandler apiHandler;
    private static String staticFilesPath;
    private ExecutorService threadpool;

    public static void main(String[] args) throws Exception {
        MicroSpringBoot.classScanner();

        Router.get("/api/helloworld", (req, res) -> "hello world!");
        Router.get("/api/hello", (req, res) -> "hello " + req.getValues("name"));
        Router.get("/api/pi", (req, resp) -> String.valueOf(Math.PI));

        String path = staticFilesPath != null ? staticFilesPath : "target/classes/webroot";

        WebServer server = new WebServer(path);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }

    public WebServer(String staticFilesPath) {
        this.staticFilesPath = staticFilesPath;
        this.fileHandler = new FileHandler(staticFilesPath);
        this.apiHandler = new ApiHandler();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            threadpool = Executors.newFixedThreadPool(10);
            running = true;
            System.out.println("Server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadpool.submit(new RequestHandler(clientSocket, fileHandler, apiHandler));
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Socket error: " + e.getMessage());
                    } else {
                        System.out.println("Server socket closed.");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        if (threadpool != null) {
            threadpool.shutdown();
            try {
                if (!threadpool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadpool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadpool.shutdownNow();
            }
        }
        System.out.println("Server stopped.");
    }

    public static void staticfiles(String path) {
        staticFilesPath = path;
    }

    public static String getStaticFilesPath() {
        return staticFilesPath;
    }
}