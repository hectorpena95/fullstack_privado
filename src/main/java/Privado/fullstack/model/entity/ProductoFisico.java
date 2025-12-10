package Privado.fullstack.model.entity;

import java.math.BigDecimal;

public class ProductoFisico extends ProductoBase {

    public ProductoFisico(Long id, String nombre, BigDecimal precio) {
        super(id, nombre, precio);
    }

    @Override
    public String tipoProducto() {
        return "FÍSICO";
    }
}
