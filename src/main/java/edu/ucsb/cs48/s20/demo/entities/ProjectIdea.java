package edu.ucsb.cs48.s20.demo.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.validation.constraints.NotBlank;

@Entity
public class ProjectIdea {

    public static final int TITLE_CHAR_MIN = 4;
    public static final int TITLE_CHAR_MAX = 60;

    public static final int DETAILS_CHAR_MIN = 30;
    public static final int DETAILS_CHAR_MAX = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(mappedBy = "projectIdea", cascade = CascadeType.REFRESH)
    @JoinColumn(name = "student_id")
    private Student student;

    @NotBlank
    private String title;

    @NotBlank
    @Column(columnDefinition = "text")
    private String details;

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL)
    private Set<Review> reviews;

    @PreRemove
    private void preRemove() {
        student.setProjectIdea(null);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Student getStudent() {
        return this.student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Set<Review> getReviews() {
        return this.reviews;
    }

    public Double getAvgRating() {
        if (reviews.isEmpty())
            return 0.0;

        return reviews.stream().mapToInt((review) -> review.getRating()).average().orElse(0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ProjectIdea)) {
            return false;
        }
        ProjectIdea projectIdea = (ProjectIdea) o;
        return id == projectIdea.id && Objects.equals(student, projectIdea.student)
               && Objects.equals(title, projectIdea.title) && Objects.equals(details, projectIdea.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, student, title, details);
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", student='" + getStudent() + "'" + ", title='" + getTitle() + "'"
               + ", details='" + getDetails() + "'" + "}";
    }

}