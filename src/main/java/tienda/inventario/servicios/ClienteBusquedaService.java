package tienda.inventario.servicios;

import org.springframework.stereotype.Service;
import tienda.inventario.dto.ClienteBusquedaDTO;
import tienda.inventario.modelo.Cliente;
import tienda.inventario.repositorio.ClienteRepositorio;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClienteBusquedaService {

    private final ClienteRepositorio clienteRepositorio;
    private final ApiConsultaService apiConsultaService;

    public ClienteBusquedaService(ClienteRepositorio clienteRepositorio, ApiConsultaService apiConsultaService) {
        this.clienteRepositorio = clienteRepositorio;
        this.apiConsultaService = apiConsultaService;
    }

    /**
     * Busca un cliente por DNI, primero en la BD local y luego en la API de RENIEC
     */
    public ClienteBusquedaDTO buscarClientePorDni(String dni) {
        ClienteBusquedaDTO resultado = new ClienteBusquedaDTO();
        resultado.setDni(dni);

        try {
            // 1. Primero buscar en la BD local
            Cliente clienteLocal = clienteRepositorio.findByDni(dni).orElse(null);
            
            if (clienteLocal != null) {
                // Cliente encontrado en BD local
                resultado.setExisteEnBD(true);
                resultado.setNombres(clienteLocal.getNombres());
                resultado.setApellidos(clienteLocal.getApellidos());
                resultado.setDireccion(clienteLocal.getDireccion());
                resultado.setTelefono(clienteLocal.getTelefono());
                resultado.setEmail(clienteLocal.getEmail());
                resultado.setMensaje("Cliente encontrado en la base de datos local");
                return resultado;
            }

            // 2. Si no está en BD local, consultar API de RENIEC
            try {
                Map<String, String> datosApi = apiConsultaService.consultarPorDocumento("DNI", dni);
                
                resultado.setExisteEnBD(false);
                resultado.setNombres(extraerNombres(datosApi.get("nombre")));
                resultado.setApellidos(extraerApellidos(datosApi.get("nombre")));
                resultado.setDireccion(datosApi.get("direccion"));
                resultado.setMensaje("Cliente encontrado en RENIEC. Puede registrarlo en la base de datos.");
                
            } catch (Exception e) {
                // Error en la API de RENIEC
                resultado.setExisteEnBD(false);
                resultado.setMensaje("DNI no encontrado en RENIEC. Verifique el número.");
            }

        } catch (Exception e) {
            resultado.setMensaje("Error en la búsqueda: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Extrae los nombres del string completo de la API
     */
    private String extraerNombres(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "";
        }
        
        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length <= 2) {
            return partes[0];
        }
        
        // Los nombres están al inicio, antes de los apellidos
        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < partes.length - 2; i++) {
            if (i > 0) nombres.append(" ");
            nombres.append(partes[i]);
        }
        return nombres.toString();
    }

    /**
     * Extrae los apellidos del string completo de la API
     */
    private String extraerApellidos(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "";
        }
        
        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length <= 2) {
            return partes.length == 2 ? partes[1] : "";
        }
        
        // Los apellidos están al final
        StringBuilder apellidos = new StringBuilder();
        for (int i = partes.length - 2; i < partes.length; i++) {
            if (i > partes.length - 2) apellidos.append(" ");
            apellidos.append(partes[i]);
        }
        return apellidos.toString();
    }
}
