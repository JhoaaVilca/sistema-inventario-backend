package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.*;
import tienda.inventario.mapper.CreditoMapper;
import tienda.inventario.modelo.*;
import tienda.inventario.repositorio.ClienteRepositorio;
import tienda.inventario.repositorio.SalidaRepositorio;
import tienda.inventario.servicios.ICreditoServicio;

@RestController
@RequestMapping("/api/creditos")
public class CreditoControlador {

    @Autowired
    private ICreditoServicio creditoServicio;

    @Autowired
    private SalidaRepositorio salidaRepositorio;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @GetMapping
    public Page<CreditoResponseDTO> listar(@PageableDefault(size = 20) Pageable pageable) {
        return creditoServicio.listarCreditos(pageable).map(CreditoMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditoResponseDTO> obtener(@PathVariable Long id) {
        var c = creditoServicio.obtenerPorId(id);
        return ResponseEntity.ok(CreditoMapper.toResponse(c));
    }

    @PostMapping
    public ResponseEntity<CreditoResponseDTO> crear(@RequestBody CreditoRequestDTO dto) {
        Salida salida = salidaRepositorio.findById(dto.getIdSalida()).orElseThrow(() -> new RuntimeException("Salida no encontrada"));
        Cliente cliente = clienteRepositorio.findById(dto.getIdCliente()).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Credito credito = CreditoMapper.toEntity(dto, salida, cliente);
        var creado = creditoServicio.crearCredito(credito);
        return ResponseEntity.ok(CreditoMapper.toResponse(creado));
    }

    @PostMapping("/{id}/pagos")
    public ResponseEntity<PagoCreditoResponseDTO> registrarPago(@PathVariable Long id, @RequestBody PagoCreditoRequestDTO dto) {
        PagoCredito pago = new PagoCredito();
        pago.setFechaPago(dto.getFechaPago());
        pago.setMonto(dto.getMonto());
        pago.setMedioPago(dto.getMedioPago());
        pago.setObservacion(dto.getObservacion());
        var guardado = creditoServicio.registrarPago(id, pago);
        PagoCreditoResponseDTO resp = new PagoCreditoResponseDTO();
        resp.setIdPago(guardado.getIdPago());
        resp.setFechaPago(guardado.getFechaPago());
        resp.setMonto(guardado.getMonto());
        resp.setMedioPago(guardado.getMedioPago());
        resp.setObservacion(guardado.getObservacion());
        return ResponseEntity.ok(resp);
    }
}


