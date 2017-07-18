package mx.com.redcup.redcup.Holders_extensions;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import mx.com.redcup.redcup.R;

/**
 * Created by downvec on 7/17/17.
 */

public class BottomSheetInviteFriends extends BottomSheetDialogFragment {

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getActivity(), R.layout.modalfragment_inviteusers, null);
        dialog.setContentView(contentView);
    }
}
