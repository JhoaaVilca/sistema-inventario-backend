package tienda.inventario.servicios;

import tienda.inventario.modelo.Empresa;

public interface IEmpresaServicio {
    Empresa obtenerConfiguracion();
    Empresa actualizarConfiguracion(Empresa empresa);
}



