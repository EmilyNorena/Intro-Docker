package com.mycompany.httpserver;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private int statusCode;
    private Map<String, String> headers;
    private byte[] body;  

    public Response() {
        this.statusCode = 200;
        this.headers = new HashMap<>();
        this.body = new byte[0];
        headers.put("Content-Type", "text/plain; charset=UTF-8");
    }

    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    //Text
    public void setBody(String body) {
        try {
            this.body = body.getBytes("UTF-8");
            headers.put("Content-Length", String.valueOf(this.body.length));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Binary
    public void setBody(byte[] body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.length));
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void send(OutputStream outputStream) throws Exception {
        PrintWriter out = new PrintWriter(outputStream, true);

        out.println("HTTP/1.1 " + statusCode + " " + getStatusText(statusCode));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            out.println(header.getKey() + ": " + header.getValue());
        }
        out.println();
        out.flush();

        outputStream.write(body);
        outputStream.flush();
    }

    private String getStatusText(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 403 -> "Forbidden";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}
