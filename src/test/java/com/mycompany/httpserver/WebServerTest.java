package com.mycompany.httpserver;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mycompany.annotations.GetMapping;
import com.mycompany.annotations.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Emily Noreña Cardozo
 */

public class WebServerTest {

    private static final String URL = "http://localhost:8080/";
    private static WebServer server;
    private static Thread serverThread;
    private static HelloController controller;

    @BeforeAll
    public static void setUp() {
        try {
            controller = new HelloController();
            MicroSpringBoot.classScanner();
            Router.get("/api/hello", (req, res) -> "hello " + req.getValues("name"));
            Router.get("/api/pi", (req, resp) -> String.valueOf(Math.PI));
            server = new WebServer("target/classes/webroot");
            serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            Thread.sleep(1000);
            System.out.println("Servidor iniciado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldLoadStaticFileHtml() throws Exception {
        String file = "index.html";
        testFileRequest(file, 200);
    }

    @Test
    public void notShouldLoadStaticFileHtml() throws Exception {
        String file = "nonexistent.html";
        testFileRequest(file, 404);
    }

    @Test
    public void shouldLoadStaticFileCss() throws Exception {
        String file = "styles.css";
        testFileRequest(file, 200);
    }

    @Test
    public void notShouldLoadStaticFileCss() throws Exception {
        String file = "nonexistent.css";
        testFileRequest(file, 404);
    }

    @Test
    public void shouldLoadStaticFileJs() throws Exception {
        String file = "script.js";
        testFileRequest(file, 200);
    }

    @Test
    public void notShouldLoadStaticFileJs() throws Exception {
        String file = "nonexistent.js";
        testFileRequest(file, 404);
    }

    @Test
    public void shouldLoadStaticImagePNG() throws Exception {
        String file = "img.png";
        testFileRequest(file, 200);
    }

    @Test
    public void shouldLoadStaticImageJPG() throws Exception {
        String file = "koala.jpg";
        testFileRequest(file, 200);
    }

    @Test
    public void notShouldLoadStaticImagePNG() throws Exception {
        String file = "nonexistent.png";
        testFileRequest(file, 404);
    }

    @Test
    public void notShouldLoadStaticImageJPG() throws Exception {
        String file = "nonexistent.jpg";
        testFileRequest(file, 404);
    }

    @Test
    public void shouldLoadRestGet() throws Exception {
        String endpoint = "api/hello?name=test";
        testEndpointRequest(endpoint, 200);
    }

    @Test
    public void shouldLoadRestPost() throws Exception {
        String endpoint = "api/hellopost";
        testPostRequest(endpoint, "name=test", 200);
    }

    @Test
    public void shouldLoadHelloRouteWithQueryParam() throws Exception {
        testEndpointRequest("api/hello?name=Pedro", 200);
    }

    @Test
    public void shouldReturnCorrectResponseFromHelloRoute() throws Exception {
        String response = getResponseBody("api/hello?name=Pedro");
        assertTrue(response.contains("hello Pedro"));
    }

    @Test
    public void shouldLoadPiRoute() throws Exception {
        testEndpointRequest("api/pi", 200);
    }

    @Test
    public void shouldReturnCorrectValueFromPiRoute() throws Exception {
        String response = getResponseBody("api/pi");
        assertEquals(String.valueOf(Math.PI), response);
    }

    @Test
    public void shouldServeStaticFileFromCustomDirectory() throws Exception {
        String file = "index.html";
        testFileRequest(file, 200);
    }

    @Test
    public void shouldReturn404ForUnregisteredRoute() throws Exception {
        testEndpointRequest("unknown/route", 404);
    }

    @Test
    void testGetMappingAnnotationPresent() throws Exception {
        Method m = HelloController.class.getDeclaredMethod("greeting", String.class);
        assertTrue(
            m.isAnnotationPresent(GetMapping.class),
            "El método greeting debe tener @GetMapping"
        );
        assertEquals("/greeting", m.getAnnotation(GetMapping.class).value());
    }

    @Test
    void testRequestParamDefaultValue() throws Exception {
        Method m = HelloController.class.getDeclaredMethod("greeting", String.class);
        RequestParam rp = m.getParameters()[0].getAnnotation(RequestParam.class);

        assertNotNull(rp, "El parámetro debe tener @RequestParam");
        assertEquals("name", rp.value(), "El parámetro debe ser 'name'");
        assertEquals("World", rp.defaultValue(), "El defaultValue debe ser 'World'");
    }

    @Test
    void testGreetingWithName() {
        Request req = new Request("/greeting?name=Emily");
        Response res = new Response();
        Route route = Router.find("/greeting");
        String result = route.handle(req, res);
        assertEquals("Hola Emily", result);
    }

    @Test
    void testGreetingWithoutName_UsesDefault() {
        Request req = new Request("/greeting");
        Response res = new Response();
        Route route = Router.find("/greeting");
        String result = route.handle(req, res);
        assertEquals("Hola World", result);
    }

    private String getResponseBody(String endpoint) throws IOException {
        URL requestUrl = new URL(URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private void testFileRequest(String file, int expectedCode) throws IOException {
        URL requestUrl = new URL(URL + file);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        
        try {
            int responseCode = connection.getResponseCode();
            assertEquals(expectedCode, responseCode);
        } finally {
            connection.disconnect();
        }
    }

    private void testEndpointRequest(String endpoint, int expectedCode) throws IOException {
        URL requestUrl = new URL(URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        
        try {
            int responseCode = connection.getResponseCode();
            assertEquals(expectedCode, responseCode);
        } finally {
            connection.disconnect();
        }
    }

    private void testPostRequest(String endpoint, String params, int expectedCode) throws IOException {
        URL requestUrl = new URL(URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        try {
            connection.getOutputStream().write(params.getBytes());
            int responseCode = connection.getResponseCode();
            assertEquals(expectedCode, responseCode);
        } finally {
            connection.disconnect();
        }
    }

    @AfterAll
    public static void tearDown() {
        try {
            if (server != null) {
                server.stop();
            }
            if (serverThread != null) {
                serverThread.join(3000);
            }
            System.out.println("Server closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}