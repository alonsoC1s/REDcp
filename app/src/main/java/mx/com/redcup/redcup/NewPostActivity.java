package mx.com.redcup.redcup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.com.redcup.redcup.myDataModels.MyEvents;
import mx.com.redcup.redcup.myDataModels.MyUsers;

@IgnoreExtraProperties
public class NewPostActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private final Runnable updateFriendsFeedList = new Runnable() {
        @Override
        public void run() {
            DatabaseReference friendsRef = mDatabase.child("Users_parent").child(getCurrentUID()).child("userFriends");
            friendsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        userFriends.add(snapshot.getValue(String.class));
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            for (String friend: userFriends){
                DatabaseReference friendReference = mDatabase.child("Feeds").child(friend);
                friendReference.updateChildren(createdPost);
            }
        }
    };

    public double chosenLat;
    public double chosenLong;

    public List<String> userFriends;
    public Map<String,Object> createdPost;

    private String TAG = "NewPostActivity";

    private DatabaseReference mDatabase;
    int selectedYear;
    int selectedMonth;
    int selectedDay;
    int selectedHour;
    int selectedMinute;

    ProfilePictureView fbProfilePic;
    TextView fbUsername;
    EditText fieldEventName;
    EditText fieldEventDescription;
    Switch makeEventPrivate;
    Button pickDateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpost);

        //Get the extra information passed on by place picker. i.e chosen LatLng
        Bundle latLngBundle = getIntent().getExtras();
        chosenLat = latLngBundle.getDouble("eventLatitude");
        chosenLong = latLngBundle.getDouble("eventLongitude");


        //Create database initial reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Create date picker dialog
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,NewPostActivity.this,mYear,mMonth,mDay);

        //Create time picker dialog
        final TimePickerDialog timePickerDialog = new TimePickerDialog(this,NewPostActivity.this,12,30,false);

        //Get handle of of UI elements
        fbProfilePic = (ProfilePictureView)findViewById(R.id.iv_newpost_fbpic);
        fbUsername = (TextView)findViewById(R.id.tv_newpost_username);
        fieldEventName = (EditText)findViewById(R.id.et_newpost_eventname);
        fieldEventDescription = (EditText)findViewById(R.id.et_newpost_eventdescription);
        makeEventPrivate = (Switch) findViewById(R.id.switch_make_private);
        pickDateBtn = (Button) findViewById(R.id.btn_newpost_datepicker);


        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
                timePickerDialog.show();
            }
        });



        setAuthorData(getCurrentUID());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Action button at toolbar clicked

        String title = fieldEventName.getText().toString();
        String content = fieldEventDescription.getText().toString();

        createPost(getCurrentUID(),title,content,chosenLat,chosenLong);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.selectedYear = year;
        this.selectedMonth = month;
        this.selectedDay = dayOfMonth;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.selectedHour = hourOfDay;
        this.selectedMinute = minute;
    }

    //Pass chosenLat and chosenLong to this
    public void createPost(String userID, String title, String content, Double lat, Double lng){ //Post creator
        //Title mandatory. Check for title
        if (TextUtils.isEmpty(title)){
            fieldEventName.setError("REQUIRED");
            return;
        }
        //Content mandatory. Check for title
        if (TextUtils.isEmpty(content)){
            fieldEventDescription.setError("REQUIRED");
        }

        //Initiate posting to Firebase database: Start
        String pushKey = mDatabase.child("Events_parent").push().getKey();

        //Check if the user wants the post as public or private
        if (makeEventPrivate.isChecked()) {
            MyEvents new_event = new MyEvents(title, content, userID, lat, lng, false, pushKey,selectedYear,selectedMonth,selectedDay,selectedHour,selectedMinute);
            mDatabase.child("Events_parent").child(pushKey).setValue(new_event);
        } else{
            MyEvents new_event = new MyEvents(title, content, userID, lat, lng, true, pushKey,selectedYear,selectedMonth,selectedDay,selectedHour,selectedMinute);
            mDatabase.child("Events_parent").child(pushKey).setValue(new_event);
        }

        //Add the post ID to a list of posts created by this user
        Map<String,Object> postUpdate = new HashMap<>();
        postUpdate.put(pushKey,pushKey);

        createdPost = postUpdate;

        mDatabase.child("Users_parent").child(userID).child("user_posts").updateChildren(postUpdate);

        //End posting to database
        Toast.makeText(getApplicationContext(),"Post created!",Toast.LENGTH_SHORT).show();

        //TODO: Push the PID to the feed list of the users friends:
        // i.e Get friend's UID from friend_list, and iterate through them, adding PID to their feed list

        returnToMap();

    }


    public String getCurrentUID(){
        String UID = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            UID = user.getUid();
        } else {
            Log.e(TAG,"User is unexpectedly null.");
        }
        return UID;
    }

    public void returnToMap(){
        Log.i(TAG,"Initiating NewPostActivity.");
        Intent intent = new Intent(this, NavActivity.class);
        startActivity(intent);
    }

    public void setAuthorData(String uID){

        mDatabase.child("Users_parent").child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers user = dataSnapshot.getValue(MyUsers.class);
                fbProfilePic.setProfileId(user.getFacebookUID());
                fbUsername.setText(user.getDisplayName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}

