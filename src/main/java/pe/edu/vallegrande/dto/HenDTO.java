package pe.edu.vallegrande.dto;

import java.time.LocalDate;

public class HenDTO {
    private Long henId;  // Campo para el ID de la gallina
    private LocalDate arrivalDate;  // Campo para la fecha de llegada

    // Getter y Setter para henId
    public Long getHenId() {
        return henId;
    }

    public void setHenId(Long henId) {
        this.henId = henId;
    }

    // Getter y Setter para arrivalDate
    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    // Método toString para depuración
    @Override
    public String toString() {
        return "HenDTO{" +
               "henId=" + henId +
               ", arrivalDate=" + arrivalDate +
               '}';
    }
}
