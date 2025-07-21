package tienda.inventario.servicios;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(ApiConsultaService.class);
    private final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Impob2FubmF2aWxjYUBnbWFpbC5jb20ifQ.mwmV-IM0AeieH3g1n4Ef2ee8PtUprxixwEGhWjVBYZw";

    public Map<String, String> consultarPorDocumento(String tipo, String numero) {
        String url;
        if (tipo.equalsIgnoreCase("DNI")) {
            url = "https://dniruc.apisperu.com/api/v1/dni/" + numero + "?token=" + TOKEN;
        } else if (tipo.equalsIgnoreCase("RUC")) {
            url = "https://dniruc.apisperu.com/api/v1/ruc/" + numero + "?token=" + TOKEN;
        } else {
            throw new IllegalArgumentException("Tipo de documento inválido: " + tipo);
        }

        logger.info("Consultando {} con número: {}", tipo, numero);
        logger.info("URL de consulta: {}", url);

        RestTemplate restTemplate = new RestTemplate();
        String resultado = restTemplate.getForObject(url, String.class);

        logger.info("Respuesta de la API: {}", resultado);

        // Validar que la respuesta no sea null
        if (resultado == null || resultado.trim().isEmpty()) {
            throw new RuntimeException("La API no devolvió ninguna respuesta");
        }

        try {
            JSONObject json = new JSONObject(resultado);
            Map<String, String> datos = new HashMap<>();

            // Verificar si hay error en la respuesta de la API
            if (json.has("error") || json.has("success") && !json.getBoolean("success")) {
                String errorMsg = json.optString("error", "Error desconocido en la API");
                throw new RuntimeException("Error en la API externa: " + errorMsg);
            }

            if (tipo.equalsIgnoreCase("DNI")) {
                // Para DNI, la API devuelve: nombres, apellidoPaterno, apellidoMaterno
                String nombres = json.optString("nombres", "");
                String apellidoPaterno = json.optString("apellidoPaterno", "");
                String apellidoMaterno = json.optString("apellidoMaterno", "");
                
                // Verificar que al menos tengamos un nombre
                if (nombres.trim().isEmpty() && apellidoPaterno.trim().isEmpty() && apellidoMaterno.trim().isEmpty()) {
                    throw new RuntimeException("No se encontraron datos para el DNI: " + numero);
                }
                
                datos.put("nombre", nombres + " " + apellidoPaterno + " " + apellidoMaterno);
                datos.put("numero", numero);
                datos.put("tipo", "DNI");
                
            } else if (tipo.equalsIgnoreCase("RUC")) {
                // Para RUC, la API devuelve: razonSocial, direccion, etc.
                String razonSocial = json.optString("razonSocial", "");
                String direccion = json.optString("direccion", "");
                String estado = json.optString("estado", "");
                
                // Verificar que al menos tengamos razón social
                if (razonSocial.trim().isEmpty()) {
                    throw new RuntimeException("No se encontraron datos para el RUC: " + numero);
                }
                
                datos.put("nombre", razonSocial);
                datos.put("direccion", direccion);
                datos.put("estado", estado);
                datos.put("numero", numero);
                datos.put("tipo", "RUC");
            }

            logger.info("Datos procesados: {}", datos);
            return datos;
            
        } catch (org.json.JSONException e) {
            logger.error("Error al parsear JSON: {}", e.getMessage());
            logger.error("Respuesta que causó el error: {}", resultado);
            throw new RuntimeException("Error al procesar la respuesta de la API: " + e.getMessage());
        }
    }
}
