package de.xikolo.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;

import de.xikolo.R;
import de.xikolo.util.ExternalStorageUtil;
import de.xikolo.util.StringUtil;

/**
 * @author Denis Fyedyayev, 6/11/15.
 */
public class StoragePreference extends DialogPreference {

    private CheckBox externalCheckBox;
    private CheckBox internalCheckBox;

    public StoragePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.preference_storage, null, false);

        externalCheckBox = (CheckBox) view.findViewById(R.id.preference_storage_external_checkbox);
        internalCheckBox = (CheckBox) view.findViewById(R.id.preference_storage_internal_checkbox);

        // Set default value for isUsingExternalMemory.
        setCheckBoxValues();

        String externalMemory = StringUtil.getUsableMemory(getContext(), ExternalStorageUtil.getSDCardPath());
        if (externalMemory == null) {   // There is no SD Card.
            view.findViewById(R.id.preference_storage_external_layout).setVisibility(View.GONE);
            setValue(false);
        }

        String internalMemory = StringUtil.getUsableMemory(getContext(),
                Environment.getExternalStorageDirectory().getAbsolutePath());

        ((TextView) view.findViewById(R.id.preference_storage_external_free))
                .setText(getContext().getString(R.string.settings_summary_storage, externalMemory));
        ((TextView) view.findViewById(R.id.preference_storage_internal_free))
                .setText(getContext().getString(R.string.settings_summary_storage, internalMemory));

        view.findViewById(R.id.preference_storage_external_layout)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setValue(true);
                        setCheckBoxValues();
                    }
                });

        view.findViewById(R.id.preference_storage_internal_layout)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setValue(false);
                        setCheckBoxValues();
                    }
                });

        builder.setView(view);
        builder.setNegativeButton(null, null);
        builder.setPositiveButton(null, null);
    }

    private void setCheckBoxValues() {
        externalCheckBox.setChecked(getDefaultValue());
        internalCheckBox.setChecked(!getDefaultValue());
    }

    private boolean getDefaultValue() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getKey(), true);
    }

    private void setValue(boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getKey(), value);
        editor.apply();
    }
}
