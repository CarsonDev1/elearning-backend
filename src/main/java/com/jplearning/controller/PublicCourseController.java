package com.jplearning.controller;

import com.jplearning.dto.response.CourseResponse;
import com.jplearning.entity.Course;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@Tag(name = "Public Course APIs", description = "APIs for accessing public course information")
@CrossOrigin(origins = "*")
public class PublicCourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all approved courses", description = "Get all published and approved courses")
    public ResponseEntity<Page<CourseResponse>> getApprovedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(courseService.getApprovedCourses(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses", description = "Search courses by title")
    public ResponseEntity<Page<CourseResponse>> searchCourses(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(courseService.searchCoursesByTitle(title, pageable));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course details", description = "Get details of a specific approved course")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long courseId) {
        CourseResponse course = courseService.getCourseById(courseId);

        // Ensure course is approved for public viewing
        if (course.getStatus() != Course.Status.APPROVED) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return ResponseEntity.ok(course);
    }
}