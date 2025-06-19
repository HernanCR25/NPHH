package pe.edu.vallegrande.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("cycle_life")
public class CicloModel {

    @Id
    private Long id;

    @Column("hen_id")
    private Long henId; 

    @Column("type_ito")
    private String typeIto;

    @Column("name_ito")
    private String nameIto;

    @Column("type_time")
    private String typeTime;

    @Column("times")
    private Integer times;

    @Column("end_date") 
    private LocalDate endDate;

    @Column("status")
    private String status;

    @Transient
    private String timesInWeeks; 
    
public String getTimesInWeeks() {
    if (times == null) {
        return null;
    }
    
    if ("Semana".equals(typeTime)) {
        return typeTime + " " + times;
    }
    
    if ("Día".equals(typeTime)) {
        // Como "times" ya es un Integer, no necesitamos convertirlo
        int dias = times; // Usamos directamente el valor Integer
        int semana = (dias - 1) / 7 + 1; // Día 1-7 es semana 1, 8-14 semana 2, etc.
        return "Semana " + semana;
    }
    
    return null; // Si no coincide con ninguno, retorna null


}
}
