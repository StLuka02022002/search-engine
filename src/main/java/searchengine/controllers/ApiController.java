package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.message.ErrorMessage;
import searchengine.dto.message.Message;
import searchengine.dto.searching.SearchingData;
import searchengine.dto.searching.SearchingResult;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.searching.SearchingQuery;
import searchengine.entity.SiteEntity;
import searchengine.exception.IndexException;
import searchengine.exception.SearchingException;
import searchengine.services.searching.SearchingService;
import searchengine.services.page.PageService;
import searchengine.services.statistics.StatisticsService;
import searchengine.services.indexing.site.IndexingSitesService;
import searchengine.services.site.SiteService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingSitesService indexingSitesService;
    private final SearchingService searchingService;
    private final PageService pageService;
    private final SiteService siteService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("Запрос на получение статистики");
        StatisticsResponse response = statisticsService.getStatistics();
        log.info("Статистика успешно получена");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Message> startIndexing() {
        log.info("Запрос на запуск индексации");
        indexingSitesService.startIndexing();
        log.info("Индексация успешно запущена");
        return ResponseEntity.ok(new Message(true));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Message> stopIndexing() {
        log.info("Запрос на остановку индексации");
        indexingSitesService.stopIndexing();
        log.info("Индексация успешно остановлена");
        return ResponseEntity.ok(new Message(true));
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Message> indexPage(@RequestParam String url) {
        log.info("Запрос на индексацию страницы: {}", url);
        try {
            pageService.index(url);
            log.info("Страница успешно проиндексирована: {}", url);
            return ResponseEntity.ok(new Message(true));
        } catch (IndexException e) {
            return ResponseEntity.ok(new ErrorMessage(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, @RequestParam @Nullable String site,
                                    @RequestParam int offset, @RequestParam int limit) {
        log.info("Запрос на поиск. Запрос: '{}', Сайт: '{}', Смещение: {}, Лимит: {}", query, site, offset, limit);
        SiteEntity siteEntity = (site != null) ? siteService.getByUrl(site) : null;

        if (siteEntity == null && site != null) {
            log.warn("Поиск завершился неудачей. Сайт '{}' не найден", site);
            throw new SearchingException("Указанная страница не найдена");
        }

        SearchingQuery searchingQuery = SearchingQuery.builder()
                .query(query)
                .site(siteEntity)
                .offset(offset)
                .limit(limit)
                .build();

        List<SearchingData> searchingDataList = searchingService.getResult(searchingQuery);

        SearchingResult searchingResult = SearchingResult.builder()
                .result(true)
                .count(searchingDataList.size())
                .data(searchingDataList.stream()
                        .skip(offset)
                        .limit(limit)
                        .toList())
                .build();

        log.info("Поиск завершен успешно. Найдено результатов: {}", searchingDataList.size());
        return ResponseEntity.ok(searchingResult);
    }

}
