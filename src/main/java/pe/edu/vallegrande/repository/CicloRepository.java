package pe.edu.vallegrande.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.model.CicloModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;

@Repository
public interface CicloRepository extends ReactiveCrudRepository<CicloModel, Long> {

    // Buscar ciclos por tipo de alimentación o vacunación
    Flux<CicloModel> findByTypeIto(String typeIto);

    // Buscar ciclos por su estado (activo o inactivo)
    Flux<CicloModel> findByStatus(String status);

    // Inactivar un ciclo por ID
    @Modifying
    @Query("UPDATE cycle_life SET status = 'I' WHERE id = :id")
    Mono<Void> deactivateCiclo(Long id);

    @Query("SELECT * FROM view_cycle_life")  
    Flux<CicloModel> findAllFromVista();
}

