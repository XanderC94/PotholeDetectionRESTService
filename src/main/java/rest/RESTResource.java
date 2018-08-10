package rest;

public class RESTResource<T> {

    private final long id;

    private T content;
    private String info = "";

    public RESTResource(long id, T content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public RESTResource<T> withInfo(String info) {
        this.setInfo(info);

        return this;
    }
}
