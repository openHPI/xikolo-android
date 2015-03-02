package de.xikolo.controller.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.QuizDemoActivity;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class QuizDemoFragment extends ContentFragment implements ZBarScannerView.ResultHandler {

    private static String TAG = QuizDemoFragment.class.getSimpleName();

    private static String HTTP_PREFIX = "http://";
    private static String HTTPS_PREFIX = "https://";

    private ZBarScannerView mScannerView;

    public static QuizDemoFragment newInstance() {
        QuizDemoFragment fragment = new QuizDemoFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivityCallback.onFragmentAttached(NavigationAdapter.NAV_ID_QUIZ, getString(R.string.scan_qr));
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

        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop camera on pause
        mScannerView.stopCamera();
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
        startQuizDemoActivity(url);
    }

    private String repairUrl(String url) {
        if (!(url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX))) {
            // Reparing URL by appending HTTP_PREFIX
            return HTTP_PREFIX + url;
        }

        return url;
    }

    private void startQuizDemoActivity(String url) {
        Intent intent = new Intent(getActivity(), QuizDemoActivity.class);
        Bundle b = new Bundle();
        b.putString(QuizDemoActivity.ARG_URL, url);
        intent.putExtras(b);
        startActivity(intent);
    }
}
