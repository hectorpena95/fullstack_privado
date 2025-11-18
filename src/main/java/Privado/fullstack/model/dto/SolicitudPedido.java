
package Privado.fullstack.model.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SolicitudPedido {

    // Lista de los detalles de la orden (los artículos y sus cantidades)
    // @NotEmpty asegura que el carrito no esté vacío.
    @NotEmpty(message = "La orden debe contener al menos un artículo.")
    // @Valid asegura que se apliquen las validaciones a cada objeto DetalleSolicitudPedido dentro de la lista.
    @Valid
    private List<DetalleSolicitudPedido> detalles;

    // Opcional: Campos para la dirección de envío
    private String direccionEnvio;
    private String ciudadEnvio;
}