package searchengine.services.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.context.ParserContext;
import searchengine.config.context.StorageContext;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.exception.IndexException;
import searchengine.repository.PageRepository;
import searchengine.services.AbstractService;
import searchengine.services.indexing.page.IndexingPageService;
import searchengine.services.indexing.parser.Parser;
import searchengine.services.indexing.storage.Storage;
import searchengine.services.site.SiteService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PageServiceImpl extends AbstractService<PageEntity, PageRepository> implements PageService {

    private final SiteService siteService;
    private final IndexingPageService indexingPageService;
    private final ParserContext parserContext;

    public PageServiceImpl(PageRepository repository, SiteService siteService,
                           IndexingPageService indexingPageService, ParserContext parserContext) {
        super(repository);
        this.siteService = siteService;
        this.indexingPageService = indexingPageService;
        this.parserContext = parserContext;
    }

    @Override
    public PageEntity update(Long id, PageEntity page) {
        page.setId(id);
        return save(page);
    }

    @Override
    public boolean existsBySiteAndPath(SiteEntity site, String path) {
        return repository.existsBySiteIdAndPath(site.getId(), path);
    }

    @Override
    public List<PageEntity> saveAll(Set<PageEntity> pages) {
        List<PageEntity> savedPages = repository.saveAll(pages);
        log.info("Сохранено {} страниц.", savedPages.size());
        return savedPages;
    }

    @Override
    public PageEntity getBySiteAndPath(SiteEntity site, String path) {
        return repository.findBySiteIdAndPath(site.getId(), path).orElse(null);
    }

    @Override
    public void index(String url) throws IndexException {
        try {
            PageEntity page = getPageByUrl(url);
            indexAsync(page);
        } catch (IndexException e) {
            log.error("Не удалось проиндексировать URL {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    private void indexAsync(PageEntity page) throws IndexException {
        try {
            log.info("Начато индексирование страницы с ID {}.", page.getId());
            indexingPageService.indexAsync(page);
        } catch (IndexException e) {
            log.error("Не удалось проиндексировать страницу с ID {}: {}", page.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void index(PageEntity page) throws IndexException {
        try {
            log.info("Начато индексирование страницы с ID {}.", page.getId());
            indexingPageService.index(page);
        } catch (IndexException e) {
            log.error("Не удалось проиндексировать страницу с ID {}: {}", page.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public long count() {
        long count = repository.count();
        log.debug("Общее количество страниц в репозитории: {}", count);
        return count;
    }

    @Override
    public PageEntity getPageByUrl(String url) throws IndexException {
        try {
            URL nuwUrl = new URL(url);
            String path = nuwUrl.getPath();
            SiteEntity site = getSiteByUrl(nuwUrl);
            if (site != null) {
                PageEntity page = repository.findBySiteIdAndPath(site.getId(), path)
                        .orElse(null);
                if (page == null) {
                    Parser parser = parserContext.getParser(url);
                    parser.parse();
                    page = new PageEntity();
                    page.setSite(site);
                    page.setPath(nuwUrl.getPath());
                    page.setCode(parser.getCode());
                    page.setContent(parser.getContent());
                    page = this.save(page);
                }
                return page;
            }
            log.error("Некорректный сайт: {}", url);
            throw this.getException();
        } catch (MalformedURLException e) {
            log.error("Некорректный URL: {}", url, e);
            throw new IndexException("Некорректный URL: " + url);
        }
    }

    private SiteEntity getSiteByUrl(URL url) throws MalformedURLException {
        String siteUrl = url.getProtocol() + "://" + url.getHost();
        return siteService.getByUrl(siteUrl);
    }

    private IndexException getException() {
        return new IndexException("Данная страница находится за пределами сайтов, " +
                "указанных в конфигурационном файле");
    }
}
