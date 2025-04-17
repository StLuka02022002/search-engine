package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.SiteEntity;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {

    boolean existsByUrl(String url);
    void deleteByUrl(String url);
    Optional<SiteEntity> findByUrl(String url);
}
