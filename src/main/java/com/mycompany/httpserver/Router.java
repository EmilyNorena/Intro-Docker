package com.mycompany.httpserver;

import java.util.HashMap;
import java.util.Map;

public class Router {
     private static final Map<String, Route> routes = new HashMap<>();

    public static void get(String path, Route handler) {
        routes.put(path, handler);
    }

    public static Route find(String path) {
        return routes.get(path);
    }
}
