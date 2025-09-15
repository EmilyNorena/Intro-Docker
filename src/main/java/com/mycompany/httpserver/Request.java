package com.mycompany.httpserver;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String fullPath){
        this.path = fullPath.split("\\?")[0];
        this.queryParams = parseQueryParams(fullPath);
    }

    public String getPath() {
        return path;
    }

    public String getValues(String key) {
        return queryParams.get(key);
    }

    public Map<String, String> getAllParams() {
        return queryParams;
    }

    private Map<String, String> parseQueryParams(String fullPath) {
        Map<String, String> params = new HashMap<>();
        if (fullPath.contains("?")) {
            String query = fullPath.split("\\?")[1];
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], ""); // caso de parámetro sin valor
                }
            }
        }
        return params;
    }
}
