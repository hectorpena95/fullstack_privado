package Privado.fullstack.controller;

import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.repository.RepositorioUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorUsuarioAdmin {

    private final RepositorioUsuario repositorioUsuario;

    public ControladorUsuarioAdmin(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return ResponseEntity.ok(repositorioUsuario.findAll());
    }
}
