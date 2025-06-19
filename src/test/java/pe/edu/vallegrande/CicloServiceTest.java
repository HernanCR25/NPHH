package pe.edu.vallegrande.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.dto.HenDTO;
import pe.edu.vallegrande.model.CicloModel;
import pe.edu.vallegrande.repository.CicloRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CicloServiceTest {

    @Mock
    private CicloRepository cicloRepository;

    @InjectMocks
    private CicloService cicloService;

    @BeforeEach
    void setup() {
        // Para poder "espiar" o simular getHenFromExternal, vamos a crear un spy parcial del servicio.
        cicloService = Mockito.spy(new CicloService(cicloRepository));
    }

    @Test
    void testGetAllCiclos() {
        CicloModel c1 = new CicloModel();
        CicloModel c2 = new CicloModel();
        when(cicloRepository.findAll()).thenReturn(Flux.just(c1, c2));

        StepVerifier.create(cicloService.getAllCiclos())
                .expectNext(c1)
                .expectNext(c2)
                .verifyComplete();

        verify(cicloRepository).findAll();
        System.out.println("testGetAllCiclos pasó correctamente");
    }

    @Test
    void testGetCicloById() {
        CicloModel c = new CicloModel();
        when(cicloRepository.findById(1L)).thenReturn(Mono.just(c));

        StepVerifier.create(cicloService.getCicloById(1L))
                .expectNext(c)
                .verifyComplete();

        verify(cicloRepository).findById(1L);
        System.out.println("testGetCicloById pasó correctamente");
    }

    @Test
    void testCreateCiclo_withValidHenAndDayType() {
        CicloModel ciclo = new CicloModel();
        ciclo.setHenId(10L);
        ciclo.setTimes(5);
        ciclo.setTypeTime("Día");

        HenDTO henDTO = new HenDTO();
        henDTO.setArrivalDate(LocalDate.of(2023, 1, 1));

        doReturn(Mono.just(henDTO)).when(cicloService).getHenFromExternal(10L);
        when(cicloRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cicloService.createCiclo(ciclo))
                .assertNext(saved -> {
                    // endDate = arrivalDate + 5 días
                    assert saved.getEndDate().equals(LocalDate.of(2023, 1, 6));
                })
                .verifyComplete();

        verify(cicloService).getHenFromExternal(10L);
        verify(cicloRepository).save(ciclo);
        System.out.println("testCreateCiclo_withValidHenAndDayType pasó correctamente");
    }

    @Test
    void testCreateCiclo_withInvalidTypeTime() {
        CicloModel ciclo = new CicloModel();
        ciclo.setHenId(10L);
        ciclo.setTimes(5);
        ciclo.setTypeTime("Mes"); // tipo no válido

        HenDTO henDTO = new HenDTO();
        henDTO.setArrivalDate(LocalDate.of(2023, 1, 1));

        doReturn(Mono.just(henDTO)).when(cicloService).getHenFromExternal(10L);

        StepVerifier.create(cicloService.createCiclo(ciclo))
                .expectErrorMessage("Tipo de tiempo no válido: Mes")
                .verify();

        verify(cicloService).getHenFromExternal(10L);
        verify(cicloRepository, never()).save(any());
        System.out.println("testCreateCiclo_withInvalidTypeTime pasó correctamente");
    }

    @Test
    void testCreateCiclo_withNullArrivalDate() {
        CicloModel ciclo = new CicloModel();
        ciclo.setHenId(10L);
        ciclo.setTimes(5);
        ciclo.setTypeTime("Día");

        HenDTO henDTO = new HenDTO();
        henDTO.setArrivalDate(null);

        doReturn(Mono.just(henDTO)).when(cicloService).getHenFromExternal(10L);

        StepVerifier.create(cicloService.createCiclo(ciclo))
                .expectErrorMessage("arrivalDate is null for henId: 10")
                .verify();

        verify(cicloService).getHenFromExternal(10L);
        verify(cicloRepository, never()).save(any());
        System.out.println("testCreateCiclo_withNullArrivalDate pasó correctamente");
    }

    @Test
    void testDeleteCiclo() {
        when(cicloRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(cicloService.deleteCiclo(1L))
                .verifyComplete();

        verify(cicloRepository).deleteById(1L);
        System.out.println("testDeleteCiclo pasó correctamente");
    }

    @Test
    void testDeactivateCiclo() {
        CicloModel ciclo = new CicloModel();
        ciclo.setStatus("A");
        when(cicloRepository.findById(1L)).thenReturn(Mono.just(ciclo));
        when(cicloRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cicloService.deactivateCiclo(1L))
                .assertNext(updated -> {
                    assert "I".equals(updated.getStatus());
                })
                .verifyComplete();

        verify(cicloRepository).findById(1L);
        verify(cicloRepository).save(ciclo);
        System.out.println("testDeactivateCiclo pasó correctamente");
    }

    @Test
    void testActivateCiclo() {
        CicloModel ciclo = new CicloModel();
        ciclo.setStatus("I");
        when(cicloRepository.findById(1L)).thenReturn(Mono.just(ciclo));
        when(cicloRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cicloService.activateCiclo(1L))
                .assertNext(updated -> {
                    assert "A".equals(updated.getStatus());
                })
                .verifyComplete();

        verify(cicloRepository).findById(1L);
        verify(cicloRepository).save(ciclo);
        System.out.println("testActivateCiclo pasó correctamente");
    }

}
