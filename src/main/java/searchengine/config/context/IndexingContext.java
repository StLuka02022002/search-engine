package searchengine.config.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.entity.SiteEntity;
import searchengine.services.indexing.site.IndexingSiteService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IndexingContext {

    private final PageService pageService;
    private final SiteService siteService;

    public IndexingSiteService getIndexingSiteService() {
        return SpringContext.getBean(IndexingSiteService.class);
    }

    public List<IndexingSiteService> getIndexingSiteServices(List<SiteEntity> sites) {
        return sites.stream().map(siteEntity -> {
            IndexingSiteService service = getIndexingSiteService();
            System.out.println(service);
            service.setSite(siteEntity);
            return service;
        }).toList();
    }
}
