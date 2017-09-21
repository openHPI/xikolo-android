package de.xikolo.utils;

import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import de.xikolo.App;
import in.uncod.android.bypass.Bypass;

public class MarkdownUtil {

    public static void formatAndSet(String markdown, TextView textView) {
        if (markdown != null) {
            Bypass bypass = new Bypass(App.getInstance());
            CharSequence spannable = bypass.markdownToSpannable(markdown);
            textView.setText(spannable);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textView.setText(null);
        }
    }

}
