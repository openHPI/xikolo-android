package de.xikolo.presenters.main;

import de.xikolo.presenters.base.PresenterFactory;

public class CertificateListPresenterFactory implements PresenterFactory<CertificateListPresenter> {

    @Override
    public CertificateListPresenter create() {
        return new CertificateListPresenter();
    }

}
