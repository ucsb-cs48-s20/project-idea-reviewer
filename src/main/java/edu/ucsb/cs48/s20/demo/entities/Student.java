package edu.ucsb.cs48.s20.demo.entities;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @CsvBindByPosition(position = 10)
    @CsvBindByName
    @NotBlank
    private String email;

    @CsvBindByPosition(position = 5)
    @CsvBindByName
    @NotBlank
    private String fname;

    @CsvBindByPosition(position = 4)
    @CsvBindByName
    @NotBlank
    private String lname;

    @CsvBindByPosition(position = 1)
    @CsvBindByName
    @NotBlank
    private String perm;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "project_idea_id")
    private ProjectIdea projectIdea;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private Set<Review> reviews;

    public Student() {
    }

    public Student(String email, String fname, String lname, String perm) {
        this.setEmail(email);
        this.fname = fname;
        this.lname = lname;
        this.perm = perm;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email.replaceFirst("@umail.ucsb.edu", "@ucsb.edu");
    }

    public String getFname() {
        return this.fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return this.lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPerm() {
        return this.perm;
    }

    public void setPerm(String perm) {
        this.perm = perm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Student)) {
            return false;
        }
        Student student = (Student) o;
        return id == student.id && Objects.equals(email, student.email) && Objects.equals(fname, student.fname)
               && Objects.equals(lname, student.lname) && Objects.equals(perm, student.perm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, fname, lname, perm);
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", email='" + getEmail() + "'" + ", fname='" + getFname() + "'"
               + ", lname='" + getLname() + "'" + ", perm='" + getPerm() + "'" + ", projectIdea='"
               + getProjectIdea().getTitle() + "'" + "}";
    }

    public ProjectIdea getProjectIdea() {
        return this.projectIdea;
    }

    public void setProjectIdea(ProjectIdea projectIdea) {
        this.projectIdea = projectIdea;
    }

    public String title() {
        if (projectIdea == null)
            return "";
        if (projectIdea.getTitle() == null)
            return "";
        return projectIdea.getTitle();
    }

    public String details() {
        if (projectIdea == null)
            return "";
        if (projectIdea.getDetails() == null)
            return "";
        return projectIdea.getDetails();
    }

    public int numReviewsEntered() {
        return reviews.size();
    }

}