package searchengine.config.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.services.indexing.parser.Parser;
import searchengine.services.indexing.parser.ParserAction;

@Component
@Scope("prototype")
public class ParserContext {

    @Value("${parser-settings.user-agent}")
    private String userAgent;

    @Value("${parser-settings.referrer}")
    private String referrer;

    @Value("${parser-settings.timeout}")
    private boolean timeout;

    @Value("${parser-settings.print-error}")
    private boolean printError;

    public Parser getParser(String url) {
        return Parser.builder()
                .url(url)
                .timeout(timeout)
                .userAgent(userAgent)
                .referrer(referrer)
                .build();
    }

    public ParserAction getParserAction() {
        ParserAction parserAction = new ParserAction(SpringContext.getBean(ParserContext.class));
        parserAction.setPrintError(printError);
        return parserAction;
    }
}
