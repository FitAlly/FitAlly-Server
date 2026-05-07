package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
import com.fitally.backend.dto.calendar.response.CalendarDailyResponse;
import com.fitally.backend.dto.calendar.response.CalendarMonthlyResponse;
import com.fitally.backend.dto.calendar.response.WeeklySummaryResponse;
import com.fitally.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    /** 월별 운동한 날짜 목록 조회 */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<CalendarMonthlyResponse>> getMonthlyMarkedDates(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success("월별 캘린더 조회 성공", calendarService.getMonthlyMarkedDates(userId, year, month)));
    }

    /** 특정 날짜 운동 기록 조회 */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<CalendarDailyResponse>> getDailyWorkouts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.success("일별 운동 기록 조회 성공", calendarService.getDailyWorkouts(userId, LocalDate.parse(date))));
    }

    /** 이번 주 요약 조회 */
    @GetMapping("/weekly-summary")
    public ResponseEntity<ApiResponse<WeeklySummaryResponse>> getWeeklySummary(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("주간 요약 조회 성공", calendarService.getWeeklySummary(userId)));
    }
}
