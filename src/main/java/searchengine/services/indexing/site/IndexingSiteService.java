package searchengine.services.indexing.site;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import searchengine.config.context.ParserContext;
import searchengine.config.context.StorageContext;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.exception.IndexException;
import searchengine.exception.IndexingException;
import searchengine.exception.StartIndexingException;
import searchengine.exception.StopIndexingException;
import searchengine.services.indexing.parser.ParserAction;
import searchengine.services.indexing.storage.Storage;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Data
@Service
@Scope("prototype")
@RequiredArgsConstructor
public class IndexingSiteService {

    private final StorageContext storageContext;
    private final SiteService siteService;
    private final ParserContext parserContext;
    private final PageService pageService;

    private SiteEntity site;
    private PageEntity startPage;
    private Storage<PageEntity> storage;
    private ParserAction parserAction;
    private ForkJoinPool pool;
    private boolean indexing;
    private boolean finished;

    @Value("${indexing-settings.count-page-to-update-site}")
    private int countPageToUpdateSite;

    public void startIndexing() throws IndexingException {
        if (isIndexing()) {
            throw new StartIndexingException("Индексация уже запущена.");
        }

        site = siteService.createSite(site);
        initialize();

        Thread thread = new Thread(this::startPool);
        thread.setDaemon(true);
        thread.start();

        setFinished(false);
        setIndexing(true);

        log.info("Индексация начата для сайта: {}", site.getUrl());
    }

    private void initialize() {
        startPage = new PageEntity();
        startPage.setSite(site);
        startPage.setPath("/");

        storage = storageContext.getStorage();
        parserAction = parserContext.getParserAction();

        pool = new ForkJoinPool();
        log.info("Инициализация завершена. Начальная страница: {}", startPage);
    }

    private void startPool() {
        parserAction.setPage(startPage);
        parserAction.setStorage(storage);

        log.info("Запуск pool потоков индексации.");
        pool.invoke(parserAction);

        if (!isFinished()) {
            setFinished(true);
            setIndexing(false);
            log.info("Самостоятельное завершение pool потоков индексации");
            finishWithoutError();
            saveResult();
        }
    }

    private void finish() {
        setFinished(true);
        pool.shutdownNow();
    }

    private void finishWithError(String error) {
        siteService.finishWithError(site, error);
        finish();
        log.error("Индексация завершена с ошибкой: {}", error);
    }

    private void finishWithoutError() {
        siteService.finishIndexing(site);
        finish();
        log.info("Индексация завершена успешно.");
    }

    public void saveResult() {
        indexingPage(storage.save());
    }

    public void indexingPage(List<PageEntity> pages) {
        int numberOfThreads = pages.size() / 100 + 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (PageEntity page : pages) {
            executorService.submit(() -> {
                try {
                    pageService.index(page);
                } catch (IndexException e) {
                    log.error("Ошибка индексации страницы: {}", page, e);
                }
            });
        }
        executorService.shutdown();
    }

    public void stopIndexing() throws IndexingException {
        if (isFinished()) {
            return;
        } else if (!isIndexing()) {
            throw new StopIndexingException("Индексация не запущена.");
        } else {
            finishWithError("Индексация прервана пользователем.");
        }
        setIndexing(false);
    }
}
