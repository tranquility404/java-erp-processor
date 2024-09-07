package com.tranquility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranquility.utils.ScrapeHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("research/abc.json");
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : Files.readAllLines(path)) {
            stringBuilder.append(s);
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(stringBuilder.toString()).path("d");
//        System.out.println(Jsoup.parse(jsonNode.asText()));

//        System.out.println(ScrapeHelper.extractAcademicCalendar(jsonNode.asText()));
        for (Map<String, String> map : ScrapeHelper.extractAcademicCalendar(jsonNode.asText())) {
            System.out.println(map.get("title"));
            System.out.println(map.get("date"));
            System.out.println();
        }
    }
}
