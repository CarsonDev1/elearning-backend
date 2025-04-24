package com.jplearning.service.impl;

import com.jplearning.dto.request.*;
import com.jplearning.dto.response.CourseResponse;
import com.jplearning.entity.*;
import com.jplearning.entity.Module;
import com.jplearning.exception.BadRequestException;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.mapper.CourseMapper;
import com.jplearning.repository.*;
import com.jplearning.service.CloudinaryService;
import com.jplearning.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

//    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request, Long tutorId) {
        // Get tutor
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

        // Map request to entity
        Course course = courseMapper.requestToCourse(request);
        course.setTutor(tutor);
        course.setStatus(Course.Status.DRAFT);

        // Initialize modules list if it's null
        if (course.getModules() == null) {
            course.setModules(new ArrayList<>());
        }

        // Process modules if provided
        if (request.getModules() != null && !request.getModules().isEmpty()) {
            saveModules(request.getModules(), course);
        }

        // Save course
        Course savedCourse = courseRepository.save(course);

        // Calculate and update lesson count
        updateLessonCount(savedCourse);

        // Return response
        return courseMapper.courseToResponse(savedCourse);
    }

    private void buildCourseObjectGraph(List<ModuleRequest> moduleRequests, Course course) {
        int modulePosition = 1;
        for (ModuleRequest moduleRequest : moduleRequests) {
            // Create module
            Module module = courseMapper.requestToModule(moduleRequest);
            module.setPosition(modulePosition++);
            module.setCourse(course);
            course.getModules().add(module);

            // Process lessons if provided
            if (moduleRequest.getLessons() != null && !moduleRequest.getLessons().isEmpty()) {
                int lessonPosition = 1;
                for (LessonRequest lessonRequest : moduleRequest.getLessons()) {
                    Lesson lesson = courseMapper.requestToLesson(lessonRequest);
                    lesson.setPosition(lessonPosition++);
                    lesson.setModule(module);
                    module.getLessons().add(lesson);

                    // Process resources if provided
                    if (lessonRequest.getResources() != null) {
                        for (ResourceRequest resourceRequest : lessonRequest.getResources()) {
                            Resource resource = courseMapper.requestToResource(resourceRequest);
                            resource.setLesson(lesson);
                            lesson.getResources().add(resource);
                        }
                    }

                    // Process exercises if provided
                    if (lessonRequest.getExercises() != null) {
                        for (ExerciseRequest exerciseRequest : lessonRequest.getExercises()) {
                            Exercise exercise = courseMapper.requestToExercise(exerciseRequest);
                            exercise.setLesson(lesson);
                            lesson.getExercises().add(exercise);

                            // Process questions if provided
                            if (exerciseRequest.getQuestions() != null) {
                                for (QuestionRequest questionRequest : exerciseRequest.getQuestions()) {
                                    Question question = courseMapper.requestToQuestion(questionRequest);
                                    question.setExercise(exercise);
                                    exercise.getQuestions().add(question);

                                    // Process options if provided
                                    if (questionRequest.getOptions() != null) {
                                        for (OptionRequest optionRequest : questionRequest.getOptions()) {
                                            Option option = courseMapper.requestToOption(optionRequest);
                                            option.setQuestion(question);
                                            question.getOptions().add(option);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public CourseResponse getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        return courseMapper.courseToResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest request, Long tutorId) {
        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Verify tutor owns this course
        if (!course.getTutor().getId().equals(tutorId)) {
            throw new AccessDeniedException("You don't have permission to update this course");
        }

        // Check if course is editable
        if (course.getStatus() != Course.Status.DRAFT && course.getStatus() != Course.Status.REJECTED) {
            throw new BadRequestException("Cannot edit a course that is pending approval or approved");
        }

        // Update course with request data
        courseMapper.updateCourseFromRequest(request, course);

        // Clear and recreate modules if provided
        if (request.getModules() != null) {
            // Remove old modules
            for (Module module : new ArrayList<>(course.getModules())) {
                course.getModules().remove(module);
                moduleRepository.delete(module);
            }

            // Add new modules
            saveModules(request.getModules(), course);
        }

        // Update lesson count
        updateLessonCount(course);

        // Save updated course
        Course updatedCourse = courseRepository.save(course);

        return courseMapper.courseToResponse(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId, Long tutorId) {
        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Verify tutor owns this course
        if (tutorId != null) {
            // Verify tutor owns this course
            if (!course.getTutor().getId().equals(tutorId)) {
                throw new AccessDeniedException("You don't have permission to delete this course");
            }
        }

        // Check if course is deletable
        if (tutorId != null &&
                course.getStatus() != Course.Status.DRAFT &&
                course.getStatus() != Course.Status.REJECTED) {
            throw new BadRequestException("Cannot delete a course that is pending approval or approved");
        }

        // Delete course
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public CourseResponse submitCourseForApproval(Long courseId, Long tutorId) {
        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Verify tutor owns this course
        if (!course.getTutor().getId().equals(tutorId)) {
            throw new AccessDeniedException("You don't have permission to submit this course");
        }

        // Check if course is in draft or rejected state
        if (course.getStatus() != Course.Status.DRAFT && course.getStatus() != Course.Status.REJECTED) {
            throw new BadRequestException("Course is already submitted or approved");
        }

        // Validate course has required elements
        validateCourseForSubmission(course);

        // Update status
        course.setStatus(Course.Status.PENDING_APPROVAL);
        Course updatedCourse = courseRepository.save(course);

        return courseMapper.courseToResponse(updatedCourse);
    }

    @Override
    @Transactional
    public CourseResponse approveCourse(Long courseId, CourseApprovalRequest request) {
        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check if course is in pending approval state
        if (course.getStatus() != Course.Status.PENDING_APPROVAL) {
            throw new BadRequestException("Course is not in pending approval state");
        }

        // Update status based on request
        course.setStatus(request.getStatus());
        Course updatedCourse = courseRepository.save(course);

        return courseMapper.courseToResponse(updatedCourse);
    }

    @Override
    public Page<CourseResponse> getCoursesByTutor(Long tutorId, Pageable pageable) {
        // Get tutor
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

        Page<Course> courses = courseRepository.findByTutor(tutor, pageable);
        return courses.map(courseMapper::courseToResponse);
    }

    @Override
    public Page<CourseResponse> getCoursesPendingApproval(Pageable pageable) {
        Page<Course> courses = courseRepository.findByStatus(Course.Status.PENDING_APPROVAL, pageable);
        return courses.map(courseMapper::courseToResponse);
    }

    @Override
    public Page<CourseResponse> getApprovedCourses(Pageable pageable) {
        Page<Course> courses = courseRepository.findByStatus(Course.Status.APPROVED, pageable);
        return courses.map(courseMapper::courseToResponse);
    }

    @Override
    public Page<CourseResponse> searchCoursesByTitle(String title, Pageable pageable) {
        Page<Course> courses = courseRepository.findByTitleContainingIgnoreCaseAndStatus(
                title, Course.Status.APPROVED, pageable);
        return courses.map(courseMapper::courseToResponse);
    }

    @Override
    @Transactional
    public CourseResponse uploadThumbnail(Long courseId, MultipartFile file, Long tutorId) throws IOException {
        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Verify tutor owns this course
        if (!course.getTutor().getId().equals(tutorId)) {
            throw new AccessDeniedException("You don't have permission to update this course");
        }

        // Check if course is editable
        if (course.getStatus() != Course.Status.DRAFT && course.getStatus() != Course.Status.REJECTED) {
            throw new BadRequestException("Cannot edit a course that is pending approval or approved");
        }

        // Validate file is an image
        validateImageFile(file);

        // Upload image to Cloudinary
        Map<String, String> uploadResult = cloudinaryService.uploadImage(file);

        // Update course thumbnail URL
        course.setThumbnailUrl(uploadResult.get("secureUrl"));
        Course updatedCourse = courseRepository.save(course);

        return courseMapper.courseToResponse(updatedCourse);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }

        // Check file size (max 2MB for thumbnails)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BadRequestException("Image size should not exceed 2MB");
        }
    }

    // Helper methods

    private void saveModules(List<ModuleRequest> moduleRequests, Course course) {
        int position = 1;
        for (ModuleRequest moduleRequest : moduleRequests) {
            // Set position to ensure order
            moduleRequest.setPosition(position++);

            // Create module
            Module module = courseMapper.requestToModule(moduleRequest);
            module.setCourse(course);

            // Add to course's modules list (which is now guaranteed to be initialized)
            course.getModules().add(module);

            // Save the module (cascading will be handled by the course save)

            // Initialize the lessons list if needed
            if (module.getLessons() == null) {
                module.setLessons(new ArrayList<>());
            }

            // Process lessons if provided
            if (moduleRequest.getLessons() != null && !moduleRequest.getLessons().isEmpty()) {
                saveLessons(moduleRequest.getLessons(), module);
            }
        }
    }


    private void saveLessons(List<LessonRequest> lessonRequests, Module module) {
        int position = 1;
        for (LessonRequest lessonRequest : lessonRequests) {
            // Set position to ensure order
            lessonRequest.setPosition(position++);

            // Create lesson
            Lesson lesson = courseMapper.requestToLesson(lessonRequest);
            lesson.setModule(module);

            // Add to module's lessons list
            module.getLessons().add(lesson);

            // Initialize collections if needed
            if (lesson.getResources() == null) {
                lesson.setResources(new ArrayList<>());
            }
            if (lesson.getExercises() == null) {
                lesson.setExercises(new ArrayList<>());
            }

            // Process resources if provided
            if (lessonRequest.getResources() != null && !lessonRequest.getResources().isEmpty()) {
                for (ResourceRequest resourceRequest : lessonRequest.getResources()) {
                    Resource resource = courseMapper.requestToResource(resourceRequest);
                    resource.setLesson(lesson);
                    lesson.getResources().add(resource);
                }
            }

            // Process exercises if provided
            if (lessonRequest.getExercises() != null && !lessonRequest.getExercises().isEmpty()) {
                for (ExerciseRequest exerciseRequest : lessonRequest.getExercises()) {
                    Exercise exercise = courseMapper.requestToExercise(exerciseRequest);
                    exercise.setLesson(lesson);
                    lesson.getExercises().add(exercise);

                    // Initialize questions list if needed
                    if (exercise.getQuestions() == null) {
                        exercise.setQuestions(new ArrayList<>());
                    }

                    // Process questions if provided
                    if (exerciseRequest.getQuestions() != null && !exerciseRequest.getQuestions().isEmpty()) {
                        for (QuestionRequest questionRequest : exerciseRequest.getQuestions()) {
                            Question question = courseMapper.requestToQuestion(questionRequest);
                            question.setExercise(exercise);
                            exercise.getQuestions().add(question);

                            // Initialize options list if needed
                            if (question.getOptions() == null) {
                                question.setOptions(new ArrayList<>());
                            }

                            // Process options if provided
                            if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
                                for (OptionRequest optionRequest : questionRequest.getOptions()) {
                                    Option option = courseMapper.requestToOption(optionRequest);
                                    option.setQuestion(question);
                                    question.getOptions().add(option);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void saveResources(List<ResourceRequest> resourceRequests, Lesson lesson) {
        for (ResourceRequest resourceRequest : resourceRequests) {
            Resource resource = courseMapper.requestToResource(resourceRequest);
            resource.setLesson(lesson);
            resourceRepository.save(resource);
        }
    }

    private void saveExercises(List<ExerciseRequest> exerciseRequests, Lesson lesson) {
        for (ExerciseRequest exerciseRequest : exerciseRequests) {
            Exercise exercise = courseMapper.requestToExercise(exerciseRequest);
            exercise.setLesson(lesson);
            Exercise savedExercise = exerciseRepository.save(exercise);

            // Process questions if provided
            if (exerciseRequest.getQuestions() != null && !exerciseRequest.getQuestions().isEmpty()) {
                saveQuestions(exerciseRequest.getQuestions(), savedExercise);
            }
        }
    }

    private void saveQuestions(List<QuestionRequest> questionRequests, Exercise exercise) {
        for (QuestionRequest questionRequest : questionRequests) {
            Question question = courseMapper.requestToQuestion(questionRequest);
            question.setExercise(exercise);
            Question savedQuestion = questionRepository.save(question);

            // Process options if provided (for multiple choice)
            if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
                saveOptions(questionRequest.getOptions(), savedQuestion);
            }
        }
    }

    private void saveOptions(List<OptionRequest> optionRequests, Question question) {
        for (OptionRequest optionRequest : optionRequests) {
            Option option = courseMapper.requestToOption(optionRequest);
            option.setQuestion(question);
            optionRepository.save(option);
        }
    }

    private void updateLessonCount(Course course) {
        int lessonCount = 0;
        for (Module module : course.getModules()) {
            lessonCount += module.getLessons().size();
        }
        course.setLessonCount(lessonCount);
        courseRepository.save(course);
    }

    private void validateCourseForSubmission(Course course) {
        List<String> errors = new ArrayList<>();

        // Check required course fields
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            errors.add("Course title is required");
        }

        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) {
            errors.add("Course description is required");
        }

        if (course.getLevel() == null) {
            errors.add("Course level is required");
        }

        if (course.getPrice() == null) {
            errors.add("Course price is required");
        }

        // Check if course has modules
        if (course.getModules() == null || course.getModules().isEmpty()) {
            errors.add("Course must have at least one module");
        } else {
            // Check if modules have lessons
            boolean hasLessons = false;
            for (Module module : course.getModules()) {
                if (module.getLessons() != null && !module.getLessons().isEmpty()) {
                    hasLessons = true;
                    break;
                }
            }
            if (!hasLessons) {
                errors.add("Course must have at least one lesson");
            }
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException("Cannot submit course: " + String.join(", ", errors));
        }
    }


}