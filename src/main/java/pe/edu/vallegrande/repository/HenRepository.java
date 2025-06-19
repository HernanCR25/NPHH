package pe.edu.vallegrande.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.model.HenModel;
import reactor.core.publisher.Flux;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;


@Repository
public interface HenRepository extends ReactiveCrudRepository<HenModel, Long> {

    // Buscar ciclos por su estado (activo o inactivo)
    Flux<HenModel> findByStatus(String status);

    @Query("SELECT * FROM hens WHERE arrival_date = :arrivalDate")
    Flux<HenModel> findByArrivalDate(LocalDate arrivalDate);
}

