package Privado.fullstack.config;

import Privado.fullstack.model.entity.Rol;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.repository.RepositorioRol;
import Privado.fullstack.repository.RepositorioUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioRol repositorioRol;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (repositorioUsuario.findByUsername("admin").isEmpty()) {

            // Crear rol si no existe
            Rol rolAdmin = repositorioRol.findByName("ROLE_ADMIN")
                    .orElseGet(() -> repositorioRol.save(new Rol("ROLE_ADMIN")));

            // Crear usuario admin
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@admin.cl");
            admin.setPassword(passwordEncoder.encode("admin123"));

            // 👇 IMPORTANTE: inicializar el Set de roles
            admin.setRoles(new HashSet<>());
            admin.getRoles().add(rolAdmin);

            repositorioUsuario.save(admin);

            System.out.println(">>> ADMIN creado correctamente con ROLE_ADMIN");
        }
    }
}
