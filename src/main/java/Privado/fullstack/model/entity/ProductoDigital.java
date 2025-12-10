package Privado.fullstack.model.entity;

import java.math.BigDecimal;

public class ProductoDigital extends ProductoBase {

    public ProductoDigital(Long id, String nombre, BigDecimal precio) {
        super(id, nombre, precio);
    }

    @Override
    public String tipoProducto() {
        return "DIGITAL";
    }
}
