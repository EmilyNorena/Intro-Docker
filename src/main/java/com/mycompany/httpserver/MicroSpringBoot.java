package com.mycompany.httpserver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.annotations.GetMapping;
import com.mycompany.annotations.RequestParam;
import com.mycompany.annotations.RestController;

public class MicroSpringBoot {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String className = args[0];
            Class<?> c = Class.forName(className);
            System.out.println("Class loaded from arguments: " + c.getName());
        }
    }

    public static void classScanner() throws Exception {
        File root = new File("classes/com/mycompany/httpserver"); //Revisar
        if (!root.exists()) {
            System.err.println("Directory not found: " + root.getAbsolutePath());
            return;
        }

        List<Class<?>> controllers = new ArrayList<>(); //Obtenemos todas las clases con @RestController
        scanDirectory(root, "com.mycompany.httpserver", controllers);

        for (Class<?> clazz : controllers) {
            System.out.println("Loading component: " + clazz.getName());

            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) { //Revisa si la anotación de métodos está presente
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String path = mapping.value(); //Tomamos el path de GetMapping

                    Router.get(path, (req, res) -> { //Registramos la ruta en Router
                        try {
                            String name = method.getParameters()[0].getAnnotation(RequestParam.class).value();
                            String param = req.getValues(name);
                        if (param == null || param.isEmpty()){
                            param = method.getParameters()[0].getAnnotation(RequestParam.class).defaultValue();
                        }
                            return (String) method.invoke(instance, param);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return "Internal error in " + path;
                        }
                    });
                    System.out.println(" -> Registered route: " + path);
                }
            }
        }
    }

    private static void scanDirectory(File dir, String packageName, List<Class<?>> controllers)
            throws ClassNotFoundException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), controllers); // Recursión para subcarpetas
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(RestController.class)) {
                    controllers.add(clazz); 
                }
            }
        }
    }

}
