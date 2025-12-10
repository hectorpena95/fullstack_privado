package Privado.fullstack.model.entity;

import java.math.BigDecimal;

public abstract class ProductoBase {

    protected Long id;
    protected String nombre;
    protected BigDecimal precio;

    public ProductoBase(Long id, String nombre, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public abstract String tipoProducto();

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }
}
