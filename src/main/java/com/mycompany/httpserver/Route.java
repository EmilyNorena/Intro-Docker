package com.mycompany.httpserver;

@FunctionalInterface
public interface Route {
    String handle(Request req, Response res);
}