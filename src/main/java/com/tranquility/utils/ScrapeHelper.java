package com.tranquility.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranquility.models.Circular;
import com.tranquility.models.Classmate;
import com.tranquility.models.Subject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrapeHelper {

    public static List<Subject> extractSubjects(String html) {
        List<Subject> subjects = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements rows = document.select("table.gridTable2.X tbody tr");     // Select the table rows (skipping the header row)

        for (Element row : rows) {
            if (!row.select("th").isEmpty()) continue;  // Skip the header row

            Elements cells = row.select("td");
            if (cells.size() >= 4) {
                String name = cells.get(1).select("a").text();  // Subject Name
                String type = cells.get(2).text(); // Subject Type
                String syllabusLink = cells.get(1).select("a").attr("href");    // Syllabus Coverage Link

                subjects.add(new Subject(name, type, syllabusLink));
            }
        }
        return subjects;
    }

    public static List<Classmate> extractClassmates(String html) {
        List<Classmate> classmates = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements cmatesDetails = doc.select(".cmatesDet");      // Select all elements with class cmatesDet


        for (Element detail : cmatesDetails) {
            Element infoDiv = detail.select(".cmatesInfo").first(); // Extract the cmatesInfo div

            if (infoDiv != null) {
                String name = extractTextAfterLabel(infoDiv, "Name :");
                String admissionCode = extractTextAfterLabel(infoDiv, "Admission Code :");
                String email = extractTextAfterLabel(infoDiv, "E-Mail Id. :");

                classmates.add(new Classmate(name, admissionCode, email));
            }
        }

        return classmates;
    }

    private static String extractTextAfterLabel(Element element, String label) {
        Elements labels = element.select("b:containsOwn(" + label + ")");
        if (!labels.isEmpty()) {
            Element labelElement = labels.first();
            return labelElement.nextSibling().toString().trim();
        }
        return "";
    }

    public static List<Circular> extractCirculars(String json) throws JsonProcessingException {
        List<Circular> circulars = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        JsonNode dataNode = jsonNode.path("d");

        for (JsonNode node : dataNode) {
            String htmlContent = node.path("message_P").asText();    // Extract the HTML content from message_P field

            Document doc = Jsoup.parse(htmlContent);
            Element link = doc.select("a").first();
            Element img = doc.select("img").first();

            String title, url;
            if (link != null) {
                title = link.attr("title");
                url = link.attr("href");
            } else {
                title = img.attr("alt");
                url = img.attr("src");
            }

            String category = node.path("Category_P").asText();
            String date = node.path("postedOn_P").asText();

            circulars.add(new Circular(title, category, url, date));
        }

        return circulars;
    }

    public static List<Map<String, String>> extractAcademicCalendar(String data) {
        List<Map<String, String>> academicCalendar = new ArrayList<>();

        Document doc = Jsoup.parse(data);
        Elements rows = doc.select("table.gridTable2.X tbody tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (!cols.isEmpty()) { // Skip header row
                Map<String, String> map = new HashMap<>();
                map.put("title", cols.get(1).text());
                map.put("date", cols.get(2).text());
                academicCalendar.add(map);
            }
        }

        return academicCalendar;
    }

}
