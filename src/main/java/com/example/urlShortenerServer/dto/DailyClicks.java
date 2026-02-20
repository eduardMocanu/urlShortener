package com.example.urlShortenerServer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DailyClicks {
    private LocalDate date;
    private Long clicksCount;


}
