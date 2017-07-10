package mx.com.redcup.redcup;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.location.Geofence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import mx.com.redcup.redcup.myDataModels.AttendanceStatus;
import mx.com.redcup.redcup.myDataModels.MyEvents;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class EventDetailsActivity extends AppCompatActivity implements OnGeofencingTransitionListener {

    private static final String TAG = "EventDetailsActivity" ;
    public DatabaseReference mDatabase_events = FirebaseDatabase.getInstance().getReference().child("Events_parent");
    public DatabaseReference mDatabase_users = FirebaseDatabase.getInstance().getReference().child("Users_parent");

    TextView postContent;
    TextView authorName;
    ProfilePictureView authorPic;
    TextView displayTime;

    CollapsingToolbarLayout toolbarTitle;
    com.github.clans.fab.FloatingActionButton fabConfirmAttendance;
    com.github.clans.fab.FloatingActionButton fabDeclineAttendance;
    com.github.clans.fab.FloatingActionButton fabMaybeAttendance;

    public String authorUserID;
    public Double currentEventLat;
    public Double currentEventLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        //Get the extra info passed on by previous activity. i.e the event id, so firebase data can be retrieved.
        Intent intent = getIntent();
        final String postID = intent.getStringExtra("event_id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get handle of UI elements
        postContent = (TextView) findViewById(R.id.tv_EventDetails_event_contentn);
        toolbarTitle = (CollapsingToolbarLayout) findViewById(R.id.ct_eventdetails_title);
        fabConfirmAttendance = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_confirm);
        fabDeclineAttendance = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_decline);
        fabMaybeAttendance = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_maybe);
        authorName = (TextView) findViewById(R.id.tv_eventDetails_userName);
        authorPic = (ProfilePictureView) findViewById(R.id.tv_eventdetails_userpic);
        displayTime = (TextView) findViewById(R.id.tv_display_time);


        //Set onClick listeners for the FABs to log the user as whatever status they chose.
        //TODO Add a function to write new post to their profiles saying they changed their attendace status to <insert status>
        fabConfirmAttendance.setOnClickListener(new View.OnClickListener() { //User checked as attending
            @Override
            public void onClick(View view) {
                //Get the postId, and add the userID to the list of attendees
                DatabaseReference attendance_listRef = mDatabase_events.child(postID).child("attendance_list");
                String userUid = getCurrentFirebaseUID();

                Map<String, Object> attendanceUpdate = new HashMap<>();
                attendanceUpdate.put(userUid,AttendanceStatus.ATTENDANCE_CONFIRMED);

                attendance_listRef.updateChildren(attendanceUpdate);

                activateGeofence(postID);

                //Toast.makeText(getApplicationContext(),(userUid+" was added to "+postID),Toast.LENGTH_LONG).show();
                Snackbar.make(view,"You marked as attending",Snackbar.LENGTH_SHORT).show();


            }
        });

        fabDeclineAttendance.setOnClickListener(new View.OnClickListener() {//User checked as not attending
            @Override
            public void onClick(View view) {
                DatabaseReference attendance_listRef = mDatabase_events.child(postID).child("attendance_list");
                String userUid = getCurrentFirebaseUID();
                Map<String, Object> attendanceUpdate = new HashMap<>();
                attendanceUpdate.put(userUid,AttendanceStatus.ATTENDANCE_DECLINED);

                attendance_listRef.updateChildren(attendanceUpdate);

                //Toast.makeText(getApplicationContext(),(userUid+" Is not going to "+postID),Toast.LENGTH_LONG).show();
                Snackbar.make(view,"You declined invitation to this event",Snackbar.LENGTH_SHORT).show();

            }
        });

        fabMaybeAttendance.setOnClickListener(new View.OnClickListener() {//User checked as not sure if attending
            @Override
            public void onClick(View view) {
                DatabaseReference attendance_listRef = mDatabase_events.child(postID).child("attendance_list");
                String userUid = getCurrentFirebaseUID();

                Map<String, Object> attendanceUpdate = new HashMap<>();
                attendanceUpdate.put(userUid,AttendanceStatus.ATTENDANCE_UNCERTAIN);

                attendance_listRef.updateChildren(attendanceUpdate);

                //Toast.makeText(getApplicationContext(),(userUid+" Is not certain he will assist "+postID),Toast.LENGTH_LONG).show();
                Snackbar.make(view,"You marked your attendance as uncertain",Snackbar.LENGTH_SHORT).show();

            }
        });

        authorPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),ProfileDetailsActivity.class);
                intent.putExtra("user_id",authorUserID);
                startActivity(intent);
            }
        });

        //Fill the fields for event name, profile picture, etc
        populateActivityData(postID);



    }

    public void activateGeofence(String postID){
        Toast.makeText(this,"The geofence was activated", Toast.LENGTH_SHORT).show();
        //TODO: Activate geofence
        GeofenceModel eventGeofence = new GeofenceModel.Builder(postID)
                .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLatitude(currentEventLat).setLongitude(currentEventLng)
                .setRadius(150).build();

        SmartLocation.with(getApplicationContext()).geofencing().add(eventGeofence)
                .start(this);
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

    public void setProfilePic(String uID){
        mDatabase_users.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);
                authorUserID = user.getFirebaseUID();// this does not belong here
                authorPic.setProfileId(user.getFacebookUID());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setUserName(String uID){
        mDatabase_users.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);
                authorName.setText(user.getDisplayName());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void populateActivityData(String postID){
        mDatabase_events.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyEvents event = dataSnapshot.getValue(MyEvents.class);
                //TODO Set the scrim to some picture. setStatusBarScrim(Drawable)

                currentEventLat = event.getEventLatitude();
                currentEventLng = event.getEventLongitude();

                postContent.setText(event.getEventContent());
                toolbarTitle.setTitle(event.getEventName());
                authorName.setText(event.getUserID());
                setUserName(event.getUserID());
                setProfilePic(event.getUserID());
                String time = (event.getEventHour() + ":" + event.getEventMinutes() + " pm" );
                displayTime.setText(time);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {

        Toast.makeText(this, "You just entered a new event", Toast.LENGTH_SHORT).show();
    }
}
