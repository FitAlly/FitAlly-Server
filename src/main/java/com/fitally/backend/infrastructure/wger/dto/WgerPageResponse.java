package com.fitally.backend.infrastructure.wger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WgerPageResponse<T> {
    private int count;
    private String next;
    private String previous;
    private List<T> results;
}
