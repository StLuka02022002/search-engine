package searchengine.services.site;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.SiteEntity;
import searchengine.entity.StatusSite;
import searchengine.repository.SiteRepository;
import searchengine.services.AbstractService;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SiteServiceImpl extends AbstractService<SiteEntity, SiteRepository> implements SiteService {

    public SiteServiceImpl(SiteRepository repository) {
        super(repository);
    }

    @Override
    public SiteEntity update(Long id, SiteEntity site) {
        site.setId(id);
        SiteEntity updatedSite = save(site);
        return updatedSite;
    }

    @Override
    public SiteEntity getByUrl(String url) {
        return repository.findByUrl(url).orElse(null);
    }

    @Override
    @Transactional
    public void deleteSite(SiteEntity site) {
        if (site.getId() == null) {
            deleteSite(site.getUrl());
        } else {
            delete(site.getId());
        }
    }

    @Override
    public void deleteSite(String url) {
        delete(url);
    }

    @Override
    public boolean existsByUrl(String url) {
        return repository.existsByUrl(url);
    }

    @Override
    public SiteEntity createSite(SiteEntity site) {
        SiteEntity nuwSite = (site.getId() != null) ? get(site.getId()) : getByUrl(site.getUrl());
        if (nuwSite == null) {
            nuwSite = site;
        }
        nuwSite.setStatus(StatusSite.INDEXING);
        return updateSiteTime(nuwSite);
    }

    @Override
    public SiteEntity updateSiteTime(SiteEntity site) {
        site.setStatusTime(LocalDateTime.now());
        SiteEntity updatedSite = save(site);
        return updatedSite;
    }

    @Override
    public void finishIndexing(SiteEntity site) {
        site.setStatus(StatusSite.INDEXED);
        updateSiteTime(site);
        log.info("Индексация сайта с ID {} завершена успешно.", site.getId());
    }

    @Override
    public void finishWithError(SiteEntity site, String error) {
        site.setStatus(StatusSite.FAILED);
        site.setLastError(error);
        updateSiteTime(site);
        log.error("Индексация сайта с ID {} завершилась с ошибкой: {}", site.getId(), error);
    }

    @Override
    public void delete(String url) {
        repository.deleteByUrl(url);
    }
}
