package com.webcrawler.reporter;

import com.webcrawler.reporter_api.WebCrawlerReporter;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:crawler.properties")
class WebCrawlerReporterImplTest {

    private static final String MALFORMED_LINK = "malformed link";
    private static final String INVALID_LINK = "https://github.com/INVALID+LINK";

    @Value("${max.visited.pages:20}")
    private int maxVisitedPages;

    @Autowired
    private WebCrawlerReporter reporter;

    @Test
    void shouldReturnMapOfLinkWithElementHits() {
        Map<String, List<Integer>> linkWithHits = reporter
                .reportLinkWithElementHits(listOfTerms());

        assertNotNull(linkWithHits);
        assertTrue(linkWithHits.size() <= maxVisitedPages);
    }

    @Test
    @Disabled
    void shouldReturnMapOfLinkWithTopTenElementHits() {
        Map<String, List<Integer>> linkWithTopHits = reporter
                .reportLinkWithTopTenElementHits(listOfTerms());

        assertNotNull(linkWithTopHits);
        assertTrue(linkWithTopHits.size() <= maxVisitedPages);
    }

    @Test
    void whenLinkIsInvalid_shouldThrowIOException() {
        IOException exception =
                assertThrows(IOException.class,
                        () -> Jsoup.connect(INVALID_LINK).get());

        assertEquals("HTTP error fetching URL", exception.getMessage());

        assertTrue(exception.getMessage().contains("HTTP"));
    }

    @Test
    void whenLinkIsMalformed_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> Jsoup.connect(MALFORMED_LINK).get());

        assertEquals("Malformed URL: " + MALFORMED_LINK, exception.getMessage());

        assertTrue(exception.getMessage().contains("Malformed"));
    }

    private List<String> listOfTerms() {
        List<String> terms = new ArrayList<>();
        terms.add("Java");
        terms.add("Spring");
        terms.add("Web development");
        return terms;
    }
}