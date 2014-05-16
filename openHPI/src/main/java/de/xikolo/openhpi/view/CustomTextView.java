package de.xikolo.openhpi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.Hashtable;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.util.Config;

public class CustomTextView extends TextView {

    private static final String TAG = CustomTextView.class.getSimpleName();

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        String customFont = a.getString(R.styleable.CustomTextView_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf;
        synchronized (cache) {
            try {
                if (!cache.containsKey(asset)) {
                    tf = Typeface.createFromAsset(ctx.getAssets(), Config.FONT_PATH + asset);
                    cache.put(asset, tf);
                }
                tf = cache.get(asset);
                setTypeface(tf);
            } catch (Exception e) {
                Log.e(TAG, "Could not get typeface: " + e.getMessage());
                return false;
            }
            return true;
        }

    }

}
