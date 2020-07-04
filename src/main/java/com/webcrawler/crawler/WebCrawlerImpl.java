package com.webcrawler.crawler;

import com.webcrawler.crawler_api.WebCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@PropertySource("classpath:crawler.properties")
public class WebCrawlerImpl implements WebCrawler {

    private final Logger log = LoggerFactory.getLogger(WebCrawlerImpl.class);

    @Value("${max.link.depth:1}")
    private int maxLinkDepth;

    @Value("${max.visited.pages:15}")
    private int maxVisitedPages;

    @Value("${root.link:https://github.com/vladkondratuk}")
    private String rootLink;

    private int visitedPages = 0;

    @Override
    public Set<String> crawlLink() {
        Set<String> rootWithInternalLinks = getInternalLinks(rootLink);
        resetVisitedPagesToZero();
        return rootWithInternalLinks;
    }

    @Override
    public Map<Integer, Set<String>> crawlLinkWithDepth() {

        String currentLink = rootLink;
        int currentLinkDepth = 0;

        Map<Integer, Set<String>> depthAndCrawledLinks = new HashMap<>();
        Set<String> crawledLinks = new HashSet<>();
        Set<String> repeatedLinks = new HashSet<>();

        crawledLinks.add(currentLink);
        repeatedLinks.add(currentLink);
        depthAndCrawledLinks.put(currentLinkDepth, crawledLinks);

        crawledLinks = getInternalLinks(currentLink);
        crawledLinks.removeAll(repeatedLinks);
        depthAndCrawledLinks.put(++currentLinkDepth, crawledLinks);
        repeatedLinks = crawledLinks;

        for (String crawledLink : crawledLinks) {

            currentLink = crawledLink;

            while (currentLinkDepth < maxLinkDepth) {
                crawledLinks = getInternalLinks(currentLink);
                crawledLinks.removeAll(repeatedLinks);
                depthAndCrawledLinks.put(++currentLinkDepth, crawledLinks);
                repeatedLinks = crawledLinks;
            }
        }

        log.info(depthAndCrawledLinks.toString());
        resetVisitedPagesToZero();

        return depthAndCrawledLinks;
    }

    @Override
    public List<Integer> countElementHits(List<String> linkElements) {
        return null;
    }

    private Set<String> getInternalLinks(String linkName) {

        Set<String> links = new HashSet<>();
        Stack<String> linksToVisit = new Stack<>();

        String current;

        linksToVisit.push(linkName);

        while (!linksToVisit.isEmpty()) {

            current = linksToVisit.pop();
            visitedPages++;

            if (!links.contains(current) && visitedPages < maxVisitedPages) {
                try {
                    links.add(current);

                    Document document = Jsoup.connect(linkName).get();
                    Elements internalLinks = document.select("a[href]");

                    for (Element link : internalLinks) {
                        linksToVisit.push(link.attr("abs:href"));
                    }

                } catch (IOException | IllegalArgumentException e) {
                    log.error("{}", e.getMessage());
                }
            }
        }

        return links;
    }

    private void resetVisitedPagesToZero() {
        this.visitedPages = 0;
    }
}
