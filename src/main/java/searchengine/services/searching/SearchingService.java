package searchengine.services.searching;

import searchengine.dto.searching.SearchingData;
import searchengine.dto.searching.SearchingQuery;
import searchengine.entity.SiteEntity;

import java.util.List;

public interface SearchingService {

    List<SearchingData> getResult(String query, SiteEntity site);

    List<SearchingData> getResult(String query, List<SiteEntity> sites);

    List<SearchingData> getResult(SearchingQuery searchingQuery);
}
