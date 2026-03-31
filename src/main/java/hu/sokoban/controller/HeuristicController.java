package hu.sokoban.controller;

import hu.sokoban.dto.HeuristicDto;
import hu.sokoban.engine.SolutionResult;
import hu.sokoban.model.Heuristic;
import hu.sokoban.model.SokobanMap;
import hu.sokoban.model.User;
import hu.sokoban.model.enums.HeuristicStatus;
import hu.sokoban.repository.EvaluationResultRepository;
import hu.sokoban.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/heuristics")
public class HeuristicController {

    private final HeuristicService heuristicService;
    private final UserService userService;
    private final MapService mapService;
    private final EvaluationService evaluationService;
    private final EvaluationResultRepository resultRepository;

    public HeuristicController(HeuristicService heuristicService,
                               UserService userService,
                               MapService mapService,
                               EvaluationService evaluationService,
                               EvaluationResultRepository resultRepository) {
        this.heuristicService = heuristicService;
        this.userService = userService;
        this.mapService = mapService;
        this.evaluationService = evaluationService;
        this.resultRepository = resultRepository;
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        User user = getUser(principal);
        model.addAttribute("heuristics", heuristicService.findByUser(user));
        return "heuristics/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        HeuristicDto dto = new HeuristicDto();
        dto.setSourceCode(getTemplate());
        model.addAttribute("heuristicDto", dto);
        return "heuristics/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("heuristicDto") HeuristicDto dto,
                         BindingResult bindingResult,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "heuristics/form";
        }

        User user = getUser(principal);
        Heuristic heuristic = heuristicService.create(dto, user);

        if (heuristic.getStatus() == HeuristicStatus.ERROR) {
            redirectAttributes.addFlashAttribute("error", "Forditasi hiba: " + heuristic.getCompilationError());
        } else {
            redirectAttributes.addFlashAttribute("success", "Sikeresen leforditva!");
        }
        return "redirect:/heuristics/" + heuristic.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Heuristic heuristic = heuristicService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Heurisztika nem talalhato: " + id));
        model.addAttribute("heuristic", heuristic);
        model.addAttribute("results", resultRepository.findByHeuristicOrderByEvaluatedAtDesc(heuristic));
        model.addAttribute("maps", mapService.findBenchmarkMaps());
        return "heuristics/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Heuristic heuristic = heuristicService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Heurisztika nem talalhato: " + id));
        HeuristicDto dto = new HeuristicDto();
        dto.setName(heuristic.getName());
        dto.setSourceCode(heuristic.getSourceCode());
        model.addAttribute("heuristicDto", dto);
        model.addAttribute("heuristicId", id);
        return "heuristics/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("heuristicDto") HeuristicDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "heuristics/form";
        }

        Heuristic heuristic = heuristicService.update(id, dto);

        if (heuristic.getStatus() == HeuristicStatus.ERROR) {
            redirectAttributes.addFlashAttribute("error", "Forditasi hiba: " + heuristic.getCompilationError());
        } else {
            redirectAttributes.addFlashAttribute("success", "Sikeresen frissitve es leforditva!");
        }
        return "redirect:/heuristics/" + id;
    }

    @PostMapping("/{heuristicId}/test/{mapId}")
    public String testRun(@PathVariable Long heuristicId,
                          @PathVariable Long mapId,
                          RedirectAttributes redirectAttributes) {
        Heuristic heuristic = heuristicService.findById(heuristicId)
                .orElseThrow(() -> new IllegalArgumentException("Heurisztika nem talalhato"));
        SokobanMap map = mapService.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Palya nem talalhato"));

        try {
            SolutionResult result = evaluationService.evaluateSingle(heuristic, map);
            if (result.isSolved()) {
                redirectAttributes.addFlashAttribute("testResult",
                        "Megoldva %.2f sec alatt, %d csomopont kiterjesztve, %d lepes".formatted(
                                result.getExecutionTimeMs() / 1000.0,
                                result.getNodesExpanded(),
                                result.getSolutionLength()));
            } else if (result.isTimedOut()) {
                redirectAttributes.addFlashAttribute("testResult",
                        "Nem sikerult megoldani az idolimiten belul (%d csomopont kiterjesztve)".formatted(
                                result.getNodesExpanded()));
            } else {
                redirectAttributes.addFlashAttribute("testResult", "Nem talalhato megoldas");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba a futtatas kozben: " + e.getMessage());
        }

        return "redirect:/heuristics/" + heuristicId;
    }

    private User getUser(Principal principal) {
        return userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Felhasznalo nem talalhato"));
    }

    private String getTemplate() {
        return """
                public int heur(SokobanState state) {
                    // Ide ird a heuristzikadat!
                    // Elerheto metodusok:
                    //   state.getPlayerRow(), state.getPlayerCol()
                    //   state.isWall(r, c), state.isGoal(r, c), state.isBox(r, c)
                    //   state.getRows(), state.getCols()
                    //   state.getBoxCount(), state.getGoalCount()
                    return 0;
                }""";
    }
}
