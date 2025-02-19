package mg.itu.prom16.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotations.RestAPI;

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

    
}
