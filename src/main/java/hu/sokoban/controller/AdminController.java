package hu.sokoban.controller;

import hu.sokoban.service.EvaluationService;
import hu.sokoban.service.MapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MapService mapService;
    private final EvaluationService evaluationService;

    public AdminController(MapService mapService, EvaluationService evaluationService) {
        this.mapService = mapService;
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("maps", mapService.findAll());
        model.addAttribute("benchmarkMaps", mapService.findBenchmarkMaps());
        return "admin/dashboard";
    }

    @PostMapping("/maps/{id}/toggle-benchmark")
    public String toggleBenchmark(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        mapService.toggleBenchmark(id);
        redirectAttributes.addFlashAttribute("success", "Benchmark statusz frissitve");
        return "redirect:/admin";
    }

    @PostMapping("/evaluate")
    public String runEvaluation(RedirectAttributes redirectAttributes) {
        evaluationService.evaluateAll();
        redirectAttributes.addFlashAttribute("success", "Kiertekeles lefutott!");
        return "redirect:/admin";
    }

    @PostMapping("/maps/{id}/delete")
    public String deleteMap(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        mapService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Palya torolve");
        return "redirect:/admin";
    }
}
