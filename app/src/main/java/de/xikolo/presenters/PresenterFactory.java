package de.xikolo.presenters;

public interface PresenterFactory <T extends Presenter> {

    T create();

}
