package hu.sokoban.controller;

import hu.sokoban.model.User;
import hu.sokoban.service.HeuristicService;
import hu.sokoban.service.MapService;
import hu.sokoban.service.RankingService;
import hu.sokoban.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;
    private final HeuristicService heuristicService;
    private final MapService mapService;
    private final RankingService rankingService;

    public ProfileController(UserService userService, HeuristicService heuristicService,
                             MapService mapService, RankingService rankingService) {
        this.userService = userService;
        this.heuristicService = heuristicService;
        this.mapService = mapService;
        this.rankingService = rankingService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Felhasznalo nem talalhato"));

        model.addAttribute("user", user);
        model.addAttribute("heuristics", heuristicService.findByUser(user));
        model.addAttribute("maps", mapService.findByUser(user));
        model.addAttribute("rankings", rankingService.calculateRanking());
        return "profile";
    }
}
