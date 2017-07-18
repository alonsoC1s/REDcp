package mx.com.redcup.redcup.Holders_extensions;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import mx.com.redcup.redcup.EventDetailsActivity;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class EventsRecyclerHolder extends RecyclerView.ViewHolder {
    private final TextView mEventName;
    private final TextView mEventContent;
    private final ImageView mProfilePic;

    public String eventID;

    CardView mCard;

    private Context context;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");
    public StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    public EventsRecyclerHolder(View itemView) {
        super(itemView);
        mEventName = (TextView)itemView.findViewById(R.id.tv_event_content);
        mEventContent = (TextView) itemView.findViewById(R.id.tv_event_title);
        mProfilePic = (ImageView) itemView.findViewById(R.id.iv_fb_userpic);
        mCard = (CardView) itemView.findViewById(R.id.card_view);

        context = itemView.getContext();

        mCard.setClickable(true);
        mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,EventDetailsActivity.class);
                intent.putExtra("event_id",eventID);
                context.startActivity(intent);
            }
        });

    }


    public void setTitle(String title){
        mEventName.setText(title);
    }

    public void setContent(String content){
        mEventContent.setText(content);
    }

    public void setProfilePic(final String uID){
        mDatabase.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);

                Glide.with(context).using(new FirebaseImageLoader())
                        .load(mStorage.child(user.getFirebaseUID()).child("profile_picture"))
                        .signature(new StringSignature(uID)).into(mProfilePic);

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
