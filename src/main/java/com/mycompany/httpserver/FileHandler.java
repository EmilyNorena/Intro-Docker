package com.mycompany.httpserver;

import java.io.*;
import java.nio.file.*;

/**
 *
 * @author Emily Noreña Cardozo
 */

public class FileHandler {
    private final Path basePath;

    public FileHandler(String baseDirectory) {
        this.basePath = Paths.get(baseDirectory).toAbsolutePath().normalize();
    }

    public void serveFile(String path, Response res, OutputStream out) throws Exception {
        if (path.equals("/")) {
            path = "/index.html"; // Default
        }

        Path filePath = resolveSafePath(path);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            Path errorPage = basePath.resolve("404.html").normalize();
            if (Files.exists(errorPage)) {
                byte[] errorContent = Files.readAllBytes(errorPage);
                res.setStatusCode(404);
                res.addHeader("Content-Type", "text/html; charset=UTF-8");
                res.setBody(errorContent);
            } else {
                res.setStatusCode(404);
                res.setBody("404 Not Found");
            }
            res.send(out);
            return;
        }

        byte[] content = Files.readAllBytes(filePath);
        String contentType = getContentType(path);

        res.addHeader("Content-Type", contentType + (contentType.startsWith("text/") ? "; charset=UTF-8" : ""));
        res.setBody(content);
        res.send(out);
    }

    private Path resolveSafePath(String requestPath) throws SecurityException {
        Path resolved = basePath.resolve(requestPath.substring(1)).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new SecurityException("Directory traversal attempt");
        }
        return resolved;
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "text/plain";
        }
    }
}