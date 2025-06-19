package pe.edu.vallegrande.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.model.HenModel;
import pe.edu.vallegrande.repository.HenRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate; 
import pe.edu.vallegrande.dto.ShedDTO;
import org.springframework.http.MediaType;

@Service
public class HenService {

    private final HenRepository henRepository;

    @Autowired
    public HenService(HenRepository henRepository) {
        this.henRepository = henRepository;
    }

    // Obtener todas las gallinas
    public Flux<HenModel> getAllHens() {
        return henRepository.findAll();
    }

    // Obtener una gallina por ID
    public Mono<HenModel> getHenById(Long id) {
        return henRepository.findById(id);
    }

    // Obtener una gallina por fecha
    public Flux<HenModel> findByArrivalDate(LocalDate arrivalDate) {
        return henRepository.findByArrivalDate(arrivalDate);
    }
    // Obtener gallinas activas
    public Flux<HenModel> getActiveHens() {
        return henRepository.findByStatus("A");
    }

    // Obtener gallinas inactivas
    public Flux<HenModel> getInactiveHens() {
        return henRepository.findByStatus("I");
    }

    // Crear una nueva gallina
    public Mono<HenModel> createHen(HenModel hen) {
        return henRepository.save(hen);
    }

    // Actualizar una gallina existente
    public Mono<HenModel> updateHen(Long id, HenModel hen) {
        return henRepository.findById(id)
                .flatMap(existingHen -> {
                    existingHen.setArrivalDate(hen.getArrivalDate()); // Manejo de LocalDate
                    existingHen.setQuantity(hen.getQuantity());
                    existingHen.setStatus(hen.getStatus());
                    existingHen.setShedId(hen.getShedId());
                    return henRepository.save(existingHen);
                });
    }

    // Eliminar una gallina físicamente por ID
    public Mono<Void> deleteHen(Long id) {
        return henRepository.deleteById(id);
    }

    // Inactivar una gallina por ID (eliminación lógica)
    public Mono<HenModel> deactivateHen(Long id) {
        return henRepository.findById(id)
                .flatMap(hen -> {
                    hen.setStatus("I");
                    return henRepository.save(hen);
                });
    }

    // Activar una gallina por ID
    public Mono<HenModel> activateHen(Long id) {
        return henRepository.findById(id)
                .flatMap(hen -> {
                    hen.setStatus("A");
                    return henRepository.save(hen);
                });
    }

    // WebClient para consumir datos de Shed
    private final WebClient shedWebClient = WebClient.builder()
    .baseUrl("https://vaccine-z4vj.onrender.com/NPH/sheds") // Ajusta la URL si cambia
    .defaultHeader("Content-Type", "application/json")
    .build();

    // Método para consumir los datos de Shed desde otro microservicio
    public Mono<ShedDTO> getShedFromExternal(Long shedId) {
    return shedWebClient.get()
        .uri("/{id}", shedId) // Usa el shedId como parámetro en la URL
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(ShedDTO.class);  // Retorna un Mono con el DTO de Shed
    }
}
