package com.webcrawler.reporter;

import com.webcrawler.crawler_api.WebCrawler;
import com.webcrawler.reporter_api.WebCrawlerReporter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toMap;

@Component
public class WebCrawlerReporterImpl implements WebCrawlerReporter {

    private final Logger log = LoggerFactory.getLogger(WebCrawlerReporterImpl.class);

    private final WebCrawler webCrawler;

    public WebCrawlerReporterImpl(WebCrawler webCrawler) {
        this.webCrawler = webCrawler;
    }

    @Override
    public Map<String, List<Integer>> reportLinksWithElementHits(List<String> linkElements) {
        return countLinksElementHits(linkElements);
    }

    @Override
    public Map<String, List<Integer>> reportLinksWithTopTenElementHits(List<String> linkElements) {
        return getLinksWithTopTenElementHits(linkElements);
    }

    private Map<String, List<Integer>> countLinksElementHits(List<String> linkElements) {

        Map<String, List<Integer>> linkElementHits = new HashMap<>();
        Set<String> links = webCrawler.crawlLinkWithDepth();

        for (String link : links) {

            int totalHits;
            int termHits = 0;

            List<Integer> elementHits = new ArrayList<>();

            try {
                List<String> textStrings = new ArrayList<>();

                for (String element : linkElements) {

                    Document document = Jsoup.connect(link).get();
                    Elements pageElements = document.getElementsContainingOwnText(element);

                    pageElements.forEach(pageElement -> textStrings.add(pageElement.text()));

                    termHits += textStrings.stream()
                            .filter(text ->
                                    text.matches(".*\\b(\\w*" + element + "\\w*)\\b.*"))
                            .count();

                    elementHits.add(termHits);
                    termHits = 0;
                }

            } catch (IOException | IllegalArgumentException e) {
                log.error("{}", e.getMessage());
            }

            totalHits = elementHits.stream().mapToInt(hits -> hits).sum();
            elementHits.add(totalHits);
            linkElementHits.put(link, elementHits);
        }

        return linkElementHits;
    }

    private Map<String, List<Integer>> getLinksWithTopTenElementHits(List<String> linkElements) {

        Map<String, List<Integer>> linksAndHits =
                countLinksElementHits(linkElements);

        linksAndHits = linksAndHits
                .entrySet()
                .stream()
                .sorted((hit1, hit2) ->
                        (hit2.getValue().get(hit2.getValue().size() - 1))
                        .compareTo((hit1.getValue().get(hit1.getValue().size() - 1))))
                .limit(10)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (k, v) -> v, LinkedHashMap::new));

        log.info("Top Ten");
        linksAndHits.forEach((link, elementHits) ->
                log.info("{} {}", link, elementHits));

        return linksAndHits;
    }
}
