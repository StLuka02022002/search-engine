package searchengine.dto.searching;

import lombok.Builder;
import lombok.Data;
import searchengine.entity.SiteEntity;

@Data
@Builder
public class SearchingQuery {

    private String query;
    private SiteEntity site;

    @Builder.Default
    private int offset = 0;

    @Builder.Default
    private int limit = 20;
}
