// Privado.fullstack.model.dto.DetalleSolicitudPedido.java

package Privado.fullstack.model.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class DetalleSolicitudPedido {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}