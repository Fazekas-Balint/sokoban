package hu.sokoban.service;

import hu.sokoban.dto.MapDto;
import hu.sokoban.model.SokobanMap;
import hu.sokoban.model.User;
import java.util.List;
import java.util.Optional;

public interface MapService {

    SokobanMap create(MapDto dto, User uploader);

    Optional<SokobanMap> findById(Long id);

    List<SokobanMap> findAll();

    List<SokobanMap> findByUser(User user);

    List<SokobanMap> findBenchmarkMaps();

    void toggleBenchmark(Long mapId);

    void delete(Long mapId);
}
