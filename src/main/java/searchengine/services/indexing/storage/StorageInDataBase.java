package searchengine.services.indexing.storage;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.entity.PageEntity;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class StorageInDataBase implements Storage<PageEntity> {

    private static final Logger log = LoggerFactory.getLogger(StorageInDataBase.class);
    private final PageService pageService;
    private final SiteService siteService;

    @Override
    public List<PageEntity> save() {
        return List.of();
    }

    @Override
    public boolean contains(PageEntity page) {
        return pageService.existsBySiteAndPath(page.getSite(), page.getPath());
    }

    @Override
    public boolean add(PageEntity page) {
        if (!contains(page)) {
            try {
                PageEntity newPage = pageService.save(page);
                pageService.index(newPage);
                update(newPage);
            } catch (Exception e) {
                log.error("Ошибка сохранения или индексации страницы {}. Ошибка: {}", page, e.getMessage());
            }
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return (int) pageService.count();
    }

    @Override
    public boolean remove(PageEntity page) {
        if (pageService.exists(page.getId())) {
            pageService.delete(page.getId());
            return true;
        }
        return false;
    }

    @Override
    public void update(PageEntity page) {
        siteService.updateSiteTime(page.getSite());
    }

    @Override
    public Set<PageEntity> getSet() {
        return new HashSet<>(pageService.getAll());
    }
}
