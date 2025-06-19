package pe.edu.vallegrande.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.model.CicloModel;
import pe.edu.vallegrande.service.CicloService;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@CrossOrigin("*")
@RestController
@RequestMapping("/cicloVida")
@RequiredArgsConstructor
public class CicloRest {

    private final CicloService cicloService;

    // Obtener todos los ciclos
    @GetMapping
    public Flux<CicloModel> getAllCiclos() {
        return cicloService.getAllCiclos();
    }
    
    @GetMapping("/Semanas")
    public Flux<CicloModel> getAllCycleLife() {
        return cicloService.getAllCycleLifeData();
    }

    // Obtener ciclo por ID
    @GetMapping("/{id}")
    public Mono<CicloModel> getCicloById(@PathVariable Long id) {
        return cicloService.getCicloById(id);
    }

    // Obtener ciclos por tipo de ITO
    @GetMapping("/type/{typeIto}")
    public Flux<CicloModel> getCiclosByTypeIto(@PathVariable String typeIto) {
        return cicloService.getCiclosByTypeIto(typeIto);
    }

    // Obtener ciclos activos
    @GetMapping("/activos")
    public Flux<CicloModel> getActiveCiclos() {
        return cicloService.getActiveCiclos();
    }

    // Obtener ciclos inactivos
    @GetMapping("/inactivos")
    public Flux<CicloModel> getInactiveCiclos() {
        return cicloService.getInactiveCiclos();
    }

    // Crear un nuevo ciclo con cálculo de endDate
    @PostMapping
    public Mono<CicloModel> createCiclo(@RequestBody CicloModel ciclo) {
        return cicloService.createCiclo(ciclo);  // El servicio se encargará de calcular el endDate
    }

    // Actualizar un ciclo existente
    @PutMapping("/update/{id}")
    public Mono<CicloModel> updateCiclo(@PathVariable Long id, @RequestBody CicloModel ciclo) {
        return cicloService.updateCiclo(id, ciclo);
    }

    // Eliminar un ciclo físicamente por ID
    @DeleteMapping("/{id}")
    public Mono<Void> deleteCiclo(@PathVariable Long id) {
        return cicloService.deleteCiclo(id);
    }

    // Inactivar un ciclo por ID (eliminación lógica)
    @PutMapping("/inactivar/{id}")
    public Mono<CicloModel> deactivateCiclo(@PathVariable Long id) {
        return cicloService.deactivateCiclo(id);
    }

    // Activar un ciclo por ID
    @PutMapping("/activar/{id}")
    public Mono<CicloModel> activateCiclo(@PathVariable Long id) {
        return cicloService.activateCiclo(id);
    }
}
