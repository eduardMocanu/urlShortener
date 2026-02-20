package com.example.urlShortenerServer.service;

import com.example.urlShortenerServer.domain.Analytic;
import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.dto.DailyClicks;
import com.example.urlShortenerServer.dto.UrlAnalyticsChart;
import com.example.urlShortenerServer.dto.UrlResponse;
import com.example.urlShortenerServer.repository.AnalyticsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private AnalyticsRepository analyticsRepository;

    @Autowired
    public void setAnalyticsRepository(AnalyticsRepository analyticsRepository){
        this.analyticsRepository = analyticsRepository;
    }

    public void addAnalytic(Url url){
        try {
            Analytic analytic = new Analytic();
            analytic.setUrl(url);
            analytic.setAccessedAt(LocalDateTime.now());
            analyticsRepository.save(analytic);
        }
        catch (Exception ignored){
        }
    }

    public UrlAnalyticsChart getUrlAnalytics(Url url){

        List<DailyClicks> dailyClicksList = analyticsRepository.getDailyClicks(url.getId());

        UrlResponse urlResponse = new UrlResponse(
                url.getId(),
                url.getUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getExpiration(),
                url.getLastAccessed(),
                url.getClicksCount(),
                url.getActive());

        return new UrlAnalyticsChart(urlResponse, dailyClicksList);
    }


}
