package com.fitally.backend.dto.calendar.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklySummaryResponse {

    private String weekStart;
    private String weekEnd;
    private int completedCount;
    private int totalSeconds;
    private int totalCalories;
    private int exerciseCount;
}
