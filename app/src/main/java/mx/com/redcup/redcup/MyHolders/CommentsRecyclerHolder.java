package mx.com.redcup.redcup.MyHolders;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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


public class CommentsRecyclerHolder extends RecyclerView.ViewHolder {
    private final TextView commentContent;
    private final ImageView commentAuthorPic;
    public String eventID;
    LinearLayout commentContainer;


    private Context context;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");
    public StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    public CommentsRecyclerHolder(View itemView) {
        super(itemView);
        commentContent = (TextView) itemView.findViewById(R.id.tv_comment_content);
        commentAuthorPic = (ImageView) itemView.findViewById(R.id.iv_comment_authorpic);
        commentContainer = (LinearLayout) itemView.findViewById(R.id.comment_container);

        commentContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Snackbar.make(v,"Soon you will be able to delete your own comments", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        context = itemView.getContext();


    }



    public void setContent(String content){
        commentContent.setText(content);
    }

    public void setAuthorPic(final String uID){
        mDatabase.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);

                Glide.with(context).using(new FirebaseImageLoader())
                        .load(mStorage.child(user.getFirebaseUID()).child("profile_picture"))
                        .signature(new StringSignature(uID)).into(commentAuthorPic);

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
