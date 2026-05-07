package com.fitally.backend.infrastructure;

import com.fitally.backend.repository.ExerciseRepository;
import com.fitally.backend.service.ExerciseInitializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseInitializationService exerciseInitializationService;

    @Override
    public void run(String... args) {
        seedExercises();
    }

    private void seedExercises() {
        if (exerciseRepository.count() > 0) {
            log.info("운동 데이터가 이미 존재합니다. ({}개) 초기화를 건너뜁니다.",
                    exerciseRepository.count());
            return;
        }

        log.info("=== 운동 데이터 초기화 시작 ===");
        try {
            exerciseInitializationService.initializeExercises();
            log.info("=== 운동 데이터 초기화 완료 ===");
        } catch (Exception e) {
            log.error("=== 운동 데이터 초기화 실패 - 앱은 계속 실행됩니다 ===", e);
        }
    }
}
