package com.jplearning.dto.response;

import com.jplearning.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseBriefResponse {
    private Long id;
    private String title;
    private Course.Level level;
    private BigDecimal price;
    private String thumbnailUrl;
    private TutorBriefResponse tutor;
}