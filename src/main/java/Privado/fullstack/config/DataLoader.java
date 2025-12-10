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

        // ============================
        // CREAR ROLES
        // ============================
        crearRolSiNoExiste("ROLE_ADMIN");
        crearRolSiNoExiste("ROLE_CLIENTE");
        crearRolSiNoExiste("ROLE_VENDEDOR");
        crearRolSiNoExiste("ROLE_CLIENTE_PREMIUM");

        // ============================
        // CREAR USUARIO ADMIN
        // ============================
        if (repositorioUsuario.findByUsername("admin").isEmpty()) {

            Rol rolAdmin = repositorioRol.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN no encontrado"));

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@admin.cl");
            admin.setPassword(passwordEncoder.encode("admin123"));

            admin.setRoles(new HashSet<>());
            admin.getRoles().add(rolAdmin);

            repositorioUsuario.save(admin);

            System.out.println(">>> ADMIN creado con ROLE_ADMIN");
        }

        // ============================
        // CREAR USUARIO VENDEDOR
        // ============================
        if (repositorioUsuario.findByUsername("vendedor").isEmpty()) {

            Rol rolVendedor = repositorioRol.findByName("ROLE_VENDEDOR")
                    .orElseThrow(() -> new RuntimeException("ROLE_VENDEDOR no encontrado"));

            Usuario vendedor = new Usuario();
            vendedor.setUsername("vendedor");
            vendedor.setEmail("vendedor@venta.cl");
            vendedor.setPassword(passwordEncoder.encode("vendedor123"));

            vendedor.setRoles(new HashSet<>());
            vendedor.getRoles().add(rolVendedor);

            repositorioUsuario.save(vendedor);

            System.out.println(">>> Usuario VENDEDOR creado con ROLE_VENDEDOR");
        }
    }

    // ============================
    // MÉTODO PARA CREAR ROLES
    // ============================
    private void crearRolSiNoExiste(String nombreRol) {
        repositorioRol.findByName(nombreRol)
                .orElseGet(() -> {
                    System.out.println(">>> Creando rol: " + nombreRol);
                    return repositorioRol.save(new Rol(nombreRol));
                });
    }
}
