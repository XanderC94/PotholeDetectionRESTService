package json;

public class UserFeedback {

    private final int markerId;
    private final String text;
    private final String date;

    public UserFeedback(final int markerId, final String text, final String date) {
        this.markerId = markerId;
        this.text = text;
        this.date = date;
    }

    public int getMarkerId() {
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
        return "UserFeedback{" +
                "markerId=" + markerId +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
