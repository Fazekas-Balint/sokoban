package hu.sokoban.controller;

import hu.sokoban.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final RankingService rankingService;

    public HomeController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("rankings", rankingService.calculateRanking());
        return "index";
    }

    @GetMapping("/ranking")
    public String ranking(Model model) {
        model.addAttribute("rankings", rankingService.calculateRanking());
        return "ranking";
    }
}
