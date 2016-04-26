package de.xikolo.lanalytics.parser;

public class JsonApiWrapper {

    private final Object payload;

    public JsonApiWrapper(Object payload) {
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

}
