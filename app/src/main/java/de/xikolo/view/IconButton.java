package de.xikolo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.xikolo.R;

public class IconButton extends RelativeLayout {
    
    private TextView label;
    private TextView icon;
    
    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconButton, 0, 0);
        String labelText = a.getString(R.styleable.IconButton_labelText);
        int labelColor = a.getColor(R.styleable.IconButton_labelColor, R.color.white);
        String iconText = a.getString(R.styleable.IconButton_iconText);
        int iconColor = a.getColor(R.styleable.IconButton_iconColor, R.color.white);
        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.icon_button, this, true);

        label = (TextView) layout.findViewById(R.id.label);
        icon = (TextView) layout.findViewById(R.id.icon);

        label.setText(labelText);
        label.setTextColor(labelColor);

        icon.setText(iconText);
        icon.setTextColor(iconColor);
    }
    
    public void setIconText(CharSequence text) {
        icon.setText(text);
    }

    public void setLabelText(CharSequence text) {
        label.setText(text);
    }
    
}
