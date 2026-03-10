package hu.sokoban.service.impl;

import hu.sokoban.dto.MapDto;
import hu.sokoban.model.SokobanMap;
import hu.sokoban.model.User;
import hu.sokoban.repository.SokobanMapRepository;
import hu.sokoban.service.MapService;
import hu.sokoban.validation.MapValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MapServiceImpl implements MapService {

    private final SokobanMapRepository mapRepository;
    private final MapValidator mapValidator;

    public MapServiceImpl(SokobanMapRepository mapRepository, MapValidator mapValidator) {
        this.mapRepository = mapRepository;
        this.mapValidator = mapValidator;
    }

    @Override
    public SokobanMap create(MapDto dto, User uploader) {
        mapValidator.validate(dto.getMapData());

        String[] lines = dto.getMapData().split("\n", -1);
        int rows = lines.length;
        int cols = 0;
        for (String line : lines) {
            cols = Math.max(cols, line.length());
        }

        SokobanMap map = new SokobanMap(dto.getName(), dto.getMapData(), rows, cols, uploader);
        map.setDifficulty(dto.getDifficulty());
        return mapRepository.save(map);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SokobanMap> findById(Long id) {
        return mapRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SokobanMap> findAll() {
        return mapRepository.findAllByOrderByUploadedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SokobanMap> findByUser(User user) {
        return mapRepository.findByUploadedByOrderByUploadedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SokobanMap> findBenchmarkMaps() {
        return mapRepository.findByBenchmarkTrueOrderByIdAsc();
    }

    @Override
    public void toggleBenchmark(Long mapId) {
        SokobanMap map = mapRepository.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Palya nem talalhato: " + mapId));
        map.setBenchmark(!map.isBenchmark());
        mapRepository.save(map);
    }

    @Override
    public void delete(Long mapId) {
        mapRepository.deleteById(mapId);
    }
}
