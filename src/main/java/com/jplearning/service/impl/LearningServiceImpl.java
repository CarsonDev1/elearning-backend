package com.jplearning.service.impl;

import com.jplearning.dto.response.*;
import com.jplearning.entity.*;
import com.jplearning.entity.Module;
import com.jplearning.exception.BadRequestException;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.repository.CourseRepository;
import com.jplearning.repository.EnrollmentRepository;
import com.jplearning.repository.LessonCompletionRepository;
import com.jplearning.repository.StudentRepository;
import com.jplearning.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonCompletionRepository lessonCompletionRepository;

    @Override
    public CourseForLearningResponse getCourseForLearning(Long courseId, Long studentId) {
        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check if course is approved
        if (course.getStatus() != Course.Status.APPROVED) {
            throw new BadRequestException("Course is not available for learning");
        }

        // Get student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Check if student is enrolled in this course
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new BadRequestException("Student is not enrolled in this course"));

        // Get completed lessons for this student in this course
        Set<Long> completedLessonIds = getCompletedLessonIds(student.getId(), course.getId());

        // Build response
        return buildCourseForLearningResponse(course, enrollment, completedLessonIds);
    }

    private Set<Long> getCompletedLessonIds(Long studentId, Long courseId) {
        // Get lesson completions for this student and course
        List<LessonCompletion> completions = lessonCompletionRepository.findByStudentIdAndCourseId(studentId, courseId);

        // Extract and return only the lesson IDs
        return completions.stream()
                .map(completion -> completion.getLesson().getId())
                .collect(Collectors.toSet());
    }

    private CourseForLearningResponse buildCourseForLearningResponse(
            Course course,
            Enrollment enrollment,
            Set<Long> completedLessonIds) {

        // Build modules with lessons
        List<ModuleForLearningResponse> modules = new ArrayList<>();

        // Process each module
        for (Module module : course.getModules()) {
            List<LessonForLearningResponse> lessonResponses = new ArrayList<>();

            // Process each lesson
            for (Lesson lesson : module.getLessons()) {
                boolean isLessonCompleted = completedLessonIds.contains(lesson.getId());

                LessonForLearningResponse lessonResponse = LessonForLearningResponse.builder()
                        .id(lesson.getId())
                        .title(lesson.getTitle())
                        .description(lesson.getDescription())
                        .videoUrl(lesson.getVideoUrl())
                        .durationInMinutes(lesson.getDurationInMinutes())
                        .position(lesson.getPosition())
                        .isCompleted(isLessonCompleted)
                        .completedAt(isLessonCompleted ? getCompletionDate(lesson.getId(), enrollment.getStudent().getId()) : null)
                        .build();

                lessonResponses.add(lessonResponse);
            }

            // Sort lessons by position
            lessonResponses.sort(Comparator.comparing(LessonForLearningResponse::getPosition));

            ModuleForLearningResponse moduleResponse = ModuleForLearningResponse.builder()
                    .id(module.getId())
                    .title(module.getTitle())
                    .durationInMinutes(module.getDurationInMinutes())
                    .position(module.getPosition())
                    .lessons(lessonResponses)
                    .build();

            modules.add(moduleResponse);
        }

        // Sort modules by position
        modules.sort(Comparator.comparing(ModuleForLearningResponse::getPosition));

        // Build tutor response
        TutorBriefResponse tutorResponse = TutorBriefResponse.builder()
                .id(course.getTutor().getId())
                .fullName(course.getTutor().getFullName())
                .avatarUrl(course.getTutor().getAvatarUrl())
                .teachingRequirements(course.getTutor().getTeachingRequirements())
                .build();

        // Get level name
        String levelName = course.getLevel().getName();

        // Build main response
        return CourseForLearningResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .durationInMinutes(course.getDurationInMinutes())
                .level(levelName)
                .courseOverview(course.getCourseOverview())
                .courseContent(course.getCourseContent())
                .thumbnailUrl(course.getThumbnailUrl())
                .tutor(tutorResponse)
                .enrollmentId(enrollment.getId())
                .progressPercentage(enrollment.getProgressPercentage())
                .completedLessons(enrollment.getCompletedLessons())
                .lastAccessedLessonId(enrollment.getLastAccessedLessonId())
                .isCompleted(enrollment.isCompleted())
                .enrolledAt(enrollment.getCreatedAt())
                .completedAt(enrollment.getCompletedAt())
                .modules(modules)
                .build();
    }

    private LocalDateTime getCompletionDate(Long lessonId, Long studentId) {
        // Get completion date from LessonCompletion entity
        return lessonCompletionRepository.findByLessonIdAndStudentId(lessonId, studentId)
                .map(LessonCompletion::getCompletedAt)
                .orElse(null);
    }
}