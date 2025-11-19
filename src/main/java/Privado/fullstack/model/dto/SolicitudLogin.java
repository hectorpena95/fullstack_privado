package Privado.fullstack.model.dto;

import lombok.Data;

@Data
public class SolicitudLogin {
    private String email;
    private String password;
}