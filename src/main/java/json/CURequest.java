package json;

/**
 * Created by Matteo Gabellini on 15/11/2018.
 */
public class CURequest<T> {

    private String token;
    private T content;

    public CURequest(final String token,final T content) {
        this.token = token;
        this.content = content;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

}
