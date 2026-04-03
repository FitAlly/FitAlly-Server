package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<RoutineSessionStateResponse>> startSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RoutineSessionStartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("루틴 세션 시작 성공", routineSessionService.startSession(userId, request.getRoutineId())));
    }

    /** 현재 세션 상태 조회 */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<RoutineSessionStateResponse>> getSessionState(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("루틴 세션 상태 조회 성공", routineSessionService.getSessionState(sessionId)));
    }

    /** 세트 완료 */
    @PostMapping("/{sessionId}/complete-set")
    public ResponseEntity<ApiResponse<RoutineSetCompleteResponse>> completeSet(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("세트 완료 성공", routineSessionService.completeSet(sessionId)));
    }

    /** 운동 항목 수정 */
    @PutMapping("/{sessionId}/exercises/{routineSessionExerciseId}")
    public ResponseEntity<ApiResponse<RoutineSessionStateResponse>> updateExercise(
            @PathVariable Long sessionId,
            @PathVariable Long routineSessionExerciseId,
            @RequestBody RoutineSessionExerciseUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("운동 항목 수정 성공", routineSessionService.updateExercise(sessionId, routineSessionExerciseId, request)));
    }

    /** 운동 항목 삭제 */
    @DeleteMapping("/{sessionId}/exercises/{routineSessionExerciseId}")
    public ResponseEntity<ApiResponse<RoutineSessionStateResponse>> deleteExercise(
            @PathVariable Long sessionId,
            @PathVariable Long routineSessionExerciseId) {
        return ResponseEntity.ok(ApiResponse.success("운동 항목 삭제 성공", routineSessionService.deleteExercise(sessionId, routineSessionExerciseId)));
    }

    /** 루틴 운동 완료 */
    @PatchMapping("/{sessionId}/finish")
    public ResponseEntity<ApiResponse<RoutineSessionFinishResponse>> finishSession(
            @PathVariable Long sessionId,
            @RequestParam int totalSeconds) {
        return ResponseEntity.ok(ApiResponse.success("루틴 운동 완료 성공", routineSessionService.finishSession(sessionId, totalSeconds)));
    }
}