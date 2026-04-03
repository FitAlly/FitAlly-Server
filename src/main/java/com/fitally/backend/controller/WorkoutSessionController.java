package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
import com.fitally.backend.dto.workout.SetCompleteResponse;
import com.fitally.backend.dto.workout.WorkoutFinishResponse;
import com.fitally.backend.dto.workout.WorkoutStartRequest;
import com.fitally.backend.dto.workout.WorkoutStartResponse;
import com.fitally.backend.service.WorkoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workout-sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    /** 운동 시작 (목표 설정) */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutStartResponse>> startSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody WorkoutStartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("운동 세션 시작 성공", workoutSessionService.startSession(userId, request)));
    }

    /** 세트 완료 */
    @PostMapping("/{sessionId}/complete-set")
    public ResponseEntity<ApiResponse<SetCompleteResponse>> completeSet(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("세트 완료 성공", workoutSessionService.completeSet(sessionId)));
    }

    /** 운동 완료 */
    @PatchMapping("/{sessionId}/finish")
    public ResponseEntity<ApiResponse<WorkoutFinishResponse>> finishSession(
            @PathVariable Long sessionId,
            @RequestParam int totalSeconds) {
        return ResponseEntity.ok(ApiResponse.success("운동 완료 성공", workoutSessionService.finishSession(sessionId, totalSeconds)));
    }
}