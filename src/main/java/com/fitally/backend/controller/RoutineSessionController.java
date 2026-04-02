package com.fitally.backend.controller;

import com.fitally.backend.dto.routinesession.*;
import com.fitally.backend.service.RoutineSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routine-sessions")
@RequiredArgsConstructor
public class RoutineSessionController {

    private final RoutineSessionService routineSessionService;

    /** 루틴 시작 */
    @PostMapping
    public ResponseEntity<RoutineSessionStateResponse> startSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RoutineSessionStartRequest request) {
        return ResponseEntity.ok(routineSessionService.startSession(userId, request.getRoutineId()));
    }

    /** 현재 세션 상태 조회 */
    @GetMapping("/{sessionId}")
    public ResponseEntity<RoutineSessionStateResponse> getSessionState(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(routineSessionService.getSessionState(sessionId));
    }

    /** 세트 완료 */
    @PostMapping("/{sessionId}/complete-set")
    public ResponseEntity<RoutineSetCompleteResponse> completeSet(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(routineSessionService.completeSet(sessionId));
    }

    /** 운동 항목 수정 */
    @PutMapping("/{sessionId}/exercises/{routineSessionExerciseId}")
    public ResponseEntity<RoutineSessionStateResponse> updateExercise(
            @PathVariable Long sessionId,
            @PathVariable Long routineSessionExerciseId,
            @RequestBody RoutineSessionExerciseUpdateRequest request) {
        return ResponseEntity.ok(routineSessionService.updateExercise(sessionId, routineSessionExerciseId, request));
    }

    /** 운동 항목 삭제 */
    @DeleteMapping("/{sessionId}/exercises/{routineSessionExerciseId}")
    public ResponseEntity<RoutineSessionStateResponse> deleteExercise(
            @PathVariable Long sessionId,
            @PathVariable Long routineSessionExerciseId) {
        return ResponseEntity.ok(routineSessionService.deleteExercise(sessionId, routineSessionExerciseId));
    }

    /** 루틴 운동 완료 */
    @PatchMapping("/{sessionId}/finish")
    public ResponseEntity<RoutineSessionFinishResponse> finishSession(
            @PathVariable Long sessionId,
            @RequestParam int totalSeconds) {
        return ResponseEntity.ok(routineSessionService.finishSession(sessionId, totalSeconds));
    }
}
