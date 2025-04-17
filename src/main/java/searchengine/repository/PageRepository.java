package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.PageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    List<PageEntity> findByPath(String path);

    Optional<PageEntity> findBySiteIdAndPath(Long id, String path);

    boolean existsBySiteIdAndPath(Long id, String path);

}
