package searchengine.config.context;

import org.springframework.stereotype.Component;
import searchengine.services.lemma.LemmaSearcher;

import java.io.IOException;

@Component
public class LemmaContext {

    public static LemmaSearcher getLemmaSearcher(){
        try {
            return new LemmaSearcher();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
