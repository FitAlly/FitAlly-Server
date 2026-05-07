package com.fitally.backend.dto.calendar.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarWorkoutItem {

    private String type;
    private String name;
    private Integer sets;
    private Integer reps;
    private List<CalendarExerciseItem> exercises;

    public static CalendarWorkoutItem ofSingle(String exerciseName, int sets, int reps) {
        return new CalendarWorkoutItem("SINGLE", exerciseName, sets, reps, null);
    }

    public static CalendarWorkoutItem ofRoutine(String routineName, List<CalendarExerciseItem> exercises) {
        return new CalendarWorkoutItem("ROUTINE", routineName, null, null, exercises);
    }
}
