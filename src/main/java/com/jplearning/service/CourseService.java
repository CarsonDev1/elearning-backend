package com.jplearning.service;

import com.jplearning.dto.request.CourseApprovalRequest;
import com.jplearning.dto.request.CourseRequest;
import com.jplearning.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CourseService {
    /**
     * Create a new course by a tutor
     *
     * @param request Course details
     * @param tutorId ID of the tutor creating the course
     * @return Created course response
     */
    CourseResponse createCourse(CourseRequest request, Long tutorId);

    /**
     * Get a course by ID
     *
     * @param courseId Course ID
     * @return Course response
     */
    CourseResponse getCourseById(Long courseId);

    /**
     * Update an existing course
     *
     * @param courseId Course ID to update
     * @param request Updated course details
     * @param tutorId ID of the tutor updating the course
     * @return Updated course response
     */
    CourseResponse updateCourse(Long courseId, CourseRequest request, Long tutorId);

    /**
     * Delete a course
     *
     * @param courseId Course ID to delete
     * @param tutorId ID of the tutor deleting the course
     */
    void deleteCourse(Long courseId, Long tutorId);

    /**
     * Submit a course for approval by admin
     *
     * @param courseId Course ID to submit
     * @param tutorId ID of the tutor submitting the course
     * @return Updated course response
     */
    CourseResponse submitCourseForApproval(Long courseId, Long tutorId);

    /**
     * Approve or reject a course by admin
     *
     * @param courseId Course ID to approve/reject
     * @param request Approval details with status and optional feedback
     * @return Updated course response
     */
    CourseResponse approveCourse(Long courseId, CourseApprovalRequest request);

    /**
     * Get all courses by a tutor
     *
     * @param tutorId Tutor ID
     * @param pageable Pagination information
     * @return Page of courses by tutor
     */
    Page<CourseResponse> getCoursesByTutor(Long tutorId, Pageable pageable);

    /**
     * Get all courses pending approval
     *
     * @param pageable Pagination information
     * @return Page of courses pending approval
     */
    Page<CourseResponse> getCoursesPendingApproval(Pageable pageable);

    /**
     * Get all approved courses
     *
     * @param pageable Pagination information
     * @return Page of approved courses
     */
    Page<CourseResponse> getApprovedCourses(Pageable pageable);

    /**
     * Search courses by title
     *
     * @param title Title to search for
     * @param pageable Pagination information
     * @return Page of matching courses
     */
    Page<CourseResponse> searchCoursesByTitle(String title, Pageable pageable);

    /**
     * Upload thumbnail for a course
     *
     * @param courseId Course ID
     * @param file Thumbnail image file
     * @param tutorId ID of the tutor uploading the thumbnail
     * @return Updated course response
     * @throws IOException If an I/O error occurs
     */
    CourseResponse uploadThumbnail(Long courseId, MultipartFile file, Long tutorId) throws IOException;
}