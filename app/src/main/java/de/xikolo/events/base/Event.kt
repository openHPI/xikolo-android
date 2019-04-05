package de.xikolo.events.base

abstract class Event(val message: String) {

    constructor() : this("An Event occurred.")

}
