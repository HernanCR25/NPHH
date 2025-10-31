package pe.edu.vallegrande.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.model.CicloModel;
import pe.edu.vallegrande.repository.CicloRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.dto.VaccineDTO;
import pe.edu.vallegrande.dto.HenDTO;
import pe.edu.vallegrande.dto.FoodDTO;
import org.springframework.http.MediaType;

import java.time.LocalDate;

@Service
public class CicloService {

    private final CicloRepository cicloRepository;

    @Autowired
    public CicloService(CicloRepository cicloRepository) {
        this.cicloRepository = cicloRepository;
    }

    // Obtener todos los ciclos mendiante la vista
    public Flux<CicloModel> getAllCycleLifeData() {
        return cicloRepository.findAllFromVista(); // Usar la vista
    }

    // Obtener todos los ciclos
    public Flux<CicloModel> getAllCiclos() {
        return cicloRepository.findAll();
    }

    // Obtener un ciclo por ID
    public Mono<CicloModel> getCicloById(Long id) {
        return cicloRepository.findById(id);
    }

    // Obtener ciclos por tipo de alimentación o vacunación
    public Flux<CicloModel> getCiclosByTypeIto(String typeIto) {
        return cicloRepository.findByTypeIto(typeIto);
    }

    // Obtener ciclos activos
    public Flux<CicloModel> getActiveCiclos() {
        return cicloRepository.findByStatus("A");
    }

    // Obtener ciclos inactivos
    public Flux<CicloModel> getInactiveCiclos() {
        return cicloRepository.findByStatus("I");
    }

    // Crear un nuevo ciclo
    //public Mono<CicloModel> createCiclo(CicloModel ciclo) {
      //  return cicloRepository.save(ciclo);
    //}
    // Método para guardar un ciclo y calcular el endDate
    public Mono<CicloModel> createCiclo(CicloModel ciclo) {
        return getHenFromExternal(ciclo.getHenId())  // Obtener la gallina desde el microservicio
            .flatMap(henDTO -> {
                LocalDate arrivalDate = henDTO.getArrivalDate();

                if (arrivalDate == null) {
                    return Mono.error(new RuntimeException("arrivalDate is null for henId: " + ciclo.getHenId()));
                }

                // Calcular el endDate según el tipo de tiempo y el número de veces
                switch (ciclo.getTypeTime()) {
                    case "Día":
                        ciclo.setEndDate(arrivalDate.plusDays(ciclo.getTimes()));
                        break;
                    case "Semana":
                        ciclo.setEndDate(arrivalDate.plusWeeks(ciclo.getTimes()));
                        break;
                    default:
                        return Mono.error(new RuntimeException("Tipo de tiempo no válido: " + ciclo.getTypeTime()));
                }

                // Guardar el ciclo de vida
                return cicloRepository.save(ciclo);
            });
    }

    // Actualizar un ciclo existente
    // Método para actualizar un ciclo y recalcular el endDate si es necesario
    public Mono<CicloModel> updateCiclo(Long id, CicloModel ciclo) {
        return getHenFromExternal(ciclo.getHenId())  // Obtener la gallina desde el microservicio
            .flatMap(henDTO -> {
                LocalDate arrivalDate = henDTO.getArrivalDate();

                if (arrivalDate == null) {
                    return Mono.error(new RuntimeException("arrivalDate is null for henId: " + ciclo.getHenId()));
                }

                // Recalcular el endDate según el tipo de tiempo y el número de veces
                switch (ciclo.getTypeTime()) {
                    case "Día":
                        ciclo.setEndDate(arrivalDate.plusDays(ciclo.getTimes()));
                        break;
                    case "Semana":
                        ciclo.setEndDate(arrivalDate.plusWeeks(ciclo.getTimes()));
                        break;
                    default:
                        return Mono.error(new RuntimeException("Tipo de tiempo no válido: " + ciclo.getTypeTime()));
                }

                // Actualizar el ciclo de vida con el nuevo endDate
                return cicloRepository.save(ciclo);
            });
    }


    // Eliminar un ciclo físicamente por ID
    public Mono<Void> deleteCiclo(Long id) {
        return cicloRepository.deleteById(id);
    }

    // Inactivar un ciclo por ID (eliminación lógica)
    public Mono<CicloModel> deactivateCiclo(Long id) {
        return cicloRepository.findById(id) // Buscar el ciclo por ID
                .flatMap(ciclo -> {
                    ciclo.setStatus("I"); // Cambiar estado a inactivo
                    return cicloRepository.save(ciclo); // Guardar cambios
                });
    }

    // Activar un ciclo por ID
    public Mono<CicloModel> activateCiclo(Long id) {
        return cicloRepository.findById(id)
                .flatMap(ciclo -> {
                    ciclo.setStatus("A");
                    return cicloRepository.save(ciclo);
                });
    }

    // WebClient para consumir datos de Vaccines
    private final WebClient vaccinesWebClient = WebClient.builder()
    .baseUrl("https://titulovaccine.onrender.com/vaccines") // Ajusta la URL si cambia
    .defaultHeader("Content-Type", "application/json")
    .build();

    // Método para consumir los datos de Shed desde otro microservicio
    public Mono<VaccineDTO> getVaccinesFromExternal(Long vaccineId) {
    return vaccinesWebClient.get()
        .uri("/{id}", vaccineId) // Usa el shedId como parámetro en la URL
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(VaccineDTO.class);  // Retorna un Mono con el DTO de Shed
    }

    // WebClient para consumir datos de HEN
    private final WebClient henWebClient = WebClient.builder()
    .baseUrl("https://nphh.onrender.com/hen") // Ajusta la URL si cambia
    .defaultHeader("Content-Type", "application/json")
    .build();

    // Método para consumir los datos de Shed desde otro microservicio
    public Mono<HenDTO> getHenFromExternal(Long henId) {
    return henWebClient.get()
        .uri("/{id}", henId) // Usa el shedId como parámetro en la URL
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(HenDTO.class);  // Retorna un Mono con el DTO de Shed
    }
        // WebClient para consumir datos de FOOD
    private final WebClient foodWebClient = WebClient.builder()
            .baseUrl("https://msfood.onrender.com/api/foods") // Ajusta la URL si cambia
            .defaultHeader("Content-Type", "application/json")
            .build();

    // Método para consumir los datos de Shed desde otro microservicio
    public Mono<FoodDTO> getFoodFromExternal(Long idFood) {
        return foodWebClient.get()
                .uri("/{id}", idFood) // Usa el idFood como parámetro en la URL
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FoodDTO.class);  // Retorna un Mono con el DTO de Shed
    }
}
