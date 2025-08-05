package tienda.inventario.servicios;

import tienda.inventario.modelo.Entrada;

import java.util.List;

public interface IEntradaServicio {
    Entrada guardarEntrada(Entrada entrada);
    List<Entrada> listarEntradas();
}
