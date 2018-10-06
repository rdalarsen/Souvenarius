package me.worric.souvenarius.ui.detail;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import me.worric.souvenarius.R;

public class DeletePhotoConfirmationDialog extends DialogFragment {

    public interface PhotoDeletionConfirmedListener {
        void onDeletePhotoConfirmed(String photoName);
    }

    public static final String KEY_PHOTO_NAME = "key_photo_name";
    private PhotoDeletionConfirmedListener mDeletionConfirmedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDeletionConfirmedListener = (PhotoDeletionConfirmedListener) getParentFragment();
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Host does not implement" +
                    " PhotoDeletionConfirmedListener: " + getParentFragment().toString());
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException("Missing parent fragment");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String photoName = getArguments().getString(KEY_PHOTO_NAME, "");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_photo_deletion_title_detail)
                .setMessage(R.string.dialog_photo_deletion_message_detail)
                .setPositiveButton(R.string.dialog_positive_label_detail, (dialog, which) -> {
                    mDeletionConfirmedListener.onDeletePhotoConfirmed(photoName);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_negative_label_detail, (dialog, which) ->
                        dialog.dismiss());
        return dialogBuilder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDeletionConfirmedListener = null;
    }

    public static DeletePhotoConfirmationDialog newInstance(@NonNull String photoName) {
        Bundle args = new Bundle();
        args.putString(KEY_PHOTO_NAME, photoName);
        DeletePhotoConfirmationDialog fragment = new DeletePhotoConfirmationDialog();
        fragment.setArguments(args);
        return fragment;
    }

}
