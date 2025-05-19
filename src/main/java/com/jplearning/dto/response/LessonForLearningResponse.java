package com.jplearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonForLearningResponse {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private Integer durationInMinutes;
    private Integer position;
    private boolean isCompleted;
    private LocalDateTime completedAt;
}
