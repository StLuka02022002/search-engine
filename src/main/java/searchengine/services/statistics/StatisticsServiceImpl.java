package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.entity.SiteEntity;
import searchengine.entity.StatusSite;
import searchengine.services.site.SiteService;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteService siteService;

    @Override
    public StatisticsResponse getStatistics() {
        log.debug("Начало сбора статистики по всем сайтам.");

        List<SiteEntity> sites = siteService.getAll();

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        sites.forEach(site -> {
            int pages = site.getPages().size();
            int lemmas = site.getLemmas().size();
            long statsTime = site.getStatusTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            StatusSite status = site.getStatus();
            String error = site.getLastError();
            error = error == null ? "" : error;

            detailed.add(DetailedStatisticsItem.builder()
                    .name(site.getName())
                    .url(site.getUrl())
                    .pages(pages)
                    .lemmas(lemmas)
                    .status(status.toString())
                    .statusTime(statsTime)
                    .error(error)
                    .build());

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);

            if (status != StatusSite.INDEXED) {
                total.setIndexing(false);
            }
        });

        StatisticsData statisticsData = StatisticsData.builder()
                .total(total)
                .detailed(detailed)
                .build();

        return StatisticsResponse.builder()
                .statistics(statisticsData)
                .result(true)
                .build();
    }
}
