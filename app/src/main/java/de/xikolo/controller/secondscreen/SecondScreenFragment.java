package de.xikolo.controller.secondscreen;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.xikolo.R;
import de.xikolo.managers.WebSocketManager;
import de.xikolo.util.ToastUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SecondScreenFragment extends Fragment {

    public static final String TAG = SecondScreenFragment.class.getSimpleName();

    private PdfRenderer renderer;

    private View buttonPrev;

    private View buttonNext;

    private TextView textPage;

    private ViewPager viewPager;

    public SecondScreenFragment() {
        // Required empty public constructor
    }

    public static SecondScreenFragment newInstance() {
        return new SecondScreenFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonPrev = view.findViewById(R.id.buttonPrev);
        buttonNext = view.findViewById(R.id.buttonNext);

        textPage = (TextView) view.findViewById(R.id.textPage);

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new PdfSlidesPagerAdapter(getContext(), renderer));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                textPage.setText((position + 1) + "/" + renderer.getPageCount());
                if (position == 0) {
                    buttonPrev.setVisibility(View.GONE);
                } else {
                    buttonPrev.setVisibility(View.VISIBLE);
                }
                if (position == renderer.getPageCount() - 1) {
                    buttonNext.setVisibility(View.GONE);
                } else {
                    buttonNext.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        textPage.setText((viewPager.getCurrentItem() + 1) + "/" + renderer.getPageCount());
        buttonPrev.setVisibility(View.GONE);

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                }
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() < renderer.getPageCount()) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            openRenderer();
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtil.show("Error! " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    private void openRenderer() throws IOException {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Download/test.pdf";
        Log.d(TAG, "PDF test file path: " + path);
        File file = new File(path);
        renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
    }

    private void closeRenderer() throws IOException {
        renderer.close();
    }

    public void onEventMainThread(WebSocketManager.WebSocketConnectedEvent event) {
//        text.append("WebSocket connected\n");
    }

    public void onEventMainThread(WebSocketManager.WebSocketClosedEvent event) {
//        text.append("WebSocket closed\n");
    }

    public void onEventMainThread(WebSocketManager.WebSocketMessageEvent event) {
//        text.append(event.getMessage() + "\n");
    }

    static class PdfSlidesPagerAdapter extends PagerAdapter {

        Context context;

        PdfRenderer renderer;

        public PdfSlidesPagerAdapter(Context context, PdfRenderer renderer) {
            this.context = context;
            this.renderer = renderer;
        }

        @Override
        public int getCount() {
            return renderer.getPageCount();
        }

        @Override
        public Object instantiateItem(ViewGroup parent, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_pdf_image, parent, false);

            ImageView image = (ImageView) layout.findViewById(R.id.image);

            PdfRenderer.Page currentPage = renderer.openPage(position);

            Bitmap bitmap = Bitmap.createBitmap(
//                    context.getResources().getDisplayMetrics().densityDpi / 72 * currentPage.getWidth(),
//                    context.getResources().getDisplayMetrics().densityDpi / 72 * currentPage.getHeight(),
                    currentPage.getWidth(),
                    currentPage.getHeight(),
                    Bitmap.Config.ARGB_8888);

            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            currentPage.close();

            image.setImageBitmap(bitmap);
            image.invalidate();

            parent.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup parent, int position, Object view) {
            parent.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
