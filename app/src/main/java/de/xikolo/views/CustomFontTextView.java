package de.xikolo.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.Hashtable;

import de.xikolo.R;
import de.xikolo.config.Config;

public class CustomFontTextView extends TextView {

    private static final String TAG = CustomFontTextView.class.getSimpleName();

    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        String customFont = a.getString(R.styleable.CustomFontTextView_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf;
        synchronized (cache) {
            try {
                if (!cache.containsKey(asset)) {
                    tf = Typeface.createFromAsset(ctx.getAssets(), Config.FONT + asset);
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
