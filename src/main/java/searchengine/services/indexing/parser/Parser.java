package searchengine.services.indexing.parser;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.exception.ParserException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
public class Parser {

    private String url;
    private int code;
    private String content;
    private Document document;

    private String userAgent;
    private String referrer;
    private boolean timeout;

    public void parse() throws ParserException {
        log.info("Начало парсинга URL: {}", url);
        try {
            if (timeout) {
                Thread.sleep((long) (Math.random() * 2000));
            }

            Connection.Response response = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .execute();

            document = response.parse();
            code = response.statusCode();
            content = document.toString();

            log.info("Успешно выполнен парсинг URL: {}, код ответа: {}", url, code);
        } catch (HttpStatusException e) {
            code = e.getStatusCode();
            log.error("Ошибка HTTP при попытке загрузить страницу '{}', код ошибки: {}", url, code);
            throw new ParserException(String.format("Страница '%s' не может быть загружена, код ошибки %d.", url, code));
        } catch (UnsupportedMimeTypeException e) {
            log.error("Ошибка типа получаемого ответа '{}'. Тип ответа '{}'", url, e.getMimeType());
            throw new ParserException(String.format("Страница '%s' не может быть загружена.", url));
        } catch (ConnectException e) {
            log.error("Ошибка подключения соединения '{}'", url);
            throw new ParserException(String.format("Страница '%s' не может быть загружена.", url));
        } catch (IOException e) {
            log.error("Ошибка ввода-вывода при попытке загрузить страницу '{}'", url, e);
            throw new ParserException(String.format("Страница '%s' не может быть загружена.", url));
        } catch (InterruptedException e) {
            log.error("Ошибка при прерывании выполнения потока для URL '{}'", url, e);
            Thread.currentThread().interrupt();
            throw new ParserException("Ошибка выполнения: " + e.getMessage());
        } catch (Exception e) {
            log.error("Непредвиденная ошибка страница '{}'. Ошибка: '{}'", url, e.getMessage(), e);
            throw new ParserException("Непредвиденная ошибка: " + e.getMessage());
        }
    }

    public List<String> getLinks() {
        log.info("Извлечение ссылок из документа для URL: {}", url);
        List<String> linksList = new ArrayList<>();
        if (document != null) {
            Elements links = document.select("a[href]");
            links.stream()
                    .map(link -> link.absUrl("href"))
                    .forEach(linksList::add);
            log.info("Найдено {} ссылок в документе для URL: {}", linksList.size(), url);
        } else {
            log.warn("Документ отсутствует для URL: {}", url);
        }
        return linksList;
    }
}
