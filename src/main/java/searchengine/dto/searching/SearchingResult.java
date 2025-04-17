package searchengine.dto.searching;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SearchingResult {

    private boolean result;
    private int count;

    @Builder.Default
    private List<SearchingData> data = new ArrayList<>();
}
