package com.fitally.backend.dto.calendar.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CalendarExerciseItem {

    private String exerciseName;
    private int sets;
    private int reps;
}
