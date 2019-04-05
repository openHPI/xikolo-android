package de.xikolo.models.base

import moe.banana.jsonapi2.Resource

interface JsonAdapter<T : Resource> {

    fun convertToJsonResource(): T

}
