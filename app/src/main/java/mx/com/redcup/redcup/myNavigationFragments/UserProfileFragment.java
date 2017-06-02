package mx.com.redcup.redcup.myNavigationFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mx.com.redcup.redcup.MyHolders.NearbyEventsHolder;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyEvents;
import mx.com.redcup.redcup.myDataModels.MyUsers;


public class UserProfileFragment extends Fragment {
    String TAG = "UserProfileFragment";

    MyUsers userProfile;

    ProfilePictureView profilePictureView;
    TextView mUserName;


    DatabaseReference mDataBaseRef_users = FirebaseDatabase.getInstance().getReference().child("Users_parent");
    DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Getting handle of the Recycler view on the layout
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv_user_posts);
        rv.setHasFixedSize(false);

        //Using Firebase-UI library: FirebaseAdapter to create a recycler view getting data straight from firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Events_parent");
        // TODO Filter the events and show only those created by user. If statement that checks MyEvents object author attribute if == me
        RecyclerView.Adapter adapter = new FirebaseRecyclerAdapter<MyEvents, NearbyEventsHolder>(MyEvents.class, R.layout.card_item, NearbyEventsHolder.class ,mDatabase){
            @Override
            protected void populateViewHolder(NearbyEventsHolder viewHolder, MyEvents event, int position) {

                viewHolder.setTitle(event.getEventContent()); //Note: This switching is on purpose. Content and title were mixed somewhere
                viewHolder.setContent(event.getEventName());
                viewHolder.setProfilePic(event.getUserID());


            }
        };

        rv.setAdapter(adapter);


        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        //Firebase querying to set name and profile picture to user values
        populateActivityData();

        //Get UI elements
        profilePictureView = (ProfilePictureView)view.findViewById(R.id.iv_fb_profilepic);
        mUserName = (TextView)view.findViewById(R.id.tv_username);


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    //End LifeCycle and native methods


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

    public void populateActivityData(){
        mDataBaseRef_users.child(getCurrentFirebaseUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(MyUsers.class);
                profilePictureView.setProfileId(userProfile.getFacebookUID());
                mUserName.setText(userProfile.getDisplayName());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
