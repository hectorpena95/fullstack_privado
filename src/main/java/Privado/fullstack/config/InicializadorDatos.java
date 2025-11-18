// Privado.fullstack.config.InicializadorDatos.java (Ajustado sin Enum)

package Privado.fullstack.config;

import Privado.fullstack.model.entity.Rol;
import Privado.fullstack.repository.RepositorioRol;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class InicializadorDatos implements CommandLineRunner {

    private final RepositorioRol repositorioRol;

    public InicializadorDatos(RepositorioRol repositorioRol) {
        this.repositorioRol = repositorioRol;
    }

    @Override
    public void run(String... args) throws Exception {
        inicializarRoles();
    }

    private void inicializarRoles() {
        // Se definen los roles como Strings, ya que el ejemplo del profesor no usa enum
        List<String> nombresRoles = List.of("ROLE_ADMIN", "ROLE_VENDOR", "ROLE_CLIENT");

        nombresRoles.forEach(nombreRol -> {
            // Se asume que RepositorioRol tiene un método findByName(String name)
            if (repositorioRol.findByName(nombreRol).isEmpty()) {

                Rol nuevoRol = new Rol(nombreRol); // Creación de Rol con String
                repositorioRol.save(nuevoRol);

                System.out.println("Rol creado: " + nombreRol);
            }
        });
    }
}