package mx.com.redcup.redcup.MyHolders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mx.com.redcup.redcup.EventDetailsActivity;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class UserProfileEventsHolder extends RecyclerView.ViewHolder {
    private final TextView mEventName;
    private final TextView mEventContent;
    private final ProfilePictureView mProfilePic;
    public String eventID;

    CardView mCard;

    private Context context;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");


    public UserProfileEventsHolder(View itemView) {
        super(itemView);
        mEventName = (TextView)itemView.findViewById(R.id.tv_event_content);
        mEventContent = (TextView) itemView.findViewById(R.id.tv_event_title);
        mProfilePic = (ProfilePictureView) itemView.findViewById(R.id.iv_fb_userpic);
        mCard = (CardView) itemView.findViewById(R.id.card_view);

        context = itemView.getContext();

        mCard.setClickable(true);


    }


    public void setTitle(String title){
        mEventName.setText(title);
    }

    public void setContent(String content){
        mEventContent.setText(content);
    }

    public void setProfilePic(String uID){
        mDatabase.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);
                mProfilePic.setProfileId(user.getFacebookUID());
                mProfilePic.setPresetSize(ProfilePictureView.SMALL);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setPostID(String postID){
        eventID = postID;
    }

}
