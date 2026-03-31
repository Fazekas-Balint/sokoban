package hu.sokoban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HeuristicDto {

    @NotBlank(message = "A heurisztika nevet add meg")
    @Size(max = 100, message = "A nev legfeljebb 100 karakter legyen")
    private String name;

    @NotBlank(message = "A forraskod megadasa kotelezo")
    private String sourceCode;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
}
