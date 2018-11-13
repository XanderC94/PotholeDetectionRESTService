package json;

public class UserFeedback {

    private final int markerId;
    private final String text;
    private final String date;
    private final boolean isUpvote;

    public UserFeedback(final int markerId, final String text, final String date, boolean isUpvote) {
        this.markerId = markerId;
        this.text = text;
        this.date = date;
        this.isUpvote = isUpvote;
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

    public boolean isUpvote() {
        return isUpvote;
    }

    @Override
    public String toString() {
        return "UserFeedback{" +
                "markerId=" + markerId +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                ", isUpvote='" + isUpvote + '\'' +
                '}';
    }
}
