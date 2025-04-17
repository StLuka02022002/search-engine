package searchengine.services.snippet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import searchengine.config.context.LemmaContext;
import searchengine.services.lemma.LemmaSearcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetServiceImpl implements SnippetService {

    private final LemmaSearcher lemmaSearcher = LemmaContext.getLemmaSearcher();

    @Value("${searching-settings.length-snippet}")
    private int lengthSnippet;

    @Override
    public List<String> getSnippetsByWord(String text, String word) {
        log.debug("Поиск сниппетов для слова '{}'", word);

        return IntStream.iterate(text.indexOf(word), index -> index != -1, index -> text.indexOf(word, index + 1))
                .mapToObj(index -> getSnippet(text, index))
                .peek(snippet -> log.debug("Найден сниппет: {}", snippet))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSnippets(String text, List<String> lemmas) {
        log.debug("Поиск сниппетов для лемм {} в тексте длиной {} символов", lemmas, text.length());

        Map<String, List<Integer>> wordsWithIndices = getWordsWithIndices(text);
        List<Integer> indices = wordsWithIndices.entrySet().parallelStream()
                .filter(entry -> lemmas.contains(lemmaSearcher.getNormalForms(entry.getKey())))
                .flatMap(entry -> entry.getValue().parallelStream())
                .sorted()
                .collect(Collectors.toList());

        log.debug("Найдено {} индексов для лемм", indices.size());
        return getSnippetsByIndices(text, indices);
    }

    private Map<String, List<Integer>> getWordsWithIndices(String text) {
        log.debug("Индексация слов в тексте длиной {} символов", text.length());

        Map<String, List<Integer>> wordsWithIndices = new ConcurrentHashMap<>();
        String normalizedText = text.toLowerCase(Locale.ROOT);

        Pattern pattern = Pattern.compile("[а-яa-z]+");
        Matcher matcher = pattern.matcher(normalizedText);

        while (matcher.find()) {
            String word = matcher.group();
            int start = matcher.start();
            wordsWithIndices.computeIfAbsent(word, list -> Collections.synchronizedList(new ArrayList<>())).add(start);
        }

        log.debug("Индексация завершена: найдено {} уникальных слов", wordsWithIndices.size());
        return wordsWithIndices;
    }

    private List<String> getSnippetsByIndices(String text, List<Integer> indices) {
        List<String> snippets = new ArrayList<>();
        List<Integer> currentIndices = new ArrayList<>();

        for (int index : indices) {
            if (currentIndices.isEmpty() || index < currentIndices.get(0) + lengthSnippet) {
                currentIndices.add(index);
            } else {
                snippets.add(getSnippet(text, currentIndices));
                currentIndices.clear();
                currentIndices.add(index);
            }
        }

        if (!currentIndices.isEmpty()) {
            snippets.add(getSnippet(text, currentIndices));
        }

        return snippets.parallelStream()
                .collect(Collectors.toList());
    }

    private String getSnippet(String text, List<Integer> indices) {
        int startSnippet = getStartSnippet(text, indices.get(0));
        int endSnippet = getEndSnippet(text, indices.get(indices.size() - 1));
        String snippet = text.substring(startSnippet, endSnippet);

        return getBoltSnippet(snippet, indices.stream()
                .map(index -> index - startSnippet)
                .collect(Collectors.toList()));
    }

    private String getSnippet(String text, int index) {
        return getSnippet(text, List.of(index));
    }

    private int getStartSnippet(String text, int leftIndex) {
        int startSnippet = Math.max(0, leftIndex - lengthSnippet);
        startSnippet = text.indexOf(" ", startSnippet) + 1;
        startSnippet = Math.min(leftIndex, startSnippet);
        return startSnippet;
    }

    private int getEndSnippet(String text, int rightIndex) {
        int endSnippet = Math.min(rightIndex + lengthSnippet, text.length());
        endSnippet = text.lastIndexOf(" ", endSnippet);
        endSnippet = Math.max(rightIndex, endSnippet);
        return endSnippet;
    }

    private String getBoltSnippet(String snippet, List<Integer> indices) {
        StringBuilder boltSnippet = new StringBuilder();
        int lastIndex = 0;

        for (int index : indices) {
            boltSnippet.append(snippet, lastIndex, index);
            int endWord = snippet.indexOf(" ", index);
            if (endWord == -1) endWord = snippet.length();
            boltSnippet.append("<b>").append(snippet, index, endWord).append("</b>");
            lastIndex = endWord;
        }

        boltSnippet.append(snippet.substring(lastIndex));
        return boltSnippet.toString();
    }
}
