package Privado.fullstack.service;

import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.repository.RepositorioUsuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioUsuario {

    private final RepositorioUsuario RepositorioUsuario;

    public ServicioUsuario(RepositorioUsuario RepositorioUsuario) {
        this.RepositorioUsuario = RepositorioUsuario;
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return RepositorioUsuario.findAll();
    }

    public void eliminarUsuario(Long id) {
        RepositorioUsuario.deleteById(id);
    }
}
