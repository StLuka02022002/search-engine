package searchengine.services.indexing.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.exception.IndexException;
import searchengine.services.index.IndexService;
import searchengine.services.lemma.LemmaSearcher;
import searchengine.services.lemma.LemmaService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingPageService {

    private final IndexService indexService;
    private final LemmaService lemmaService;
    private final LemmaSearcher lemmaSearcher;

    public void index(PageEntity page) throws IndexException {
        if (page.getContent() != null && page.getCode() < 400) {
            try {
                deleteLemmas(page);
                updateLemmas(page);
            } catch (Exception e) {
                log.error("Ошибка при индексации страницы с ID {}: {}", page.getId(), e.getMessage(), e);
                throw new IndexException("Ошибка при индексации страницы");
            }
        } else {
            log.warn("Страница с ID {} имеет недопустимый контент или код ответа: {}", page.getId(), page.getCode());
        }
    }

    public void indexAsync(PageEntity page) throws IndexException {
        if (page.getContent() != null && page.getCode() < 400) {
            try {
                startUpdateLemmas(page);
            } catch (Exception e) {
                log.error("Ошибка при индексации страницы с ID {}: {}", page.getId(), e.getMessage(), e);
                throw new IndexException("Ошибка при индексации страницы");
            }
        } else {
            log.warn("Страница с ID {} имеет недопустимый контент или код ответа: {}", page.getId(), page.getCode());
        }
    }

    public void startUpdateLemmas(PageEntity page) {
        deleteLemmas(page);
        Thread updateLemmansThread = new Thread(() -> {
            updateLemmas(page);
        });
        updateLemmansThread.setDaemon(true);
        updateLemmansThread.start();
    }

    private void updateLemmas(PageEntity page) {
        String cleanedText = lemmaSearcher.htmlClear(page.getContent());
        Map<String, Integer> lemmas = lemmaSearcher.getLemmas(cleanedText);
        lemmas.forEach((lemma, frequency) -> {
                    try {
                        LemmaEntity lemmaEntity = lemmaService.updateByPageAndLemma(page, lemma);
                        indexService.saveByPageAndLemmaAndFrequency(page, lemmaEntity, (float) frequency);
                        log.info("Обновлена лемма '{}' для страница ID {}. Частота леммы: {}", lemma, page.getId(), frequency);
                    } catch (Exception e) {
                        log.error("Ошибка при обновлении леммы '{}' для страницы ID {}: {}", lemma, page.getId(), e.getMessage());
                    }

                }
        );
        log.info("Страница успешно проиндексирована: {}", page.getId());
    }

    private void deleteLemmas(PageEntity page) {
        lemmaService.deleteByPage(page);
    }
}
