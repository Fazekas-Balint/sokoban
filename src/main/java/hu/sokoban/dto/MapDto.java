package hu.sokoban.dto;

import hu.sokoban.model.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MapDto {

    @NotBlank(message = "A palya nevet add meg")
    @Size(max = 100, message = "A nev legfeljebb 100 karakter legyen")
    private String name;

    @NotBlank(message = "A palya megadasa kotelezo")
    private String mapData;

    private Difficulty difficulty;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMapData() { return mapData; }
    public void setMapData(String mapData) { this.mapData = mapData; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
}
