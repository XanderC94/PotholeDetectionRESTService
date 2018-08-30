package json;

public class Comment {

    private final int markerId;
    private final String text;
    private final String date;

    public Comment(final int markerId, final  String text, final String date) {
        this.markerId = markerId;
        this.text = text;
        this.date = date;
    }

    public int getMarkerID() {
        return markerId;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "markerId=" + markerId +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
