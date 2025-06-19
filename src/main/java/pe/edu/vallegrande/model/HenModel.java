package pe.edu.vallegrande.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("hens")
public class HenModel {

    @Id
    private Long id;

    @Column("arrival_date")
    private LocalDate arrivalDate;

    @Column("quantity")
    private Integer quantity;

    @Column("status")
    private String status;

    @Column("shed_id")
    private Long shedId;
}


