package Privado.fullstack.service;

import Privado.fullstack.model.entity.ProductoBase;
import org.springframework.stereotype.Service;

@Service
public class ProductoPolimorfismoService {

    public String describirProducto(ProductoBase producto) {
        return "Producto: " + producto.getNombre() +
                " | Tipo: " + producto.tipoProducto() +
                " | Precio: " + producto.getPrecio();
    }
}
