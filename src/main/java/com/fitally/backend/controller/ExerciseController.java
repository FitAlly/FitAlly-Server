package com.fitally.backend.controller;

import com.fitally.backend.common.response.ApiResponse;
import com.fitally.backend.dto.exercise.ExerciseDetailResponse;
import com.fitally.backend.dto.exercise.ExerciseListResponse;
import com.fitally.backend.dto.exercise.ExercisePreviewResponse;
import com.fitally.backend.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    /** 홈화면 미리보기 */
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<List<ExercisePreviewResponse>>> getPreview() {
        return ResponseEntity.ok(ApiResponse.success(exerciseService.getPreview()));
    }

    /** 전체보기 (키워드 검색 / 카테고리·난이도 필터) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExerciseListResponse>>> getExercises(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty) {
        return ResponseEntity.ok(ApiResponse.success(exerciseService.getExercises(keyword, category, difficulty)));
    }

    /** 단건 상세 */
    @GetMapping("/{exerciseId}")
    public ResponseEntity<ApiResponse<ExerciseDetailResponse>> getExercise(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(ApiResponse.success(exerciseService.getExercise(exerciseId)));
    }
}
