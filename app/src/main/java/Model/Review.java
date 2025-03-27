package Model;

public class Review {
    private String IDCus;
    private int rating;
    private String comment;

    public String getIDCus() {
        return IDCus;
    }

    public void setIDCus(String IDCus) {
        this.IDCus = IDCus;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Review(String IDCus, int rating, String comment) {
        this.IDCus = IDCus;
        this.rating = rating;
        this.comment = comment;
    }

    public Review(){}
}
