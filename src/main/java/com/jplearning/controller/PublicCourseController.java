package com.jplearning.controller;

import com.jplearning.dto.response.CourseDetailResponse;
import com.jplearning.dto.response.CourseResponse;
import com.jplearning.entity.Course;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.security.services.UserDetailsImpl;
import com.jplearning.service.CourseService;
import com.jplearning.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@Tag(name = "Public Course APIs", description = "APIs for accessing public course information")
@CrossOrigin(origins = "*")
public class PublicCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

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
    @Operation(summary = "Get course details", description = "Get details of a specific approved course (full content for enrolled students, preview for others)")
    public ResponseEntity<CourseDetailResponse> getCourseById(@PathVariable Long courseId) {
        // Kiểm tra khóa học có tồn tại và đã được phê duyệt
        CourseResponse course = courseService.getCourseById(courseId);

        // Đảm bảo khóa học đã được phê duyệt để hiển thị công khai
        if (course.getStatus() != Course.Status.APPROVED) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        // Kiểm tra người dùng đã đăng nhập chưa
        boolean isEnrolled = false;
        Long studentId = null;

        // Lấy thông tin người dùng hiện tại nếu đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            studentId = userDetails.getId();

            // Kiểm tra xem học viên đã đăng ký khóa học này chưa
            if (studentId != null) {
                isEnrolled = enrollmentService.isStudentEnrolledInCourse(studentId, courseId);
            }
        }

        // Tạo phản hồi dựa trên trạng thái đăng ký
        CourseDetailResponse response = new CourseDetailResponse(course, isEnrolled);

        return ResponseEntity.ok(response);
    }
}