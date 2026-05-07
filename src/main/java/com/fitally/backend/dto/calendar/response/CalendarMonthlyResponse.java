package com.fitally.backend.dto.calendar.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarMonthlyResponse {

    private List<Integer> markedDates;
}
