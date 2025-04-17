package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.IndexEntity;

import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {

    Optional<IndexEntity> findByPageIdAndLemmaId(Long page_id, Long lemma_id);

    boolean existsByPageIdAndLemmaId(Long page_id, Long lemma_id);
}
