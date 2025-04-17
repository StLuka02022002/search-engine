package searchengine.services.searching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.context.LemmaContext;
import searchengine.dto.searching.SearchingData;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.dto.searching.SearchingQuery;
import searchengine.entity.SiteEntity;
import searchengine.exception.SearchingException;
import searchengine.services.lemma.LemmaSearcher;
import searchengine.services.lemma.LemmaService;
import searchengine.services.site.SiteService;
import searchengine.services.snippet.SnippetService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {

    private final LemmaService lemmaService;
    private final SnippetService snippetService;
    private final SiteService siteService;
    private final LemmaSearcher lemmaSearcher = LemmaContext.getLemmaSearcher();

    @Value("${searching-settings.limit-lemma-frequency}")
    private int limitLemmaFrequency;

    private final int parallelism = Runtime.getRuntime().availableProcessors();

    @Override
    public List<SearchingData> getResult(SearchingQuery searchingQuery) throws SearchingException {
        log.info("Поиск по запросу '{}'", searchingQuery.getQuery());
        if (searchingQuery.getSite() == null) {
            return getResult(searchingQuery.getQuery(), siteService.getAll());
        } else {
            return getResult(searchingQuery.getQuery(), searchingQuery.getSite());
        }
    }

    @Override
    public List<SearchingData> getResult(String query, SiteEntity site) throws SearchingException {
        if (query == null || query.trim().isEmpty()) {
            throw new SearchingException("Задан пустой поисковый запрос.");
        }

        List<LemmaEntity> lemmas = filterLemmasForSite(getLemmasFromQuery(query), site);

        if (lemmas.isEmpty()) {
            log.warn("По запросу '{}' не найдено лемм для сайта '{}'.", query, site.getUrl());
            throw new SearchingException("По данному запросу ничего не найдено.");
        }

        return getResult(lemmas, site);
    }

    @Override
    public List<SearchingData> getResult(String query, List<SiteEntity> sites) throws SearchingException {
        List<SearchingData> searchingDataList = sites.stream()
                .flatMap(site -> {
                    try {
                        return getResult(query, site).stream();
                    } catch (SearchingException e) {
                        log.error("Ошибка при поиске по запросу '{}' для сайта '{}': {}", query, site.getUrl(), e.getMessage());
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        if (searchingDataList.isEmpty()) {
            log.warn("По запросу '{}' не найдено результатов для указанных сайтов.", query);
            throw new SearchingException("По данному запросу ничего не найдено.");
        }
        return searchingDataList;
    }

    @Transactional
    public List<SearchingData> getResult(List<LemmaEntity> lemmas, SiteEntity site) {
        List<LemmaEntity> lemmaEntities = filterLemmasForSite(lemmas, site);
        Map<PageEntity, Float> pagesWithRank = getPagesWithRank(lemmaEntities);
        float maxRank = getMaxRank(pagesWithRank.values());

        List<SearchingData> result = pagesWithRank.entrySet().parallelStream()
                .flatMap(entry -> getSearchingData(entry.getKey(), lemmaEntities, entry.getValue() / maxRank).stream())
                .collect(Collectors.toList());

        log.info("Найдено {} результатов для сайта '{}'.", result.size(), site.getUrl());
        return result;
    }

    private List<SearchingData> getSearchingData(PageEntity page, List<LemmaEntity> lemmas, float relevance) {
        List<String> snippets = getSnippets(page, lemmas);
        return snippets.parallelStream()
                .map(snippet -> getSearchingData(page, snippet, relevance))
                .collect(Collectors.toList());
    }

    private SearchingData getSearchingData(PageEntity page, String snippet, float relevance) {
        SiteEntity site = page.getSite();
        return SearchingData.builder()
                .site(site.getUrl())
                .siteName(site.getName())
                .uri(page.getPath())
                .title(getTitle(page))
                .snippet(snippet)
                .relevance(relevance)
                .build();
    }

    private List<String> getSnippets(PageEntity page, List<LemmaEntity> lemmaEntities) {
        List<String> lemmas = lemmaEntities.stream()
                .map(LemmaEntity::getLemma)
                .collect(Collectors.toList());
        String text = lemmaSearcher.htmlClear(page.getContent());
        return snippetService.getSnippets(text, lemmas);
    }

    @Transactional
    private Map<PageEntity, Float> getPagesWithRank(List<LemmaEntity> lemmas) {
        return lemmas.parallelStream()
                .flatMap(lemma -> lemma.getIndices().stream())
                .collect(Collectors.toConcurrentMap(
                        IndexEntity::getPage,
                        IndexEntity::getRank,
                        Float::sum
                ));
    }

    private float getMaxRank(Collection<Float> ranks) {
        return ranks.parallelStream()
                .max(Float::compareTo)
                .orElse(1f);
    }

    private List<LemmaEntity> getLemmasFromQuery(String query) {
        Map<String, Integer> lemmas = lemmaSearcher.getLemmas(query);
        return lemmaService.getByLemmas(new ArrayList<>(lemmas.keySet()));
    }

    private List<LemmaEntity> filterLemmasForSite(List<LemmaEntity> lemmas, SiteEntity site) {
        return lemmas.parallelStream()
                .filter(lemma -> lemma.getFrequency() < limitLemmaFrequency)
                .filter(lemma -> lemma.getSite().getId().equals(site.getId()))
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .collect(Collectors.toList());
    }

    private String getTitle(PageEntity page) {
        Document document = Jsoup.parse(page.getContent());
        return Optional.of(document.title())
                .filter(title -> !title.isEmpty())
                .orElseGet(() -> Optional.ofNullable(document.selectFirst("h1"))
                        .map(Element::text)
                        .orElse(""));
    }
}
