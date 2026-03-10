package hu.sokoban.controller;

import hu.sokoban.dto.RegistrationDto;
import hu.sokoban.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registration", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") RegistrationDto dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.match", "A jelszavak nem egyeznek");
        }
        if (userService.existsByUsername(dto.getUsername())) {
            bindingResult.rejectValue("username", "error.exists", "Ez a felhasznalonev mar foglalt");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.exists", "Ez az email cim mar regisztralva van");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(dto);
        redirectAttributes.addFlashAttribute("success", "Sikeres regisztracio! Most mar bejelentkezhetsz.");
        return "redirect:/login";
    }
}
