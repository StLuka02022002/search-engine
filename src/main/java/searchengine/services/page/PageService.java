package searchengine.services.page;

import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.exception.IndexException;
import searchengine.services.Service;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

public interface PageService extends Service<PageEntity> {

    boolean existsBySiteAndPath(SiteEntity site, String path);
    List<PageEntity> saveAll(Set<PageEntity> pages);
    PageEntity getBySiteAndPath(SiteEntity site, String path);
    PageEntity getPageByUrl(String url) throws MalformedURLException, IndexException;
    void index(String url) throws IndexException;
    void index(PageEntity page) throws IndexException;
    long count();
}
