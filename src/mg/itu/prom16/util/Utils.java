package mg.itu.prom16.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotations.RestAPI;
import mg.itu.prom16.map.Mapping;
import mg.itu.prom16.views.ModelView;

public class Utils {
    public static FileUpload handleFileUpload(HttpServletRequest request, String inputFileParam)
            throws IOException, ServletException {
        Part filePart = request.getPart(inputFileParam);
        if (filePart == null)
            return null;
        String fileName = extractFileName(filePart);
        byte[] fileContent = filePart.getInputStream().readAllBytes();

        String uploadDir = request.getServletContext().getRealPath("") + "uploads\\" + fileName;
        System.out.println("upload = " + uploadDir);

        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String uploadPath = uploadDir + File.separator + fileName;
        System.out.println("upload = " + uploadPath);

        filePart.write(uploadPath);

        return new FileUpload(fileName, uploadPath, fileContent);
    }

    private static String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] items = contentDisposition.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }
        return "";
    }

    public static Object caster(String value, Class<?> classe) {

        if (classe == int.class || classe == Integer.class) {
            return Integer.parseInt(value);
        } else if (classe == long.class || classe == Long.class) {
            return Long.parseLong(value);
        } else if (classe == double.class || classe == Double.class) {
            return Double.parseDouble(value);
        } else if (classe == float.class || classe == Float.class) {
            return Float.parseFloat(value);
        } else if (classe == boolean.class || classe == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (classe == byte.class || classe == Byte.class) {
            return Byte.parseByte(value);
        } else if (classe == short.class || classe == Short.class) {
            return Short.parseShort(value);
        } else if (classe == char.class || classe == Character.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Cannot convert string to char: " + value);
            }
            return value.charAt(0);
        } else if (classe == LocalDate.class) {
            return LocalDate.parse(value);
        } else {
            return value;
        }

        // done
    }

    public static boolean isRestAPI(Object obj, String methodName) {
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (method.isAnnotationPresent(RestAPI.class)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public static void sendModelView(ModelView modelView, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println(modelView.getUrl());
        for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
            System.out.println("DATA ---");
            request.setAttribute(entry.getKey(), entry.getValue());
            System.out.println(entry.getKey() + "_" + entry.getValue());
        }

        for (Map.Entry<String, String> errorEntry : modelView.getValidationErrors().entrySet()) {
            System.out.println("ValidationsErrors ---");
            request.setAttribute(errorEntry.getKey(), errorEntry.getValue());
            System.out.println(errorEntry.getKey() + " : " + errorEntry.getValue());
        }

        for (Map.Entry<String, Object> valueEntry : modelView.getValidationValues().entrySet()) {
            System.out.println("ValidationsValues ---");
            request.setAttribute(valueEntry.getKey(), valueEntry.getValue());
            System.out.println(valueEntry.getKey() + " : " + valueEntry.getValue());
        }

        RequestDispatcher dispatch = request.getRequestDispatcher(modelView.getUrl());
        dispatch.forward(request, response);
    }

    public static void checkAuthProfil(Mapping mapping, HttpServletRequest req, String hote_name)
            throws Exception {
        String hote = "auth";
        if (hote_name != null && hote_name != "") {
            hote = hote_name;
        }

        if (mapping.needAuth()) {
            System.out.println(mapping.getProfil() + " != " + req.getSession().getAttribute(hote));
            if (!mapping.getProfil().equals(req.getSession().getAttribute(hote))) {
                // throw new CustomException.RequestException("unauthorize");
                throw new Exception("Unauthorize");
            }
        }
    }

    public static String getSerarchedController(String url) {
        String[] requestUrlSplitted = url.split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];
        return url.endsWith("/") ? "/" : url.endsWith("\\") ? "\\" : controllerSearched;

    }

    public static void handleRedirect(Redirect redirect, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String lien = redirect.getLien();
        if (lien == null || lien.equalsIgnoreCase("")) {
            return;
        }
        RequestDispatcher dispatch = request.getRequestDispatcher(lien);
        request.setAttribute("isRedirection", true);
        request.setAttribute("data_redirect", redirect.getData());
        for (Map.Entry<String, Object> entry : redirect.getData().entrySet()) {
            String cle = entry.getKey();
            Object valeur = entry.getValue();
            System.out.println("Clé : " + cle + ", Valeur : " + valeur);
        }
        dispatch.forward(request, response);

    }

    public static Redirect handleRedirectAttribute(Parameter parameter, HttpServletRequest request) throws Exception {
        Redirect redirect = ((Redirect) parameter.getType().getDeclaredConstructor().newInstance());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = ((Map<String, Object>) request.getAttribute("data_redirect"));
        data.forEach((cle, valeur) -> {
            System.out.println(cle + " = " + valeur);
        });
        redirect.setData(data);
        return redirect;
    }
}
