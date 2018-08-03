package me.worric.souvenarius.ui.detail;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import me.worric.souvenarius.R;

public class DeletePhotoConfirmationDialog extends DialogFragment {

    public static final String KEY_PHOTO_NAME = "key_photo_name";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String photoName = getArguments().getString(KEY_PHOTO_NAME, "");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_photo_deletion_title_detail)
                .setMessage(R.string.dialog_photo_deletion_message_detail)
                .setPositiveButton(R.string.dialog_positive_label_detail, (dialog, which) -> {
                    ((DetailFragment) getParentFragment()).onDeletePhotoConfirmed(photoName);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_negative_label_detail, (dialog, which) ->
                        dialog.dismiss());
        return dialogBuilder.create();
    }

    public static DeletePhotoConfirmationDialog newInstance(String photoName) {
        Bundle args = new Bundle();
        args.putString(KEY_PHOTO_NAME, photoName);
        DeletePhotoConfirmationDialog fragment = new DeletePhotoConfirmationDialog();
        fragment.setArguments(args);
        return fragment;
    }

}
