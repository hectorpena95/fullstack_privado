package Privado.fullstack.model;

import lombok.Data;

@Data
public class SolicitudRegistro {
    private String username;
    private String email;
    private String password;
    private String fullName; // Nombre completo opcional
}