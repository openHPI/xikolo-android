package de.xikolo.presenters.base;

public interface PresenterFactory <T extends Presenter> {

    T create();

}
