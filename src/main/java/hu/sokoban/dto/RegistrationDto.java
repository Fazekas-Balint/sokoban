package hu.sokoban.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationDto {

    @NotBlank(message = "A felhasznalonev megadasa kotelezo")
    @Size(min = 3, max = 50, message = "A felhasznalonev 3-50 karakter hosszu legyen")
    private String username;

    @NotBlank(message = "Az email megadasa kotelezo")
    @Email(message = "Ervenyes email cimet adj meg")
    private String email;

    @NotBlank(message = "A jelszo megadasa kotelezo")
    @Size(min = 6, message = "A jelszo legalabb 6 karakter legyen")
    private String password;

    @NotBlank(message = "A jelszo megerositese kotelezo")
    private String confirmPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
