package json;

public class Comment {

    final int mid;
    final String comment;
    final String date;

    public Comment(final int mid, final  String comment, final String date) {
        this.mid = mid;
        this.comment = comment;
        this.date = date;
    }

    public int getMarkerID() {
        return mid;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "mid=" + mid +
                ", comment='" + comment + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
