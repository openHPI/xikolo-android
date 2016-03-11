package de.xikolo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;

public class AutofitRecyclerView extends RecyclerView {

    private GridLayoutManager manager;

    private final ArrayList<ItemDecoration> mItemDecorations = new ArrayList<>();

    private int columnWidth = -1;

    private int spanCount;

    public AutofitRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public AutofitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutofitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }

        manager = new GridLayoutManager(getContext(), 1);
        setLayoutManager(manager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (columnWidth > 0) {
            spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }
    }

    public int getSpanCount() {
        return spanCount;
    }

    public void setSpanSizeLookup(GridLayoutManager.SpanSizeLookup spanSizeLookup) {
        manager.setSpanSizeLookup(spanSizeLookup);
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return manager.getSpanSizeLookup();
    }

    @Override
    public void addItemDecoration(ItemDecoration decor) {
        super.addItemDecoration(decor);
        addItemDecoration(decor, -1);
    }

    @Override
    public void addItemDecoration(ItemDecoration decor, int index) {
        super.addItemDecoration(decor, index);
        if (index < 0) {
            mItemDecorations.add(decor);
        } else {
            mItemDecorations.add(index, decor);
        }
    }

    public void clearItemDecorations() {
        for (ItemDecoration decor : mItemDecorations) {
            super.removeItemDecoration(decor);
        }
        mItemDecorations.clear();
    }

}
