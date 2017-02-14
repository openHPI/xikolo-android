package de.xikolo.models;

import moe.banana.jsonapi2.Resource;

public interface JsonAdapter<T extends Resource> {

    public T convertToJsonResource();

}
