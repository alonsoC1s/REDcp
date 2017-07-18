package mx.com.redcup.redcup.Holders_extensions;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    Button commentDelete;
    LinearLayout commentContainer;

    public String commentID;
    public String authorID;
    public String parentEventID;

    private Context context;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users_parent");
    private DatabaseReference mDatabase_comments = FirebaseDatabase.getInstance().getReference().child("Events_parent");
    public StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    public CommentsRecyclerHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        commentContent = (TextView) itemView.findViewById(R.id.tv_comment_content);
        commentAuthorPic = (ImageView) itemView.findViewById(R.id.iv_comment_authorpic);
        commentContainer = (LinearLayout) itemView.findViewById(R.id.comment_container);
        commentDelete = (Button) itemView.findViewById(R.id.btn_comment_delete);

        commentContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                commentContainer.setForeground(new ColorDrawable(ContextCompat.getColor(context,R.color.colorAccentTransparent)));

                String currentUID = getCurrentFirebaseUID();
                if (authorID.equals(currentUID)){
                    commentDelete.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        commentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentContainer.setForeground(new ColorDrawable(ContextCompat.getColor(context,R.color.transparent)));
                commentDelete.setVisibility(View.GONE);
            }
        });

        commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {mDatabase_comments.child(parentEventID).child("event_comments").child(commentID).removeValue();
            }});
    }



    public void setContent(String content){
        commentContent.setText(content);
    }

    public void setAuthorPic(final String uID){
        authorID = uID;
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

    public void setCommentID(String pushID){
        this.commentID = pushID;
    }
    public void setParentEventID(String eventID) {this.parentEventID = eventID;}

    public String getCurrentFirebaseUID(){
        String UID = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            UID = user.getUid();
        } else {
            Log.e("Comments Recyclerview","User is unexpectedly null.");
        }
        return UID;
    }


}
