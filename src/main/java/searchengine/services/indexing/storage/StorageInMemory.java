package searchengine.services.indexing.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class StorageInMemory implements Storage<PageEntity> {

    private final PageService service;
    private final SiteService siteService;
    private final Set<PageEntity> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicInteger previous = new AtomicInteger(0);

    @Value("${indexing-settings.count-page-to-update-site}")
    private int countPageToUpdateSite;

    @Override
    public List<PageEntity> save() {
        return service.saveAll(getSet());
    }

    @Override
    public boolean contains(PageEntity page) {
        return set.contains(page);
    }

    @Override
    public boolean add(PageEntity page) {
        if (set.add(page)) {
            update(page);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean remove(PageEntity page) {
        return set.remove(page);
    }

    @Override
    public void update(PageEntity page) {
        int currentSize = size();
        if (currentSize - previous.get() > countPageToUpdateSite) {
            SiteEntity site = page.getSite();
            siteService.updateSiteTime(site);
            previous.set(currentSize);
        }
    }

    @Override
    public Set<PageEntity> getSet() {
        return Collections.unmodifiableSet(set);
    }
}
