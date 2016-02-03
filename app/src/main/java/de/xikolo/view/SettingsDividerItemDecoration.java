package de.xikolo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SettingsDividerItemDecoration extends RecyclerView.ItemDecoration {

    final int dividerHeight;
    final Paint paint = new Paint();
    final Drawable divider;

    public SettingsDividerItemDecoration(Context context) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.argb(102, 204, 204, 204));

        final int[] attrs = {android.R.attr.listDivider};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        divider = ta.getDrawable(0);

        if (divider == null) {
            dividerHeight = 2;
        } else {
            dividerHeight = divider.getIntrinsicHeight();
        }

        ta.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        LinearLayoutManager lm = (LinearLayoutManager) parent.getLayoutManager();

        final int first = lm.findFirstVisibleItemPosition();
        final int last = lm.findLastVisibleItemPosition();

        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        RecyclerView.Adapter adapter = parent.getAdapter();

        for (int i = first; i <= last; i++) {
            if (adapter.getItemCount() - 1 <= i) {
                continue;
            }

            final int viewType = adapter.getItemViewType(i);
            final int viewTypeNext = adapter.getItemViewType(i + 1);

            if (viewType == 0 || viewTypeNext == 0) {
                continue; // skipping on and before categories
            }

            final View view = lm.findViewByPosition(i);

            final int top = view.getBottom() + view.getPaddingBottom();
            final int bottom = top + dividerHeight;

            if (divider == null) {
                c.drawRect(left, top, right, bottom, paint);
            } else {
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, dividerHeight);
    }

}
