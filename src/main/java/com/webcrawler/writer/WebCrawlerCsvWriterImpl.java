package com.webcrawler.writer;

import com.webcrawler.reporter_api.WebCrawlerReporter;
import com.webcrawler.writer_api.WebCrawlerCsvWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class WebCrawlerCsvWriterImpl implements WebCrawlerCsvWriter {

    private static final Logger log = LoggerFactory
            .getLogger(WebCrawlerCsvWriterImpl.class);

    private final WebCrawlerReporter reporter;

    public WebCrawlerCsvWriterImpl(WebCrawlerReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void writeLinksWithTermHits(List<String> terms) {

        String fileName = "LinksWithTermHits.csv";

        Map<String, List<Integer>> linksWithTermsHits =
                reporter.reportLinksWithTermHits(terms);

        invokeCreateCsvFile(linksWithTermsHits, terms, fileName);
    }

    @Override
    public void writeLinksWithTopTenTermHits(List<String> terms) {

        String fileName = "LinksWithTopTenTermHits.csv";

        Map<String, List<Integer>> linksWithTopTermsHits =
                reporter.reportLinksWithTopTenTermHits(terms);

        invokeCreateCsvFile(linksWithTopTermsHits, terms, fileName);
    }

    private void invokeCreateCsvFile(Map<String, List<Integer>> linksWithHits,
                                     List<String> terms, String fileName) {
        try {
            String filePath = "statistics" + File.separator + fileName;
            createCsvFile(linksWithHits, terms, filePath);
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
    }

    private void createCsvFile(Map<String, List<Integer>> linksWithHits,
                               List<String> terms, String filePath) throws IOException {

        FileWriter out = new FileWriter(filePath);

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(getHeaders(terms)))) {

            List<String> records = new ArrayList<>();

            linksWithHits.forEach((link, termHits) -> {

                records.add(link);
                termHits.forEach(hit -> records.add(hit.toString()));

                try {
                    printer.printRecords(Collections.singletonList(records));
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }

                records.clear();
            });
        }
    }

    private String[] getHeaders(List<String> terms) {

        List<String> headers = new ArrayList<>();

        if (terms != null && !terms.isEmpty()) {
            headers.add("link");
            headers.addAll(terms);
            headers.add("total");
        }

        return headers.toArray(new String[0]);
    }
}
