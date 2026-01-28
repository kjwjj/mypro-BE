//package com.example.mypro_be.news.service;
//
//import com.example.mypro_be.news.dto.NewsDto;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class NewsService {
//
//    @Value("${naver.client.id}")
//    private String naverclientId;
//
//    @Value("${naver.client.secret}")
//    private String naverclientSecret;
//
//    @Value("${kakao.rest.key}")
//    private String kakaoRestKey;
//
//
//    public List<NewsDto> getHousingNews() {
//        String query = "이사 OR 주거 OR 대출 OR 부동산";
//
//        String url = "https://openapi.naver.com/v1/search/news.json"
//                + "?query=" + query
//                + "&display=20"
//                + "&sort=date";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("X-Naver-Client-Id", naverclientId);
//        headers.set("X-Naver-Client-Secret", naverclientSecret);
//
//        HttpEntity<Void> request = new HttpEntity<>(headers);
//        RestTemplate restTemplate = new RestTemplate();
//
//        ResponseEntity<String> response =
//                restTemplate.exchange(url, HttpMethod.GET, request, String.class);
//
//        return parseNews(response.getBody());
//    }
//
//    private List<NewsDto> parseNews(String json) {
//        List<NewsDto> newsList = new ArrayList<>();
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//            JsonNode items = mapper.readTree(json).get("items");
//
//            for (JsonNode item : items) {
//                String title = clean(item.get("title").asText());
//                String description = clean(item.get("description").asText());
//
//                // 2차 필터링
//                if (!isHousingRelated(title + description)) continue;
//
//                newsList.add(
//                        NewsDto.builder()
//                                .id(System.currentTimeMillis()) // 간단하게 ID 생성
//                                .source("네이버")
//                                .title(title)
//                                .summary(description)
//                                .date(item.get("pubDate").asText().substring(0, 16))
//                                .link(item.get("link").asText())
//                                .color("success")
//                                .build()
//                );
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return newsList;
//    }
//
//    private boolean isHousingRelated(String text) {
//        return text.contains("이사")
//                || text.contains("주거")
//                || text.contains("대출")
//                || text.contains("부동산")
//                || text.contains("전세")
//                || text.contains("월세");
//    }
//
//    private String clean(String text) {
//        return text.replaceAll("<[^>]*>", "");
//    }
//}
//
//
package com.example.mypro_be.news.service;

import com.example.mypro_be.news.dto.NewsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.secret}")
    private String naverClientSecret;

    @Value("${kakao.rest.key}")
    private String kakaoRestKey;

    // ------------------ 전체 뉴스 가져오기 ------------------
    public List<NewsDto> getHousingNews() {
        List<NewsDto> naverNews = getNaverNews();
        List<NewsDto> kakaoNews = getKakaoNews();

        // 네이버, 카카오 각각 최신순 정렬
        naverNews.sort(Comparator.comparing(this::parseNaverDate).reversed());
        kakaoNews.sort(Comparator.comparing(this::parseKakaoDate).reversed());

        // 합쳐서 전체 최신순 정렬
        List<NewsDto> allNews = new ArrayList<>();
        allNews.addAll(naverNews);
        allNews.addAll(kakaoNews);
        allNews.sort(Comparator.comparing(this::parseCombinedDate).reversed());

        return allNews;
    }

    // ------------------ 네이버 뉴스 ------------------
    private List<NewsDto> getNaverNews() {
        String query = "이사 OR 주거 OR 대출 OR 부동산";
        String url = "https://openapi.naver.com/v1/search/news.json?query=" + query + "&display=20&sort=date";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return parseNaverNews(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<NewsDto> parseNaverNews(String json) {
        List<NewsDto> newsList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        int idx = 0;

        try {
            JsonNode items = mapper.readTree(json).get("items");
            for (JsonNode item : items) {
                String title = clean(item.get("title").asText());
                String description = clean(item.get("description").asText());

                if (!isHousingRelated(title + description)) continue;

                newsList.add(
                        NewsDto.builder()
                                .id("네이버-" + System.currentTimeMillis() + "-" + (idx++))
                                .source("네이버")
                                .title(title)
                                .summary(description)
                                .date(item.get("pubDate").asText()) // 원본 문자열 그대로 저장
                                .link(item.get("link").asText())
                                .color("success")
                                .build()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

    private Date parseNaverDate(NewsDto news) {
        try {
            // 예: "Wed, 20 Dec 2023 12:34:56 +0900"
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
                    .parse(news.getDate());
        } catch (Exception e) {
            return new Date(0);
        }
    }

    // ------------------ 카카오 뉴스 ------------------
    private List<NewsDto> getKakaoNews() {
        String query = "이사 OR 주거 OR 대출 OR 부동산";
        String url = "https://dapi.kakao.com/v2/search/web?query=" + query + "&sort=recency&size=20";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return parseKakaoNews(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<NewsDto> parseKakaoNews(String json) {
        List<NewsDto> newsList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        int idx = 0;

        try {
            JsonNode documents = mapper.readTree(json).get("documents");
            for (JsonNode doc : documents) {
                String title = clean(doc.get("title").asText());
                String description = clean(doc.get("contents").asText());

                if (!isHousingRelated(title + description)) continue;

                newsList.add(
                        NewsDto.builder()
                                .id("카카오-" + System.currentTimeMillis() + "-" + (idx++))
                                .source("카카오")
                                .title(title)
                                .summary(description)
                                .date(doc.get("datetime").asText()) // ISO 8601 문자열 그대로
                                .link(doc.get("url").asText())
                                .color("danger")
                                .build()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

    private Date parseKakaoDate(NewsDto news) {
        try {
            // ISO 8601 포맷: "2026-01-27T22:37:00.000+09:00"
            OffsetDateTime odt = OffsetDateTime.parse(news.getDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return Date.from(odt.toInstant());
        } catch (Exception e) {
            e.printStackTrace();
            return new Date(0);
        }
    }

    // 전체 합친 후 최신순 정렬용
    private Date parseCombinedDate(NewsDto news) {
        try {
            if (news.getSource().equals("네이버")) return parseNaverDate(news);
            else return parseKakaoDate(news);
        } catch (Exception e) {
            return new Date(0);
        }
    }

    // ------------------ 유틸 ------------------
    private boolean isHousingRelated(String text) {
        return text.contains("이사")
                || text.contains("주거")
                || text.contains("대출")
                || text.contains("부동산")
                || text.contains("전세")
                || text.contains("월세");
    }

    private String clean(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}


