package searchengine.services.lemma;

import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.Service;

import java.util.List;

public interface LemmaService extends Service<LemmaEntity> {

    LemmaEntity getBySiteAndLemma(SiteEntity site, LemmaEntity lemma);
    LemmaEntity updateByPageAndLemma(PageEntity page, LemmaEntity lemma);
    LemmaEntity updateByPageAndLemma(PageEntity page, String lemma);
    List<LemmaEntity> getByLemmas(List<String> lemmas);
    void deleteByPage(PageEntity page);
    void deleteByPageAndLemma(PageEntity page, LemmaEntity lemma);
    void deleteAllById(List<Long> lemmas);
}
