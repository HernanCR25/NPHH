package pe.edu.vallegrande.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.model.HenModel;
import pe.edu.vallegrande.service.HenService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


@CrossOrigin("*")
@RestController
@RequestMapping("/hen")
@RequiredArgsConstructor
public class HenRest {

    private final HenService henService;

    // Obtener todas las gallinas
    @GetMapping
    public Flux<HenModel> getAllHens() {
        return henService.getAllHens();
    }

    // Obtener una gallina por ID
    @GetMapping("/{id}")
    public Mono<HenModel> getHenById(@PathVariable Long id) {
        return henService.getHenById(id);
    }

    // Obtener gallinas activas
    @GetMapping("/activos")
    public Flux<HenModel> getActiveHens() {
        return henService.getActiveHens();
    }

    // Obtener gallinas inactivas
    @GetMapping("/inactivos")
    public Flux<HenModel> getInactiveHens() {
        return henService.getInactiveHens();
    }

    // Crear una nueva gallina
    @PostMapping
    public Mono<HenModel> createHen(@RequestBody HenModel hen) {
        return henService.createHen(hen);
    }

    // Actualizar una gallina existente
    @PutMapping("/update/{id}")
    public Mono<HenModel> updateHen(@PathVariable Long id, @RequestBody HenModel hen) {
        return henService.updateHen(id, hen);
    }

    // Eliminar una gallina físicamente por ID
    @DeleteMapping("/{id}")
    public Mono<Void> deleteHen(@PathVariable Long id) {
        return henService.deleteHen(id);
    }

    // Inactivar una gallina por ID (eliminación lógica)
    @PutMapping("/inactivar/{id}")
    public Mono<HenModel> deactivateHen(@PathVariable Long id) {
        return henService.deactivateHen(id);
    }

    // Activar una gallina por ID
    @PutMapping("/activar/{id}")
    public Mono<HenModel> activateHen(@PathVariable Long id) {
        return henService.activateHen(id);
    }

    @GetMapping("/buscar/{arrivalDate}")
    public Flux<HenModel> getHensByArrivalDate(@PathVariable String arrivalDate) {
        try {
            LocalDate date = LocalDate.parse(arrivalDate); // Convertir String a LocalDate
            return henService.findByArrivalDate(date);
        } catch (DateTimeParseException e) {
            return Flux.empty(); // Retorna vacío si la fecha no es válida
        }
    }
}
