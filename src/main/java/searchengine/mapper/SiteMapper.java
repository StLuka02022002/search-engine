package searchengine.mapper;

import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.entity.SiteEntity;

@Component
public class SiteMapper {

    public Site siteEntityToSite(SiteEntity siteEntity) {
        Site site = new Site();
        site.setName(siteEntity.getName());
        site.setUrl(siteEntity.getUrl());
        return site;
    }

    public SiteEntity siteToSiteEntity(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        return siteEntity;
    }
}
