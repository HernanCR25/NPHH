package pe.edu.vallegrande.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FoodDTO {

    @JsonProperty("id_food")
    private Long idFood;

    public Long getIdFood() {
        return idFood;
    }

    public void setIdFood(Long idFood) {
        this.idFood = idFood;
    }

    @Override
    public String toString() {
        return "FoodDTO{" +
               "idFood=" + idFood +
               '}';
    }
}

