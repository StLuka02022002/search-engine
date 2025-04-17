package searchengine.services.lemma;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.repository.LemmaRepository;
import searchengine.services.AbstractService;
import searchengine.services.index.IndexService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LemmaServiceImpl extends AbstractService<LemmaEntity, LemmaRepository> implements LemmaService {

    private final IndexService indexService;

    public LemmaServiceImpl(LemmaRepository repository, IndexService indexService) {
        super(repository);
        this.indexService = indexService;
    }

    @Override
    public LemmaEntity update(Long id, LemmaEntity lemma) {
        lemma.setId(id);
        return save(lemma);
    }

    @Override
    public LemmaEntity getBySiteAndLemma(SiteEntity site, LemmaEntity lemma) {
        return repository.findBySiteIdAndLemma(site.getId(), lemma.getLemma())
                .orElse(null);
    }

    @Override
    @Transactional
    public LemmaEntity updateByPageAndLemma(PageEntity page, LemmaEntity lemma) {
        LemmaEntity existingLemma = getBySiteAndLemma(page.getSite(), lemma);

        if (existingLemma == null) {
            lemma.setSite(page.getSite());
            lemma.setFrequency(1);
            try {
                return save(lemma);
            } catch (DataIntegrityViolationException ex) {
                return getBySiteAndLemma(page.getSite(), lemma);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        if (!indexService.existsByPageAndLemma(page, existingLemma)) {
            existingLemma.setFrequency(existingLemma.getFrequency() + 1);
            return save(existingLemma);
        }

        return null;
    }


    @Override
    public LemmaEntity updateByPageAndLemma(PageEntity page, String lemma) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setLemma(lemma);
        return updateByPageAndLemma(page, lemmaEntity);
    }

    @Override
    public List<LemmaEntity> getByLemmas(List<String> lemmas) {
        return repository.findAllByLemmaIn(lemmas);
    }

    @Override
    public void deleteByPageAndLemma(PageEntity page, LemmaEntity lemma) {
        LemmaEntity existingLemma = getBySiteAndLemma(page.getSite(), lemma);
        if (existingLemma != null) {
            delete(existingLemma.getId());
            log.info("Удалена лемма с ID {} из страницы ID {}.", existingLemma.getId(), page.getId());
        } else {
            log.warn("Лемма с ID {} не найдена на странице ID {} для удаления.", lemma.getId(), page.getId());
        }
    }

    @Override
    public void deleteByPage(PageEntity page) {
        List<Long> deleteLemmasId = new ArrayList<>();
        //TODO переделать запрос напрямую в базе
        Hibernate.initialize(page.getIndices());
        page.getIndices().stream()
                .map(IndexEntity::getLemma)
                .forEach(lemma -> {
                    lemma.setFrequency(lemma.getFrequency() - 1);
                    if (lemma.getFrequency() == 0) {
                        deleteLemmasId.add(lemma.getId());
                        log.debug("Лемма с ID {} имеет частоту 0 и будет удалена.", lemma.getId());
                    } else {
                        save(lemma);
                        log.debug("Обновлена частота леммы с ID {}: {}", lemma.getId(), lemma.getFrequency());
                    }
                });

        if (!deleteLemmasId.isEmpty()) {
            deleteAllById(deleteLemmasId);
        }
    }

    @Override
    public void deleteAllById(List<Long> lemmasId) {
        repository.deleteAllById(lemmasId);
        log.info("Удалены леммы с ID: {}", lemmasId);
    }
}
