package edu.ucsb.cs48.s20.demo.formbeans;

import java.util.Objects;

public class Idea {
    private String title;
    private String details;

    public Idea() {
    }

    public Idea(String title, String details) {
        this.title = title;
        this.details = details;
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

    public Idea title(String title) {
        this.title = title;
        return this;
    }

    public Idea details(String details) {
        this.details = details;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Idea)) {
            return false;
        }
        Idea idea = (Idea) o;
        return Objects.equals(title, idea.title) && Objects.equals(details, idea.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, details);
    }

    @Override
    public String toString() {
        return "{" +
               " title='" + getTitle() + "'" +
               ", details='" + getDetails() + "'" +
               "}";
    }


}