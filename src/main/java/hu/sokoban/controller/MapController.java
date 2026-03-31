package hu.sokoban.controller;

import hu.sokoban.dto.MapDto;
import hu.sokoban.model.SokobanMap;
import hu.sokoban.model.User;
import hu.sokoban.service.MapService;
import hu.sokoban.service.UserService;
import hu.sokoban.validation.MapValidator.MapValidationException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/maps")
public class MapController {

    private final MapService mapService;
    private final UserService userService;

    public MapController(MapService mapService, UserService userService) {
        this.mapService = mapService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("maps", mapService.findAll());
        return "maps/list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("mapDto", new MapDto());
        return "maps/upload";
    }

    @PostMapping("/upload")
    public String upload(@Valid @ModelAttribute("mapDto") MapDto dto,
                         BindingResult bindingResult,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "maps/upload";
        }

        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Felhasznalo nem talalhato"));

        try {
            SokobanMap created = mapService.create(dto, user);
            redirectAttributes.addFlashAttribute("success", "Palya sikeresen feltoltve!");
            return "redirect:/maps/" + created.getId();
        } catch (MapValidationException e) {
            bindingResult.rejectValue("mapData", "error.validation", e.getMessage());
            return "maps/upload";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        SokobanMap map = mapService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Palya nem talalhato: " + id));
        model.addAttribute("map", map);
        return "maps/view";
    }
}
