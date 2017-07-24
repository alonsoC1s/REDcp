package mx.com.redcup.redcup.Holders_extensions;

import android.content.Context;
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

import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class UserfriendsRecyclerHolder extends RecyclerView.ViewHolder {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");
    public StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    ImageView friendImage;
    TextView friendName;

    Context context;

    public UserfriendsRecyclerHolder(View itemView) {
        super(itemView);

        friendImage = (ImageView) itemView.findViewById(R.id.iv_invite_friendpic);
        friendName = (TextView) itemView.findViewById(R.id.tv_invite_friendname);

        context = itemView.getContext();

    }

    public void setupRow(String userID){
        final String UID = userID;
        mDatabase.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers friend = dataSnapshot.getValue(MyUsers.class);

                friendName.setText(friend.getDisplayName());
                Glide.with(context).using(new FirebaseImageLoader())
                        .load(mStorage.child(UID).child("profile_picture"))
                        .signature(new StringSignature(UID)).into(friendImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
