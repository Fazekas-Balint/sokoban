package hu.sokoban.repository;

import hu.sokoban.model.SokobanMap;
import hu.sokoban.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SokobanMapRepository extends JpaRepository<SokobanMap, Long> {

    List<SokobanMap> findByUploadedByOrderByUploadedAtDesc(User user);

    List<SokobanMap> findByBenchmarkTrueOrderByIdAsc();

    List<SokobanMap> findAllByOrderByUploadedAtDesc();
}
