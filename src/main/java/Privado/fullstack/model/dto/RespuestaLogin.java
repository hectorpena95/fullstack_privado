package Privado.fullstack.model.dto;

import lombok.Data;

@Data
public class RespuestaLogin {
    private String token;
    private String tipo = "Bearer";
    private String username;
    private String rol;
}