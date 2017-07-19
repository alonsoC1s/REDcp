package mx.com.redcup.redcup;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
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

import java.util.HashMap;
import java.util.Map;

import mx.com.redcup.redcup.Holders_extensions.UserProfileEventsHolder;
import mx.com.redcup.redcup.myDataModels.MyEvents;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class ProfileDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProfileDetailsActivity" ;
    public StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    public DatabaseReference mDatabase_users = FirebaseDatabase.getInstance().getReference().child("Users_parent");

    TextView postContent;
    ImageView authorPic;
    ImageView coverPic;
    RecyclerView userPosts;

    CollapsingToolbarLayout toolbarTitle;
    com.github.clans.fab.FloatingActionButton fabAddFriend;
    com.github.clans.fab.FloatingActionButton fabInviteUser;
    com.github.clans.fab.FloatingActionButton fabFollowUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        //Get the extra info passed on by previous activity. i.e the event id, so firebase data can be retrieved.
        Intent intent = getIntent();
        final String userID = intent.getStringExtra("user_id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profileDetails);
        setSupportActionBar(toolbar);

        //Get handle of UI elements
        postContent = (TextView) findViewById(R.id.tv_EventDetails_event_contentn);
        toolbarTitle = (CollapsingToolbarLayout) findViewById(R.id.ct_profileDetails_title);
        fabAddFriend = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_add_friend);
        fabInviteUser = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.fab_invite_user);
        fabFollowUser = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_follow_user);
        authorPic = (ImageView) findViewById(R.id.iv_profiledetails_profilepic);
        coverPic = (ImageView) findViewById(R.id.profiledetails_coverimage);
        userPosts = (RecyclerView) findViewById(R.id.rv_profiledetails_userposts);

        DatabaseReference mPostRef = FirebaseDatabase.getInstance().getReference().child("Users_parent").child(getCurrentFirebaseUID()).child("user_posts");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Events_parent");

        RecyclerView.Adapter indexedAdapter = new FirebaseIndexRecyclerAdapter<MyEvents, UserProfileEventsHolder>(
                MyEvents.class, R.layout.recyclerrow_events, UserProfileEventsHolder.class, mPostRef, mDatabase) {
            @Override
            protected void populateViewHolder(UserProfileEventsHolder viewHolder, MyEvents event, int position) {
                viewHolder.setTitle(event.getEventContent()); //Note: This switching is on purpose. Content and title were mixed somewhere
                viewHolder.setContent(event.getEventName());
                viewHolder.setProfilePic(event.getUserID());
            }
        };

        userPosts.setAdapter(indexedAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        userPosts.setLayoutManager(llm);


        //Set onClick listeners for the FABs to log the user as whatever status they chose.
        //TODO Add a function to write new post to their profiles saying they changed their attendace status to <insert status>

        fabAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(userID,view);
            }
        });

        fabInviteUser.setOnClickListener(new View.OnClickListener() {//User checked as not attending
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Sorry, this function is still in development", Toast.LENGTH_LONG).show();
            }
        });

        fabFollowUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFollower(userID, view);
            }
        });

        //Fill the fields for event name, profile picture, etc
        populateActivityData(userID);

    }

    public void addFriend(String userID, View view){
        String currentUID = getCurrentFirebaseUID();

        if (!userID.equals(currentUID)) {
            DatabaseReference user1_friendsRef = mDatabase_users.child(userID).child("userFriends"); //Profile owner
            DatabaseReference user2_friendsRef = mDatabase_users.child(currentUID).child("userFriends"); // Viewer ref

            //For profile owner
            Map<String, Object> userFiend = new HashMap<>();
            userFiend.put(currentUID, currentUID);

            //For viewer
            Map<String, Object> newFriend = new HashMap<>();
            newFriend.put(userID, userID);

            user1_friendsRef.updateChildren(userFiend);
            user2_friendsRef.updateChildren(newFriend);

            Snackbar.make(view, "You just made a new friend!", Snackbar.LENGTH_SHORT).show();

        } else {
            Snackbar.make(view, "You can't befriend yourself", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void addFollower(String userID, View view){
        String currentUID = getCurrentFirebaseUID();

        if (!userID.equals(currentUID)) {
            DatabaseReference user1_friendsRef = mDatabase_users.child(userID).child("userFollowers"); //Profile owner
            DatabaseReference user2_friendsRef = mDatabase_users.child(currentUID).child("userFollowers"); // Viewer ref

            //For profile owner
            Map<String, Object> userFiend = new HashMap<>();
            userFiend.put(currentUID, currentUID);

            //For viewer
            Map<String, Object> newFriend = new HashMap<>();
            newFriend.put(userID, userID);

            user1_friendsRef.updateChildren(userFiend);
            user2_friendsRef.updateChildren(newFriend);

            Snackbar.make(view, "You just made a new friend!", Snackbar.LENGTH_SHORT).show();

        } else {
            Snackbar.make(view, "You can't follow yourself", Snackbar.LENGTH_SHORT).show();
        }
    }



    public void populateActivityData(final String userID){
        mDatabase_users.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);
                ///postContent.setText(event.getEventContent());
                toolbarTitle.setTitle(user.getDisplayName());
                Glide.with(getApplicationContext()).using(new FirebaseImageLoader())
                        .load(mStorage.child(userID).child("profile_picture")).signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into(authorPic);

                Glide.with(getApplicationContext()).using(new FirebaseImageLoader())
                        .load(mStorage.child(userID).child("cover_image")).signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into(coverPic);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String getCurrentFirebaseUID(){
        String UID = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            UID = user.getUid();
        } else {
            Log.e(TAG,"User is unexpectedly null.");
        }
        return UID;
    }
}
