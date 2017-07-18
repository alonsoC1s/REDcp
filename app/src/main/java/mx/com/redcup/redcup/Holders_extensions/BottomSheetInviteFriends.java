package mx.com.redcup.redcup.Holders_extensions;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mx.com.redcup.redcup.R;

/**
 * Created by downvec on 7/17/17.
 */

public class BottomSheetInviteFriends extends BottomSheetDialogFragment {

    RecyclerView usersFriendsList;
    Bundle extras;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        extras = getArguments();
        String userID = extras.getString("userID");

        View contentView = View.inflate(getActivity(), R.layout.modalfragment_inviteusers, null);

        usersFriendsList = (RecyclerView) contentView.findViewById(R.id.rv_invitefriends_friendlist);
        usersFriendsList.setHasFixedSize(false);
        usersFriendsList.setLayoutManager(new LinearLayoutManager(getActivity()));


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users_parent").child(userID).child("userFriends");

        RecyclerView.Adapter adapter = new FirebaseRecyclerAdapter<String,UserfriendsRecyclerHolder>(
                String.class, R.layout.recyclerrow_userfriends, UserfriendsRecyclerHolder.class, ref) {
            @Override
            protected void populateViewHolder(UserfriendsRecyclerHolder viewHolder, String model, int position) {
                viewHolder.setupRow(model);
            }
        };



        usersFriendsList.setAdapter(adapter);


        dialog.setContentView(contentView);
    }
}
