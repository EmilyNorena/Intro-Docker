package com.mycompany.httpserver;
import java.io.*;
import java.util.*;

/**
 *
 * @author Emily Noreña Cardozo
 */

public class ApiHandler {

    public String handleApiRequest(String method, String path, Map<String, String> queryParams, BufferedReader in) throws IOException {
        if ("/api/hello".equals(path)) {
            return handleHello(queryParams);
        } else if ("/api/hellopost".equals(path) && "POST".equals(method)) {
            return handleHelloPost(in);
        }
        return "{\"error\":\"Endpoint not found\"}";
    }

    private String handleHello(Map<String, String> queryParams) {
        String name = queryParams.get("name");
        return "{\"message\":\"Hola, " + (name != null ? name : "NA") + "\"}";
    }

    private String handleHelloPost(BufferedReader in) throws IOException {
        StringBuilder body = new StringBuilder();
        while (in.ready()) {
            body.append((char) in.read());
        }
        String bodyStr = body.toString();
        String name = "NA";
        if (bodyStr.contains("=")) {
            String[] parts = bodyStr.split("=", 2);
            if (parts.length == 2 && !parts[1].isEmpty()) {
                name = parts[1];
            }
        }
        return "{\"message\":\"Hola, " + name + "\"}";
    }
}