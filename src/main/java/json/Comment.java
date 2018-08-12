package json;

public class Comment {

    final int marker;
    final String comment;
    final String date;

    public Comment(final int marker, final  String comment, final String date) {
        this.marker = marker;
        this.comment = comment;
        this.date = date;
    }


    public int getMarkerID() {
        return marker;
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
                "marker=" + marker +
                ", comment='" + comment + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
