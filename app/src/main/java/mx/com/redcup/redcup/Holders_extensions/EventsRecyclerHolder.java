package mx.com.redcup.redcup.Holders_extensions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private final TextView eventContent;
    private final TextView eventTitle;
    private final ImageView mProfilePic;
    private final ImageView eventPicture;
    public TextView sponsoredBadge;

    public String eventID;
    public boolean sponsoredEvent;

    CardView mCard;

    private Context context;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");
    public StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    public EventsRecyclerHolder(View itemView) {
        super(itemView);
        eventContent = (TextView)itemView.findViewById(R.id.tv_event_content);
        eventTitle = (TextView) itemView.findViewById(R.id.tv_event_title);
        mProfilePic = (ImageView) itemView.findViewById(R.id.iv_fb_userpic);
        mCard = (CardView) itemView.findViewById(R.id.card_view);
        eventPicture = (ImageView) itemView.findViewById(R.id.iv_event_picture);
        sponsoredBadge = (TextView) itemView.findViewById(R.id.tv_event_sponsoredBadge);

        context = itemView.getContext();

        mCard.setClickable(true);
        mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,EventDetailsActivity.class);
                intent.putExtra("event_id",eventID);

                Pair<View, String> authorPicAnim = Pair.create((View)mProfilePic, "authorPic");
                Pair<View,String> eventTitleAnim = Pair.create((View) eventTitle,"eventTitle");
                //Pair<View,String> eventContentAnim = Pair.create((View) eventContent,"eventContent");

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, authorPicAnim, eventTitleAnim);
                context.startActivity(intent, options.toBundle());
            }
        });

    }


    public void setTitle(String title){
        eventTitle.setText(title);
    }

    public void setContent(String content){
        eventContent.setText(content);
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

    public void displayEventImage(final String pID){
        mStorage.child(pID).child(pID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                eventPicture.setVisibility(View.VISIBLE);
                Glide.with(context).using(new FirebaseImageLoader()).load(mStorage.child(pID).child(pID))
                        .signature(new StringSignature(pID)).into(eventPicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                eventPicture.setVisibility(View.GONE);
            }
        });

    }

    public void setPostID(String postID){
        eventID = postID;
    }

    public void setSponsoredStatus(Boolean isSponsored){
        sponsoredEvent = isSponsored;
        if (isSponsored) {
            sponsoredBadge.setVisibility(View.VISIBLE);
        }
    }


}
