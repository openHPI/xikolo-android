package de.xikolo.controller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.util.ItemTitle;

public class ItemFragment extends Fragment {

    public static final String TAG = ItemFragment.class.getSimpleName();

    private static final String ARG_MODULE = "module";
    private static final String ARG_ITEM = "item";

    private Module mModule;
    private Item mItem;

    public ItemFragment() {
        // Required empty public constructor
    }

    public static ItemFragment newInstance(Module module, Item item) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MODULE, module);
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mModule = getArguments().getParcelable(ARG_MODULE);
            mItem = getArguments().getParcelable(ARG_ITEM);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_item, container, false);

        TextView title = (TextView) layout.findViewById(R.id.textTitle);
        title.setText(ItemTitle.format(mModule.name, mItem.title));

        return layout;
    }

}
