package searchengine.services.snippet;

import java.util.List;

public interface SnippetService {

    List<String> getSnippetsByWord(String text, String lemma);

    List<String> getSnippets(String text, List<String> lemmas);

}
