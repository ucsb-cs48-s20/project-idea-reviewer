package edu.ucsb.cs48.s20.demo.advice;

import org.springframework.web.bind.annotation.ModelAttribute;

import edu.ucsb.cs48.s20.demo.controllers.AppUsersController;
import edu.ucsb.cs48.s20.demo.entities.AppUser;
import edu.ucsb.cs48.s20.demo.entities.ProjectIdea;
import edu.ucsb.cs48.s20.demo.entities.Review;
import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.repositories.ProjectIdeaRepository;
import edu.ucsb.cs48.s20.demo.repositories.ReviewRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;
import edu.ucsb.cs48.s20.demo.services.MembershipService;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class StudentFlowAdvice {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProjectIdeaRepository projectIdeaRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Value("${app.number_reviews_required}")
    private int NUMBER_OF_REVIEWS_REQUIRED;

    @Value("${app.show_student_reviews_of_their_idea}")
    private boolean showStudentReviewsOfTheirIdea;

    @ModelAttribute("TITLE_CHAR_MIN")
    public int get_TITLE_CHAR_MIN() {
        return ProjectIdea.TITLE_CHAR_MIN;
    }

    @ModelAttribute("TITLE_CHAR_MAX")
    public int get_TITLE_CHAR_MAX() {
        return ProjectIdea.TITLE_CHAR_MAX;
    }

    @ModelAttribute("DETAILS_CHAR_MIN")
    public int get_DETAILS_CHAR_MIN() {
        return ProjectIdea.DETAILS_CHAR_MIN;
    }

    @ModelAttribute("DETAILS_CHAR_MAX")
    public int get_DETAILS_CHAR_MAX() {
        return ProjectIdea.DETAILS_CHAR_MAX;
    }

    @ModelAttribute("needsToSubmitProjectIdea")
    public boolean needsToSubmitProjectIdea(OAuth2AuthenticationToken token) {
        Student student = getStudent(token);
        if (student == null)
            return false;
        return (student.getProjectIdea() == null);
    }

    @ModelAttribute("student")
    public Student getStudent(OAuth2AuthenticationToken token) {
        if (!(membershipService.isStudent(token))) {
            return null;
        }
        String email = membershipService.email(token);
        List<Student> students = studentRepository.findByEmail(email);
        if (students.size() == 0) {
            return null;
        }
        Student student = students.get(0);
        return student;
    }

    @ModelAttribute("reviewsNeededFromStudent")
    public int getReviewsNeeded(OAuth2AuthenticationToken token) {
        Student student = getStudent(token);
        if (student == null) {
            return 0;
        }

        List<Review> reviews = reviewRepository.findByReviewer(student);

        return Integer.max(0, NUMBER_OF_REVIEWS_REQUIRED - reviews.size());
    }

    @ModelAttribute("randomIdeaThatNeedsAReview")
    public ProjectIdea getRandomIdeaThatNeedsAReview(OAuth2AuthenticationToken token) {
        Student student = getStudent(token);
        if (student == null) {
            return null;
        }

        // Case 1: deliver projects with < NUMBER_OF_REVIEWS_REQUIRED reviews currently
        Stream<ProjectIdea> projects = StreamSupport.stream(projectIdeaRepository.findAll().spliterator(), false)
        .filter((idea) -> {
            List<Review> reviews = reviewRepository.findByIdea(idea);
            if (idea.getStudent().equals(student))
                return false;
            if (reviews.stream().anyMatch((review) -> review.getReviewer().equals(student)))
                return false;
            return reviews.size() < NUMBER_OF_REVIEWS_REQUIRED;
        });

        List<ProjectIdea> remainingProjects = projects.collect(Collectors.toList());

        // Case 2: deliver any project that fits the criteria
        if (remainingProjects.isEmpty()) {
            projects = StreamSupport.stream(projectIdeaRepository.findAll().spliterator(), false)
            .filter((idea) -> {
                List<Review> reviews = reviewRepository.findByIdea(idea);
                if (idea.getStudent().equals(student))
                    return false;
                if (reviews.stream().anyMatch((review) -> review.getReviewer().equals(student)))
                    return false;
                return true;
            });

            remainingProjects = projects.collect(Collectors.toList());
        }

        if (remainingProjects.isEmpty()) {
            return null;
        }
        return remainingProjects.get(new Random().nextInt(remainingProjects.size()));

    }

    @ModelAttribute("showStudentReviewsOfTheirIdea")
    public boolean showStudentReviewsOfTheirIdea() {
        return showStudentReviewsOfTheirIdea;
    }

}