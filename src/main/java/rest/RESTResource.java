package rest;

import json.Point;

import java.util.List;

public class RESTResource<T> {

    private final long id;
    private final T content;

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
}
