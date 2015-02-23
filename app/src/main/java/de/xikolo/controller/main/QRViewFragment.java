package de.xikolo.controller.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class QRViewFragment extends ContentFragment implements ZBarScannerView.ResultHandler {

    private static String TAG = QRViewFragment.class.getSimpleName();

    private static String HTTP_PREFIX = "http://";
    private static String HTTPS_PREFIX = "https://";

    final String quizViewFragmentTag = "quiz";

    private ZBarScannerView mScannerView;
    private ContentFragment mFragment;

    public static QRViewFragment newInstance() {
        QRViewFragment fragment = new QRViewFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
       mActivityCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_QUIZ, getString(R.string.scan_qr));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mScannerView = new ZBarScannerView(getActivity());

        // Setting supported Barcode Formats
        List<BarcodeFormat> listBarcodeFormats = new ArrayList<BarcodeFormat>();
        listBarcodeFormats.add(BarcodeFormat.QRCODE);
        mScannerView.setFormats(listBarcodeFormats);

        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((ViewGroup) mScannerView.getParent()).removeView(mScannerView);
    }

    @Override
    public void handleResult(Result rawResult) {
        mScannerView.stopCamera();

            String url = repairUrl(rawResult.getContents());
            startFragmentTransaction(url);
    }

    private String repairUrl(String url) {

        if(!(url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX))) {
            // Reparing URL by appending HTTP_PREFIX
            return HTTP_PREFIX + url;
        }

        return url;
    }

    private void startFragmentTransaction(String url) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        WebViewFragment webViewFragment = WebViewFragment.newInstance(url, true, null);
        webViewFragment.setLoadQuizUrl(true);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, webViewFragment, quizViewFragmentTag);
        transaction.addToBackStack(quizViewFragmentTag);
        transaction.commit();
    }
}
