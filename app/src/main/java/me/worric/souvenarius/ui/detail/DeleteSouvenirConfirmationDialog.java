package me.worric.souvenarius.ui.detail;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import me.worric.souvenarius.R;

public class DeleteSouvenirConfirmationDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_souvenir_deletion_title_detail)
                .setMessage(R.string.dialog_souvenir_deletion_message_detail)
                .setPositiveButton(R.string.dialog_positive_label_detail, (dialog, which) -> {
                    ((DetailFragment)getParentFragment()).onDeleteSouvenir();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_negative_label_detail, (dialog, which) ->
                        dialog.dismiss());
        return dialogBuilder.create();
    }

    public static DeleteSouvenirConfirmationDialog newInstance() {
        return new DeleteSouvenirConfirmationDialog();
    }

}
