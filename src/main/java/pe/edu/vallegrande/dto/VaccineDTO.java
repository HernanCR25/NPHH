package pe.edu.vallegrande.dto;

public class VaccineDTO {
    private Long vaccineId;  // Solo se usará el ID de la vacuna

    // Getter y Setter
    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }

    // toString para depuración
    @Override
    public String toString() {
        return "VaccineDTO{" +
               "vaccineId=" + vaccineId +
               '}';
    }
}
