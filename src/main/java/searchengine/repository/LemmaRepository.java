package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.LemmaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity,Long> {

    List<LemmaEntity> findByLemma(String lemma);

    List<LemmaEntity> findAllByLemmaIn(List<String> lemma);

    Optional<LemmaEntity> findBySiteIdAndLemma(Long id, String lemma);

}
