package Privado.fullstack.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControladorHolaMundo {

    @GetMapping("/api/privado/hola")
    public String HolaMundo(){
        return "Hola mundo privado";
    }
}
