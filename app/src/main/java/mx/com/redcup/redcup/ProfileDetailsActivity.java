package mx.com.redcup.redcup;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import mx.com.redcup.redcup.myDataModels.AttendanceStatus;
import mx.com.redcup.redcup.myDataModels.MyEvents;
import mx.com.redcup.redcup.myDataModels.MyUsers;
import mx.com.redcup.redcup.myDataModels.RelationDetails;


public class ProfileDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProfileDetailsActivity" ;
    public DatabaseReference mDatabase_events = FirebaseDatabase.getInstance().getReference().child("Events_parent");
    public DatabaseReference mDatabase_users = FirebaseDatabase.getInstance().getReference().child("Users_parent");

    TextView postContent;
    TextView authorName;
    ProfilePictureView authorPic;
    TextView displayTime;

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
        authorName = (TextView) findViewById(R.id.tv_eventDetails_userName);
        authorPic = (ProfilePictureView) findViewById(R.id.tv_eventdetails_userpic);
        displayTime = (TextView) findViewById(R.id.tv_display_time);


        //Set onClick listeners for the FABs to log the user as whatever status they chose.
        //TODO Add a function to write new post to their profiles saying they changed their attendace status to <insert status>

        fabAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user id, and add it to the authors list of friends
                DatabaseReference user_friendsRef = mDatabase_users.child(userID).child("userFriends");
                String userUid = getCurrentFirebaseUID();

                Map<String, Object> userFiends = new HashMap<>();
                userFiends.put(userUid, RelationDetails.USER_FRIEND);

                user_friendsRef.updateChildren(userFiends);

                //Toast.makeText(getApplicationContext(),(userUid+" befriended "+ userID),Toast.LENGTH_LONG).show();
                Snackbar.make(view,"You just made a new friend!",Snackbar.LENGTH_SHORT).show();


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
                //Get the user id, and add it to the authors list of followers
                DatabaseReference user_friendsRef = mDatabase_users.child(userID).child("userFriends");
                String userUid = getCurrentFirebaseUID();

                Map<String, Object> userFiends = new HashMap<>();
                userFiends.put(userUid, RelationDetails.USER_FOLLOWER);

                user_friendsRef.updateChildren(userFiends);

                //Toast.makeText(getApplicationContext(),(userUid+" now follows "+ userID),Toast.LENGTH_LONG).show();
                Snackbar.make(view,"You are now a follower...",Snackbar.LENGTH_SHORT).show();
            }
        });



        //Fill the fields for event name, profile picture, etc
        populateActivityData(userID);



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



    public void populateActivityData(String userID){
        mDatabase_users.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);
                //TODO Set the scrim to some picture. setStatusBarScrim(Drawable)
                ///postContent.setText(event.getEventContent());
                toolbarTitle.setTitle(user.getDisplayName());
                authorName.setText(user.getDisplayName());
                authorPic.setProfileId(user.getFacebookUID());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


}
