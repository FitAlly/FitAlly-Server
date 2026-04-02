package com.fitally.backend.controller;

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
    public ResponseEntity<WorkoutStartResponse> startSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody WorkoutStartRequest request) {
        return ResponseEntity.ok(workoutSessionService.startSession(userId, request));
    }

    /** 세트 완료 */
    @PostMapping("/{sessionId}/complete-set")
    public ResponseEntity<SetCompleteResponse> completeSet(@PathVariable Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.completeSet(sessionId));
    }

    /** 운동 완료 */
    @PatchMapping("/{sessionId}/finish")
    public ResponseEntity<WorkoutFinishResponse> finishSession(
            @PathVariable Long sessionId,
            @RequestParam int totalSeconds) {
        return ResponseEntity.ok(workoutSessionService.finishSession(sessionId, totalSeconds));
    }
}
