package Privado.fullstack.config;

import Privado.fullstack.model.entity.Producto;
import Privado.fullstack.repository.RepositorioProducto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CatalogoDataLoader implements CommandLineRunner {

    private final RepositorioProducto repositorioProducto;

    public CatalogoDataLoader(RepositorioProducto repositorioProducto) {
        this.repositorioProducto = repositorioProducto;
    }

    @Override
    public void run(String... args) throws Exception {

        if (repositorioProducto.count() > 0) {
            System.out.println("📦 Catálogo ya existe, no se cargará nuevamente.");
            return;
        }

        System.out.println("🛒 Cargando catálogo inicial de productos...");

        Producto p1 = new Producto();
        p1.setNombre("Catan");
        p1.setDescripcion("Un clásico juego de estrategia donde los jugadores compiten por colonizar la isla de Catan.");
        p1.setPrecio(new BigDecimal("29990"));
        p1.setStock(50);
        p1.setUrlImagen("https://i5.walmartimages.com/asr/a1863816-7553-441d-ad37-734a716ed55a.00a63b9153c188bd583dbfd05b191f90.jpeg");
        p1.setCategoria("juegos-de-mesa");

        Producto p2 = new Producto();
        p2.setNombre("Carcassonne");
        p2.setDescripcion("Juego de colocación de losetas ambientado en un reino medieval.");
        p2.setPrecio(new BigDecimal("24990"));
        p2.setStock(40);
        p2.setUrlImagen("https://dementegames.cl/9316-large_default/carcassonne.jpg");
        p2.setCategoria("juegos-de-mesa");

        Producto p3 = new Producto();
        p3.setNombre("Control Xbox Series X");
        p3.setDescripcion("Control inalámbrico con mejor ergonomía y precisión.");
        p3.setPrecio(new BigDecimal("59990"));
        p3.setStock(60);
        p3.setUrlImagen("https://assets.pcfactory.cl/public/foto/39440/1_1000.jpg?t=1752512513282");
        p3.setCategoria("accesorios");

        Producto p4 = new Producto();
        p4.setNombre("PlayStation 5");
        p4.setDescripcion("Consola de nueva generación con gráficos impresionantes.");
        p4.setPrecio(new BigDecimal("549990"));
        p4.setStock(20);
        p4.setUrlImagen("https://cl-dam-resizer.ecomm.cencosud.com/unsafe/adaptive-fit-in/3840x0/filters:quality(75)/cl/paris/670996999/variant/68472c20bf554683bcbb0e07/images/114c1658-dc6d-437d-a029-07947689fc27/670996999-0000-002.jpg");
        p4.setCategoria("consolas");

        Producto p5 = new Producto();
        p5.setNombre("PC Gamer ASUS ROG Strix");
        p5.setDescripcion("PC gamer de alto rendimiento para los más exigentes.");
        p5.setPrecio(new BigDecimal("1299990"));
        p5.setStock(15);
        p5.setUrlImagen("https://sipoonline.cl/wp-content/uploads/2024/05/Pc-Gamer-Asus-Strix_Intel-Core-i9-14900KF-64GB-DDR5-5600mhz-RTX-4090-24GB.png");
        p5.setCategoria("computadores-gamers");

        Producto p6 = new Producto();
        p6.setNombre("Silla Gamer Secretlab Titan");
        p6.setDescripcion("Silla ergonómica premium con excelente soporte.");
        p6.setPrecio(new BigDecimal("349990"));
        p6.setStock(25);
        p6.setUrlImagen("https://images-na.ssl-images-amazon.com/images/I/41vyYB3rS9L.jpg");
        p6.setCategoria("sillas-gamers");

        Producto p7 = new Producto();
        p7.setNombre("Mouse Logitech G502 HERO");
        p7.setDescripcion("Mouse gamer con sensor HERO de alta precisión.");
        p7.setPrecio(new BigDecimal("49990"));
        p7.setStock(70);
        p7.setUrlImagen("https://media.spdigital.cl/thumbnails/products/snbujg5__29f7dd61_thumbnail_4096.jpg");
        p7.setCategoria("mouse");

        Producto p8 = new Producto();
        p8.setNombre("Mousepad Razer Goliathus Chroma");
        p8.setDescripcion("Mousepad extendido RGB con superficie optimizada.");
        p8.setPrecio(new BigDecimal("29990"));
        p8.setStock(90);
        p8.setUrlImagen("https://dz019gex1wne1.cloudfront.net/_img_productos/mousepad-razer-goliathus-xl-foto.jpg");
        p8.setCategoria("mousepad");

        Producto p9 = new Producto();
        p9.setNombre("Polera Gamer Level-Up");
        p9.setDescripcion("Polera gamer personalizada con diseño exclusivo.");
        p9.setPrecio(new BigDecimal("14990"));
        p9.setStock(100);
        p9.setUrlImagen("https://www.gustore.cl/img/estampados/8305/8305_1.png");
        p9.setCategoria("poleras-personalizadas");

        // Guardar todos en BD
        repositorioProducto.saveAll(List.of(
                p1, p2, p3, p4, p5, p6, p7, p8, p9
        ));

        System.out.println("✅ Catálogo cargado correctamente.");
    }
}
