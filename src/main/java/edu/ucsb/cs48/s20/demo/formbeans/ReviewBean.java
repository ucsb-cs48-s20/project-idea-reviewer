package edu.ucsb.cs48.s20.demo.formbeans;

import java.util.Objects;

public class ReviewBean {
    private String rating; // must be a string or spring throws an error
    private String details;


    public String getRating() {
        return this.rating;
    }

    public void setRating(String rating) {
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
        return "{" +
               " rating='" + getRating() + "'" +
               ", details='" + getDetails() + "'" +
               "}";
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ReviewBean)) {
            return false;
        }
        ReviewBean reviewBean = (ReviewBean) o;
        return Objects.equals(rating, reviewBean.rating) && Objects.equals(details, reviewBean.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating, details);
    }

}