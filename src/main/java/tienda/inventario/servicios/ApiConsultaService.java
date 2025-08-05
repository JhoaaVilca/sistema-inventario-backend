package tienda.inventario.servicios;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(ApiConsultaService.class);

    @Value("${api.peru.token}")
    private String TOKEN;

    public Map<String, String> consultarPorDocumento(String tipo, String numero) {
        String url;

        // Validación básica del número
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }

        // Construir URL según tipo (usando apiperu.dev y api_token)
        if (tipo.equalsIgnoreCase("DNI")) {
            url = "https://apiperu.dev/api/dni/" + numero + "?api_token=" + TOKEN;
        } else if (tipo.equalsIgnoreCase("RUC")) {
            url = "https://apiperu.dev/api/ruc/" + numero + "?api_token=" + TOKEN;
        } else {
            throw new IllegalArgumentException("Tipo de documento inválido: " + tipo);
        }

        logger.info("Consultando {} con número: {}", tipo, numero);
        logger.info("URL de consulta: {}", url);

        RestTemplate restTemplate = new RestTemplate();
        String resultado = restTemplate.getForObject(url, String.class);

        if (resultado == null || resultado.trim().isEmpty()) {
            throw new RuntimeException("La API no devolvió ninguna respuesta");
        }

        logger.info("Respuesta de la API: {}", resultado);

        try {
            JSONObject json = new JSONObject(resultado);
            Map<String, String> datos = new HashMap<>();

            // Validar si la API respondió con error
            if (!json.optBoolean("success", true)) {
                String errorMsg = json.optString("message", "Error desconocido en la API");
                throw new RuntimeException("Error en la API externa: " + errorMsg);
            }

            JSONObject data = json.optJSONObject("data");
            if (data == null) {
                throw new RuntimeException("No se recibió el objeto 'data' en la respuesta");
            }

            if (tipo.equalsIgnoreCase("DNI")) {
                String nombres = data.optString("nombres", "");
                String apellidoPaterno = data.optString("apellido_paterno", "");
                String apellidoMaterno = data.optString("apellido_materno", "");

                if (nombres.isEmpty() && apellidoPaterno.isEmpty() && apellidoMaterno.isEmpty()) {
                    throw new RuntimeException("No se encontraron datos para el DNI: " + numero);
                }

                datos.put("nombre", nombres + " " + apellidoPaterno + " " + apellidoMaterno);
                datos.put("numero", numero);
                datos.put("tipo", "DNI");

            } else { // RUC
                String razonSocial = data.optString("nombre_o_razon_social", "");
                String direccion = data.optString("direccion", "");
                String estado = data.optString("estado", "");

                if (razonSocial.isEmpty()) {
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
