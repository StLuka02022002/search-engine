package searchengine.services.index;

import org.springframework.stereotype.Service;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.repository.IndexRepository;
import searchengine.services.AbstractService;

@Service
public class IndexServiceImpl extends AbstractService<IndexEntity, IndexRepository> implements IndexService {

    public IndexServiceImpl(IndexRepository repository) {
        super(repository);
    }

    @Override
    public IndexEntity update(Long id, IndexEntity index) {
        index.setId(id);
        return save(index);
    }

    @Override
    public IndexEntity getByPageAndLemma(PageEntity page, LemmaEntity lemma) {
        return repository.findByPageIdAndLemmaId(page.getId(), lemma.getId()).orElse(null);
    }

    @Override
    public void saveByPageAndLemmaAndFrequency(PageEntity page, LemmaEntity lemma, Float frequency) {
        IndexEntity index = new IndexEntity();
        index.setLemma(lemma);
        index.setPage(page);
        index.setRank(frequency.floatValue());
        save(index);
    }

    @Override
    public boolean existsByPageAndLemma(PageEntity page, LemmaEntity lemma) {
        return repository.existsByPageIdAndLemmaId(page.getId(), lemma.getId());
    }
}
