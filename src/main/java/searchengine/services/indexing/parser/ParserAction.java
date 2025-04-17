package searchengine.services.indexing.parser;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.context.ParserContext;
import searchengine.entity.PageEntity;
import searchengine.exception.ParserException;
import searchengine.services.indexing.storage.Storage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Slf4j
@Data
@RequiredArgsConstructor
public class ParserAction extends RecursiveAction {

    private final ParserContext parserContext;

    private Storage<PageEntity> storage;
    private PageEntity page;
    private boolean printError;

    @Override
    protected void compute() {
        String url = page.getSite().getUrl() + page.getPath();
        Parser parser = parserContext.getParser(url);

        // Не уверен в правильности решения
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        try {
            parser.parse();
            page.setCode(parser.getCode());
            page.setContent(parser.getContent());

            if (storage.add(page)) {
                log.info("Добавлена страница: {}", url);
                createNewTask(parser.getLinks());
            }
        } catch (ParserException e) {
            page.setCode(parser.getCode());
            page.setContent(parser.getContent());

            if (printError) {
                storage.add(page);
            }
        } catch (Exception e){
            log.error("Непредвиденная ошибка: {}",e.getMessage());
        }
    }

    private void createNewTask(List<String> links) {
        List<ParserAction> tasks = new ArrayList<>();
        for (String link : links) {
            PageEntity pageFromLink = new PageEntity();
            String normalizeLink = getNormalizeLink(link);

            if (normalizeLink != null) {
                pageFromLink.setSite(page.getSite());
                pageFromLink.setPath(normalizeLink);

                if (!storage.contains(pageFromLink)) {
                    ParserAction parserAction = parserContext.getParserAction();
                    parserAction.setStorage(storage);
                    parserAction.setPage(pageFromLink);
                    tasks.add(parserAction);
                }
            }
        }
        ForkJoinTask.invokeAll(tasks);
    }

    private String getNormalizeLink(String link) {
        try {
            URL url = new URL(link);
            if (page.getSite().getUrl().contains(url.getHost())) {
                return url.getPath();
            }
        } catch (MalformedURLException e) {
            log.warn("Некорректный URL: {}", link);
        }
        return null;
    }
}
