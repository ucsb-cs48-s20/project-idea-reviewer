package edu.ucsb.cs48.s20.demo.entities;

import java.util.Objects;

import javax.persistence.*;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student reviewer;

    @ManyToOne
    @JoinColumn(name = "idea_id")
    private ProjectIdea idea;

    private Integer rating;

    @Column(columnDefinition = "text")
    private String details;

    @PreRemove
    private void preRemove() {
        setIdea(null);
        setReviewer(null);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Student getReviewer() {
        return this.reviewer;
    }

    public void setReviewer(Student reviewer) {
        this.reviewer = reviewer;
    }

    public ProjectIdea getIdea() {
        return this.idea;
    }

    public void setIdea(ProjectIdea idea) {
        this.idea = idea;
    }

    public Integer getRating() {
        return this.rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", reviewer='" + getReviewer() + "'" + ", idea='" + getIdea() + "'"
               + ", rating='" + getRating() + "'" + ", details='" + getDetails() + "'" + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Review)) {
            return false;
        }
        Review review = (Review) o;
        return id == review.id && Objects.equals(reviewer, review.reviewer) && Objects.equals(idea, review.idea)
               && Objects.equals(rating, review.rating) && Objects.equals(details, review.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reviewer, idea, rating, details);
    }

}