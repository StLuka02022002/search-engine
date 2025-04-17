package searchengine.services.site;

import searchengine.entity.SiteEntity;
import searchengine.services.Service;

public interface SiteService extends Service<SiteEntity> {

    SiteEntity getByUrl(String url);
    boolean existsByUrl(String url);
    void deleteSite(SiteEntity site);
    void deleteSite(String url);
    SiteEntity createSite(SiteEntity site);
    SiteEntity updateSiteTime(SiteEntity site);
    void finishIndexing(SiteEntity site);
    void finishWithError(SiteEntity site, String error);
    void delete(String url);

}
