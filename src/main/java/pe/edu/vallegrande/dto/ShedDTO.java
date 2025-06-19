package pe.edu.vallegrande.dto;

public class ShedDTO {
    private Long shedId;  // Campo para el shedId

    // Getter y Setter para shedId
    public Long getShedId() {
        return shedId;
    }

    public void setShedId(Long shedId) {
        this.shedId = shedId;
    }

    // Método toString para depuración
    @Override
    public String toString() {
        return "ShedDTO{" +
               "shedId=" + shedId +
               '}';
    }
}