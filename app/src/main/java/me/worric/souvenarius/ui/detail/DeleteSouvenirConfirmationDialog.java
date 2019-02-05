package me.worric.souvenarius.ui.detail;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import me.worric.souvenarius.R;

public class DeleteSouvenirConfirmationDialog extends DialogFragment {

    public interface SouvenirDeletionConfirmedListener {
        void onDeleteSouvenirConfirmed();
    }

    private SouvenirDeletionConfirmedListener mDeletionConfirmedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDeletionConfirmedListener = (SouvenirDeletionConfirmedListener) getParentFragment();
        }  catch (ClassCastException cce) {
            throw new IllegalArgumentException("Host does not implement" +
                    " SouvenirDeletionConfirmedListener: " + getParentFragment().toString(), cce);
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException("Missing parent fragment", npe);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_souvenir_deletion_title_detail)
                .setMessage(R.string.dialog_souvenir_deletion_message_detail)
                .setPositiveButton(R.string.dialog_positive_label_detail, (dialog, which) -> {
                    mDeletionConfirmedListener.onDeleteSouvenirConfirmed();
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

    public static DeleteSouvenirConfirmationDialog newInstance() {
        return new DeleteSouvenirConfirmationDialog();
    }

}
