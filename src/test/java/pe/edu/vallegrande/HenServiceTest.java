package pe.edu.vallegrande.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pe.edu.vallegrande.model.HenModel;
import pe.edu.vallegrande.repository.HenRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;

public class HenServiceTest {

    private HenRepository henRepository;
    private HenService henService;

    @BeforeEach
    void setUp() {
        henRepository = Mockito.mock(HenRepository.class);
        henService = new HenService(henRepository);
    }

    @Test
    void testGetAllHens() {
        HenModel hen = new HenModel(1L, LocalDate.now(), 100, "A", 1L);
        Mockito.when(henRepository.findAll()).thenReturn(Flux.just(hen));

        StepVerifier.create(henService.getAllHens())
                .expectNext(hen)
                .verifyComplete();

        System.out.println("✅ test obtener todas las gallinas pasó");
    }

    @Test
    void testGetHenById() {
        HenModel hen = new HenModel(1L, LocalDate.now(), 100, "A", 1L);
        Mockito.when(henRepository.findById(1L)).thenReturn(Mono.just(hen));

        StepVerifier.create(henService.getHenById(1L))
                .expectNext(hen)
                .verifyComplete();

        System.out.println("✅ test obtener gallina por ID pasó");
    }

    @Test
    void testFindByArrivalDate() {
        LocalDate date = LocalDate.now();
        HenModel hen = new HenModel(1L, date, 100, "A", 1L);
        Mockito.when(henRepository.findByArrivalDate(date)).thenReturn(Flux.just(hen));

        StepVerifier.create(henService.findByArrivalDate(date))
                .expectNext(hen)
                .verifyComplete();

        System.out.println("✅ test búsqueda por fecha pasó");
    }

    @Test
    void testGetActiveHens() {
        HenModel hen = new HenModel(1L, LocalDate.now(), 100, "A", 1L);
        Mockito.when(henRepository.findByStatus("A")).thenReturn(Flux.just(hen));

        StepVerifier.create(henService.getActiveHens())
                .expectNext(hen)
                .verifyComplete();

        System.out.println("✅ test obtener gallinas activas pasó");
    }

    @Test
    void testCreateHen() {
        HenModel hen = new HenModel(null, LocalDate.now(), 100, "A", 1L);
        HenModel savedHen = new HenModel(1L, hen.getArrivalDate(), 100, "A", 1L);
        Mockito.when(henRepository.save(hen)).thenReturn(Mono.just(savedHen));

        StepVerifier.create(henService.createHen(hen))
                .expectNext(savedHen)
                .verifyComplete();

        System.out.println("✅ test crear gallina pasó");
    }

    @Test
    void testUpdateHen() {
        HenModel existingHen = new HenModel(1L, LocalDate.now(), 50, "I", 2L);
        HenModel updatedData = new HenModel(null, LocalDate.now(), 100, "A", 1L);
        HenModel updatedHen = new HenModel(1L, updatedData.getArrivalDate(), 100, "A", 1L);

        Mockito.when(henRepository.findById(1L)).thenReturn(Mono.just(existingHen));
        Mockito.when(henRepository.save(any(HenModel.class))).thenReturn(Mono.just(updatedHen));

        StepVerifier.create(henService.updateHen(1L, updatedData))
                .expectNext(updatedHen)
                .verifyComplete();

        System.out.println("✅ test actualizar gallina pasó");
    }

    @Test
    void testDeleteHen() {
        Mockito.when(henRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(henService.deleteHen(1L))
                .verifyComplete();

        System.out.println("✅ test eliminar gallina pasó");
    }

    @Test
    void testDeactivateHen() {
        HenModel hen = new HenModel(1L, LocalDate.now(), 100, "A", 1L);
        HenModel deactivated = new HenModel(1L, hen.getArrivalDate(), hen.getQuantity(), "I", hen.getShedId());

        Mockito.when(henRepository.findById(1L)).thenReturn(Mono.just(hen));
        Mockito.when(henRepository.save(any(HenModel.class))).thenReturn(Mono.just(deactivated));

        StepVerifier.create(henService.deactivateHen(1L))
                .expectNext(deactivated)
                .verifyComplete();

        System.out.println("✅ test desactivar gallina pasó");
    }

    @Test
    void testActivateHen() {
        HenModel hen = new HenModel(1L, LocalDate.now(), 100, "I", 1L);
        HenModel activated = new HenModel(1L, hen.getArrivalDate(), hen.getQuantity(), "A", hen.getShedId());

        Mockito.when(henRepository.findById(1L)).thenReturn(Mono.just(hen));
        Mockito.when(henRepository.save(any(HenModel.class))).thenReturn(Mono.just(activated));

        StepVerifier.create(henService.activateHen(1L))
                .expectNext(activated)
                .verifyComplete();

        System.out.println("✅ test activar gallina pasó");
    }
}
