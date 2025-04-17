package searchengine.services.indexing.site;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SitesList;
import searchengine.config.context.IndexingContext;
import searchengine.exception.IndexingException;
import searchengine.mapper.SiteMapper;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class IndexingSitesService {

    private final IndexingContext indexingContext;
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;
    private final SiteMapper siteMapper;

    private boolean indexing;
    private List<IndexingSiteService> services;

    public void startIndexing() throws IndexingException {
        if (isIndexing()) {
            log.error("Запуск индексации при уже запущенной.");
            throw new IndexingException("Индексация уже запущена.");
        }

        log.info("Запуск индексации.");
        initialize();
        removeSites();
        servicesAction(IndexingSiteService::startIndexing);
        setIndexing(true);
        log.info("Индексация успешно запущена.");
    }

    @Transactional
    private void removeSites() {
        log.info("Удаление существующих данных из базы данных.");
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
        log.info("Удаление данных завершено.");
    }

    private void initialize() {
        if (services == null) {
            log.info("Инициализация списка сервисов индексации.");
            services = indexingContext.getIndexingSiteServices(
                    sitesList.getSites().stream()
                            .map(siteMapper::siteToSiteEntity)
                            .toList()
            );
            log.info("Инициализация завершена. Количество сервисов индексации: {}", services.size());
        }
    }

    public void stopIndexing() throws IndexingException {
        if (services == null || !isIndexing()) {
            log.error("Остановка индексации при ещё не запущенной.");
            throw new IndexingException("Индексация ещё не запущена.");
        }

        log.info("Остановка индексации.");
        servicesAction(IndexingSiteService::stopIndexing);
        servicesAction(IndexingSiteService::saveResult);
        setIndexing(false);
        log.info("Индексация успешно остановлена.");
    }

    private void servicesAction(ServiceAction action) throws IndexingException {
        List<IndexingException> exceptions = new ArrayList<>();
        services.forEach(service -> {
            try {
                action.execute(service);
            } catch (IndexingException e) {
                log.error("Ошибка при выполнении действия {} для сервиса: {}", action, service, e);
                exceptions.add(e);
            }
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    public boolean isIndexing() {
        if (services == null) {
            return false;
        }
        if (indexing) {
            List<IndexingSiteService> indexingServices = services.stream()
                    .filter(IndexingSiteService::isIndexing)
                    .toList();
            indexing = !indexingServices.isEmpty();
        }
        return indexing;
    }

    @FunctionalInterface
    private interface ServiceAction {
        void execute(IndexingSiteService service) throws IndexingException;
    }
}
