package hu.sokoban.service;

import hu.sokoban.dto.RankingEntry;
import java.util.List;

public interface RankingService {

    List<RankingEntry> calculateRanking();
}
