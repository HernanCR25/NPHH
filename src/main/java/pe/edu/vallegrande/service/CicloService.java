package pe.edu.vallegrande.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.dto.FoodDTO;
import pe.edu.vallegrande.dto.HenDTO;
import pe.edu.vallegrande.dto.VaccineDTO;
import pe.edu.vallegrande.model.CicloModel;
import pe.edu.vallegrande.repository.CicloRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
public class CicloService {

    private final CicloRepository cicloRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    public CicloService(CicloRepository cicloRepository) {
        this.cicloRepository = cicloRepository;
    }

    // Obtener todos los ciclos mediante la vista
    public Flux<CicloModel> getAllCycleLifeData() {
        return cicloRepository.findAllFromVista();
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

    // Crear un nuevo ciclo con cálculo de endDate
    public Mono<CicloModel> createCiclo(CicloModel ciclo) {
        return getHenFromExternal(ciclo.getHenId())
            .flatMap(henDTO -> {
                LocalDate arrivalDate = henDTO.getArrivalDate();

                if (arrivalDate == null) {
                    return Mono.error(new RuntimeException("arrivalDate is null for henId: " + ciclo.getHenId()));
                }

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

                return cicloRepository.save(ciclo);
            });
    }

    // Actualizar un ciclo existente
    public Mono<CicloModel> updateCiclo(Long id, CicloModel ciclo) {
        return getHenFromExternal(ciclo.getHenId())
            .flatMap(henDTO -> {
                LocalDate arrivalDate = henDTO.getArrivalDate();

                if (arrivalDate == null) {
                    return Mono.error(new RuntimeException("arrivalDate is null for henId: " + ciclo.getHenId()));
                }

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

                return cicloRepository.save(ciclo);
            });
    }

    // Eliminar un ciclo físicamente
    public Mono<Void> deleteCiclo(Long id) {
        return cicloRepository.deleteById(id);
    }

    // Inactivar un ciclo (eliminación lógica)
    public Mono<CicloModel> deactivateCiclo(Long id) {
        return cicloRepository.findById(id)
                .flatMap(ciclo -> {
                    ciclo.setStatus("I");
                    return cicloRepository.save(ciclo);
                });
    }

    // Activar un ciclo
    public Mono<CicloModel> activateCiclo(Long id) {
        return cicloRepository.findById(id)
                .flatMap(ciclo -> {
                    ciclo.setStatus("A");
                    return cicloRepository.save(ciclo);
                });
    }

    // ==============================
    // Llamadas externas con JWT
    // ==============================

    public Mono<VaccineDTO> getVaccinesFromExternal(Long vaccineId) {
        return Mono.deferContextual(ctxView -> {
            String token = ctxView.getOrDefault("Authorization", null);
            if (token == null) {
                return Mono.error(new RuntimeException("JWT token not found in context"));
            }

            return webClientBuilder.build()
                    .get()
                    .uri("https://titulovaccine.onrender.com/vaccines/{id}", vaccineId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(VaccineDTO.class);
        });
    }

    public Mono<HenDTO> getHenFromExternal(Long henId) {
        return Mono.deferContextual(ctxView -> {
            String token = ctxView.getOrDefault("Authorization", null);
            if (token == null) {
                return Mono.error(new RuntimeException("JWT token not found in context"));
            }

            return webClientBuilder.build()
                    .get()
                    .uri("https://nphh.onrender.com/hen/{id}", henId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(HenDTO.class);
        });
    }

    public Mono<FoodDTO> getFoodFromExternal(Long idFood) {
        return Mono.deferContextual(ctxView -> {
            String token = ctxView.getOrDefault("Authorization", null);
            if (token == null) {
                return Mono.error(new RuntimeException("JWT token not found in context"));
            }

            return webClientBuilder.build()
                    .get()
                    .uri("https://msfood.onrender.com/api/foods/{id}", idFood)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(FoodDTO.class);
        });
    }
}
