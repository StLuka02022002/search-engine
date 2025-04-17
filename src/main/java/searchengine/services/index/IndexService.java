package searchengine.services.index;

import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.services.Service;

public interface IndexService extends Service<IndexEntity> {

    IndexEntity getByPageAndLemma(PageEntity page, LemmaEntity lemma);

    void saveByPageAndLemmaAndFrequency(PageEntity page, LemmaEntity lemma, Float frequency);

    boolean existsByPageAndLemma(PageEntity page, LemmaEntity lemma);
}
