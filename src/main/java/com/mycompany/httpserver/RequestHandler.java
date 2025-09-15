package com.mycompany.httpserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Emily
 */
public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final FileHandler fileHandler;
    private final ApiHandler apiHandler;

    public RequestHandler(Socket clientSocket, FileHandler fileHandler, ApiHandler apiHandler) {
        this.clientSocket = clientSocket;
        this.fileHandler = fileHandler;
        this.apiHandler = apiHandler;
    }

    public RequestHandler(Socket clientSocket, String staticFilesPath) {
        this(clientSocket, new FileHandler(WebServer.getStaticFilesPath()), new ApiHandler());
    }

    public void handleRequest() throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream rawOut = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null)
                return;

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String fullPath = parts[1];
            String path = fullPath.split("\\?")[0];
            System.out.println("PATH RECIBIDO: " + path);

            // Headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                String[] header = line.split(":", 2);
                if (header.length == 2)
                    headers.put(header[0].trim(), header[1].trim());
            }

            Request req = new Request(fullPath);
            Response res = new Response();

            Route route = Router.find(path);
            if (route != null && "GET".equals(method)) {
                String body = route.handle(req, res);
                res.setBody(body);
                res.send(rawOut);
                return;
            }

            if (path.startsWith("/api/")) {
                String body = apiHandler.handleApiRequest(method, path, req.getAllParams(), in);
                res.addHeader("Content-Type", "application/json");
                res.setBody(body);
                res.send(rawOut);
            } else {
                fileHandler.serveFile(path, res, rawOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Response errorRes = new Response();
            errorRes.setStatusCode(500);
            errorRes.setBody("Internal Server Error");
            errorRes.send(clientSocket.getOutputStream());
        }
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
