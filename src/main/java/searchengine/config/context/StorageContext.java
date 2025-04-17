package searchengine.config.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import searchengine.entity.PageEntity;
import searchengine.exception.IndexingException;
import searchengine.services.indexing.storage.Storage;
import searchengine.services.indexing.storage.StorageInDataBase;
import searchengine.services.indexing.storage.StorageInMemory;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageContext {

    private final PageService pageService;
    private final SiteService siteService;

    @Value("${indexing-settings.storage.type}")
    private String storageType;

    // Получить нужную реализацию работы либо через Hash, либо через DataBase
    public Storage<PageEntity> getStorage() {
        log.info("Выбор типа хранилища: {}", storageType);
        switch (storageType) {
            case "memory":
                return new StorageInMemory(pageService, siteService);
            case "database":
                log.debug("Выбран тип хранилища 'database'. Создание StorageInDataBase.");
                return new StorageInDataBase(pageService, siteService);
            default:
                String errorMsg = "Не указан тип storage.";
                log.error("Ошибка выбора хранилища: {}", errorMsg);
                throw new IndexingException(errorMsg);
        }
    }
}
