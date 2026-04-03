package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
import com.fitally.backend.dto.routine.RoutineCreateRequest;
import com.fitally.backend.dto.routine.RoutineResponse;
import com.fitally.backend.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;

    /** 내 루틴 목록 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoutineResponse>>> getMyRoutines(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(routineService.getMyRoutines(userId)));
    }

    /** 루틴 이름 검색 */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RoutineResponse>>> searchRoutines(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.success(routineService.searchRoutines(userId, name)));
    }

    /** AI 추천 루틴 목록 */
    @GetMapping("/ai")
    public ResponseEntity<ApiResponse<List<RoutineResponse>>> getAiRoutines() {
        return ResponseEntity.ok(ApiResponse.success(routineService.getAiRoutines()));
    }

    /** AI 추천 루틴 랜덤 2개 (운동 탭 중간 노출용) */
    @GetMapping("/ai/random")
    public ResponseEntity<ApiResponse<List<RoutineResponse>>> getRandomAiRoutines() {
        return ResponseEntity.ok(ApiResponse.success(routineService.getRandomAiRoutines(2)));
    }

    /** AI 추천 루틴 내 루틴에 추가 */
    @PostMapping("/ai/{routineId}/save")
    public ResponseEntity<ApiResponse<RoutineResponse>> saveAiRoutine(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long routineId) {
        return ResponseEntity.ok(ApiResponse.success(routineService.saveAiRoutine(userId, routineId)));
    }

    /** 직접 만들기 루틴 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<RoutineResponse>> createRoutine(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(routineService.createRoutine(userId, request)));
    }

    /** 루틴 수정 */
    @PutMapping("/{routineId}")
    public ResponseEntity<ApiResponse<RoutineResponse>> updateRoutine(
            @PathVariable Long routineId,
            @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(routineService.updateRoutine(routineId, request)));
    }

    /** 루틴 삭제 */
    @DeleteMapping("/{routineId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoutine(@PathVariable Long routineId) {
        routineService.deleteRoutine(routineId);
        return ResponseEntity.ok(ApiResponse.success("루틴이 삭제되었습니다.", null));
    }
}
