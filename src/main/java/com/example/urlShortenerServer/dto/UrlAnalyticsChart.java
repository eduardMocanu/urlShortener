package com.example.urlShortenerServer.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class UrlAnalyticsChart {
    private UrlResponse url;
    private List<DailyClicks> analyticsChart;
}
