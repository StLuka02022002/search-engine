package searchengine.services.lemma;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LemmaSearcher {

    private final LuceneMorphology russianMorphology;
    private final LuceneMorphology englishMorphology;
    private static final String REGEX = "\\P{L}+";
    private static final Set<String> RUSSIAN_PARTICLES_NAMES = Set.of("МЕЖД", "ПРЕДЛ", "СОЮЗ");
    private static final Set<String> ENGLISH_PARTICLES_NAMES = Set.of("PREP", "CONJ", "ARTICLE", "PART");

    public LemmaSearcher(LuceneMorphology russianMorphology, LuceneMorphology englishMorphology) {
        this.russianMorphology = russianMorphology;
        this.englishMorphology = englishMorphology;
    }

    public LemmaSearcher() throws IOException {
        this(new RussianLuceneMorphology(), new EnglishLuceneMorphology());
        log.info("Инициализированы LemmaSearcher с RussianLuceneMorphology и LemmaSearcher с EnglishLuceneMorphology.");
    }

    public Map<String, Integer> getLemmas(String text) {
        if (text == null || text.isEmpty()) {
            log.warn("Передан пустой или нулевой текст для лемматизации.");
            return Collections.emptyMap();
        }

        Map<String, Integer> lemmas = getWords(text).parallelStream()
                .filter(this::isNotParticle)
                .map(this::getNormalForms)
                .filter(Objects::nonNull)
                .collect(Collectors.toConcurrentMap(
                        lemma -> lemma,
                        lemma -> 1,
                        Integer::sum
                ));

        log.info("Получены леммы: {}", lemmas);
        return lemmas;
    }

    public String htmlClear(String html) {
        String text = Jsoup.parse(html).text();
        log.debug("Очистка HTML");
        return text;
    }

    public String getNormalForms(String word) {
        if (word.matches("[а-яё]+")) {
            return getNormalForms(russianMorphology, word);
        } else if (word.matches("[a-z]+")) {
            return getNormalForms(englishMorphology, word);
        }
        return null;
    }

    private String getNormalForms(LuceneMorphology luceneMorphology, String word) {
        List<String> lemmas = luceneMorphology.getNormalForms(word);
        String normalForm = lemmas.isEmpty() ? null : lemmas.get(0);

        if (normalForm == null) {
            log.warn("Не удалось найти нормальную форму для слова: '{}'", word);
        }
        return normalForm;
    }

    public boolean isNotParticle(String word) {
        return !isParticle(word);
    }

    public boolean isParticle(String word) {
        if (word.matches("[а-яё]+")) {
            return isRussianParticle(word);
        } else if (word.matches("[a-z]+")) {
            return isEnglishParticle(word);
        }
        return false;
    }

    private boolean isRussianParticle(String word) {
        return russianMorphology.getMorphInfo(word).parallelStream()
                .anyMatch(this::hasRussianParticleProperty);
    }

    private boolean isEnglishParticle(String word) {
        return englishMorphology.getMorphInfo(word).parallelStream()
                .anyMatch(this::hasEnglishParticleProperty);
    }

    private boolean hasRussianParticleProperty(String wordBase) {
        return RUSSIAN_PARTICLES_NAMES.parallelStream().anyMatch(wordBase::contains);
    }

    private boolean hasEnglishParticleProperty(String wordBase) {
        return ENGLISH_PARTICLES_NAMES.parallelStream().anyMatch(wordBase::contains);
    }

    public List<String> getWords(String text) {
        if (text == null || text.isEmpty()) {
            log.warn("Передан пустой или нулевой текст для разбиения на слова.");
            return List.of();
        }

        List<String> words = Arrays.stream(text.toLowerCase(Locale.ROOT)
                        .replaceAll("[^а-яa-zё\\s]", " ")
                        .trim()
                        .split(REGEX))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());

        log.debug("Получено {} слов из текста", words.size());
        return words;
    }
}
