package mx.com.redcup.redcup.Holders_extensions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class UserProfileEventsHolder extends RecyclerView.ViewHolder {
    private final TextView mEventName;
    private final TextView mEventContent;
    private final ImageView mProfilePic;
    public String eventID;

    FrameLayout container;

    private Context context;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");


    public UserProfileEventsHolder(View itemView) {
        super(itemView);
        mEventName = (TextView)itemView.findViewById(R.id.tv_event_content);
        mEventContent = (TextView) itemView.findViewById(R.id.tv_event_title);
        mProfilePic = (ImageView) itemView.findViewById(R.id.iv_fb_userpic);
        container = (FrameLayout) itemView.findViewById(R.id.carditem_viewcontainer);

        context = itemView.getContext();


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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setPostID(String postID){
        eventID = postID;
    }

    public void makeInvisible(){
        container.setVisibility(View.GONE);
    }

}
