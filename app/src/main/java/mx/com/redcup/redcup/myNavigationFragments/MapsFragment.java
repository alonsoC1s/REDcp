package mx.com.redcup.redcup.myNavigationFragments;


import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import mx.com.redcup.redcup.EventDetailsActivity;
import mx.com.redcup.redcup.Holders_extensions.BottomSheetInviteFriends;
import mx.com.redcup.redcup.NewPostActivity;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.AttendanceStatus;
import mx.com.redcup.redcup.myDataModels.MyEvents;
import mx.com.redcup.redcup.myDataModels.MyUsers;

import static android.app.Activity.RESULT_OK;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    String TAG = "MapsFragment";

    static GoogleMap googleMap_fragment;

    DatabaseReference mDataBase_events;
    DatabaseReference mDataBase_users;
    StorageReference mStor;

    Drawable red_heart;
    Drawable blue_heart;

    FloatingActionButton fabNewEvent;
    BottomSheetBehavior bottomSheetBehavior;
    TextView bottomSheetTitle;
    TextView bottomSheetContent;
    LinearLayout eventNamecontainer;
    ImageView cardAuthorPic;
    TextView cardAuthorName;
    Button gotoEventDetails;
    TextView likeCounter;

    LatLng mDefaultLocation = new LatLng(0, 0);
    Location mCurrentLocation;

    //Variables for holding the event id and others from the currently clicked marker
    String currentMarkerEventID;
    String currentMarkerEventName;
    String currentMarkerAuthorID;
    Boolean isEventLiked;

    //UI bindings for the modalsheet
    Button favEvent;
    Button shareEvent;
    Button inviteFriends;
    Button menuMore;

    private final Runnable updateFriendsFeedList = new Runnable() {
        @Override
        public void run() {
            String currentFireUID = getCurrentFirebaseUID();
            DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Feeds").child(currentFireUID).child(currentMarkerEventID);
            eventRef.removeValue();
            DatabaseReference friendsRef = mDataBase_users.child(currentFireUID).child("userFriends");
            friendsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        String friend = snapshot.getValue(String.class);
                        DatabaseReference friendReference = FirebaseDatabase.getInstance().getReference().child("Feeds").child(friend);
                        friendReference.child(currentMarkerEventID).removeValue();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
    };

    //Create custom listners
    View.OnClickListener fabClickExpanded = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatabaseReference attendance_listRef = mDataBase_events.child(currentMarkerEventID).child("attendance_list");
            String userUid = getCurrentFirebaseUID();

            Map<String, Object> attendanceUpdate = new HashMap<>();
            attendanceUpdate.put(userUid, AttendanceStatus.ATTENDANCE_CONFIRMED);

            attendance_listRef.updateChildren(attendanceUpdate);

            //TODO: Activate geofencing for the event and generate QR code

            //Toast.makeText(getApplicationContext(),(userUid+" was added to "+postID),Toast.LENGTH_LONG).show();
            Snackbar.make(v,String.format("You are now in %s attendance list", currentMarkerEventName),Snackbar.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener fabClickCollapsed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Open the google place picker
            final int PLACE_PICKER_REQUEST = 1;
            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent = builder.build(getActivity());
                startActivityForResult(intent, PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e(TAG,"Google play services repairable exception");
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG,"Google play services not available exception");
            }
        }
    };

    View.OnClickListener startEventDetails = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(),EventDetailsActivity.class);
            intent.putExtra("event_id",currentMarkerEventID);
            getActivity().startActivity(intent);
        }
    };

    //Drawbles and colors
    int handIcon;
    int red;
    int white;
    int black;

    BottomSheetBehavior.BottomSheetCallback bottomFragmentCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState){
                case BottomSheetBehavior.STATE_DRAGGING:
                    fabNewEvent.setOnClickListener(fabClickExpanded);
                    fabNewEvent.setImageResource(handIcon);

                    eventNamecontainer.setBackgroundColor(red);
                    bottomSheetTitle.setTextColor(white);

                    getEventDataOnMarkerClick(currentMarkerEventID);

                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    fabNewEvent.setOnClickListener(fabClickCollapsed);
                    fabNewEvent.setImageResource(R.drawable.button_add);

                    eventNamecontainer.setBackgroundColor(white);
                    bottomSheetTitle.setTextColor(black);
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    fabNewEvent.setOnClickListener(fabClickCollapsed);
                    fabNewEvent.setImageResource(R.drawable.button_add);

                    eventNamecontainer.setBackgroundColor(white);
                    bottomSheetTitle.setTextColor(black);
                    break;
                }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        //Init firebase database
        mDataBase_events = FirebaseDatabase.getInstance().getReference().child("Events_parent");
        mDataBase_users = FirebaseDatabase.getInstance().getReference().child("Users_parent");
        mStor = FirebaseStorage.getInstance().getReference();

        //Get UI Elements
        fabNewEvent = (FloatingActionButton) view.findViewById(R.id.fab_create_event);
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));
        bottomSheetTitle = (TextView) view.findViewById(R.id.tv_modalsheet_title);
        bottomSheetContent = (TextView) view.findViewById(R.id.tv_modalsheet_content);
        eventNamecontainer = (LinearLayout) view.findViewById(R.id.header_container);
        cardAuthorPic = (ImageView) view.findViewById(R.id.card_authorPic);
        cardAuthorName = (TextView) view.findViewById(R.id.card_authorName);
        gotoEventDetails = (Button) view.findViewById(R.id.btn_goto_eventdetails);
        likeCounter = (TextView) view.findViewById(R.id.tv_modalsheet_likecount);
        //
        favEvent = (Button) view.findViewById(R.id.btn_modalsheet_fav);
        shareEvent = (Button) view.findViewById(R.id.btn_modalsheet_share);
        inviteFriends = (Button) view.findViewById(R.id.btn_modalsheet_invite);
        menuMore = (Button) view.findViewById(R.id.btn_modalsheet_more);

        //Get colors and drawables
        handIcon = R.drawable.ic_attend;
        black = getResources().getColor(R.color.black);
        red = getResources().getColor(R.color.colorPrimary);
        white = getResources().getColor(R.color.white);
        blue_heart = getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
        red_heart = getResources().getDrawable(R.drawable.ic_favorite_red);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(bottomFragmentCallback);

        fabNewEvent.setOnClickListener(fabClickCollapsed);
        gotoEventDetails.setOnClickListener(startEventDetails);

        favEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {faveThisEvent(currentMarkerEventID,v);}
        });
        shareEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {shareThisEvent(currentMarkerEventID,v);}
        });
        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {inviteFriend(currentMarkerEventID,v);}
        });
        menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {openMoreMenu(currentMarkerEventID,v);}
        });

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MapFragment myGoogleMap = (MapFragment) getChildFragmentManager().findFragmentById(R.id.my_google_map);
        myGoogleMap.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap_fragment = googleMap;

        //Adding style options to map. Json options in raw folder
        //googleMap_fragment.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        googleMap_fragment.setMyLocationEnabled(true);
        getCurrentLocation();

        if (mCurrentLocation != null) {
            LatLng currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            googleMap_fragment.moveCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 11));
        } else {
            googleMap_fragment.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 3));
        }


        //Draw markers from event objects in firebase database
        drawMarkersFromFirebaseDB();


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                bottomSheetTitle.setText(marker.getTitle());

                currentMarkerEventName = marker.getTitle();
                currentMarkerEventID = String.valueOf(marker.getTag());

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                return false;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    void getCurrentLocation() {
        SmartLocation.with(getActivity()).location().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                Log.e("Hellod", "This is from the listeners");
                Log.e("Coordinates", String.valueOf(location.getLatitude()));
                mCurrentLocation = location;

                LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap_fragment.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 11));
            }
        });

    }

    public void drawMarkersFromFirebaseDB() {
        //TODO: ask if the object is post or event by calling snapshot.child(content_type).getValue(String). If P use P consturctor if E use E constructor
        mDataBase_events.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    MyEvents newEvent = myDataSnapshot.getValue(MyEvents.class);

                    if (newEvent.eventIsPublic()) {
                        LatLng coords = new LatLng(newEvent.getEventLatitude(), newEvent.getEventLongitude());
                        MarkerOptions options = new MarkerOptions().position(coords)
                                .title(newEvent.getEventName());

                        googleMap_fragment.addMarker(options).setTag(newEvent.getEventID());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Functions called to fill in modalsheet details
    public void getEventDataOnMarkerClick(final String eventId){
        mDataBase_events.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyEvents newEvent = dataSnapshot.getValue(MyEvents.class);

                bottomSheetContent.setText(newEvent.getEventContent());
                Long nOfLikes = dataSnapshot.child("likes").getChildrenCount();
                if (nOfLikes != 1) {
                    String likes = String.valueOf(dataSnapshot.child("likes").getChildrenCount()) + " Likes";
                    likeCounter.setText(likes);
                }else{
                    String likes = String.valueOf(dataSnapshot.child("likes").getChildrenCount()) + " Like";
                    likeCounter.setText(likes);
                }

                getAuthorDataOnMarkerClick(newEvent.getUserID());

                if (dataSnapshot.child("likes").child(getCurrentFirebaseUID()).exists()){
                    favEvent.setBackground(red_heart);
                    isEventLiked = true;
                }else {
                    favEvent.setBackground(blue_heart);
                    isEventLiked = false;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getAuthorDataOnMarkerClick(final String authorUID){
        mDataBase_users.child(authorUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyUsers newUser = dataSnapshot.getValue(MyUsers.class);
                cardAuthorName.setText(newUser.getDisplayName());
                currentMarkerAuthorID = newUser.getFirebaseUID();
                Glide.with(getActivity()).using(new FirebaseImageLoader()).load(mStor.child(authorUID).child("profile_picture"))
                        .signature(new StringSignature(authorUID)).into(cardAuthorPic);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Functions for the buttons on card
    public void faveThisEvent(String postID, View view){
        String currentUID = getCurrentFirebaseUID();
        DatabaseReference user1_friendsRef = mDataBase_events.child(postID).child("likes"); //Author ref

        if ( !isEventLiked) {//Liked for the first time
            //Marking the user liked the post
            Map<String, Object> newLike = new HashMap<>();
            newLike.put(currentUID, currentUID);

            user1_friendsRef.updateChildren(newLike);
            favEvent.setBackground(red_heart);
            isEventLiked = true;
        } else {
            user1_friendsRef.child(currentUID).removeValue();
            favEvent.setBackground(blue_heart);
            isEventLiked = false;
        }
    }

    public void shareThisEvent(String postID, View view){
        Intent intent = new AppInviteInvitation.IntentBuilder("Invite all your friends")
                .setMessage("I will be going out tonight, join me. Here is the event info")
                .setDeepLink(Uri.parse("https://cb8v7.app.goo.gl/jTpt"))
                .build();

        startActivityForResult(intent,3);
    }

    public void inviteFriend(String postID, View view){
        //TODO: Promopt bottom modal fragment and show autocomplete list of user's friends, then send a notification to invitee
        String currentUID = getCurrentFirebaseUID();
        Bundle extras = new Bundle();
        extras.putString("userID",currentUID);
        extras.putString("postID",postID);

        BottomSheetInviteFriends dialogFragment = new BottomSheetInviteFriends();
        dialogFragment.setArguments(extras);

        Toast.makeText(getActivity(),"Still in development",Toast.LENGTH_LONG).show();
    }

    public void openMoreMenu(final String postID, final View view){

        final Dialog extraEventOptions = new Dialog(getActivity());
        extraEventOptions.setContentView(R.layout.dialog_event_options);

        Button editEvent = (Button) extraEventOptions.findViewById(R.id.btn_eventdialog_edit);
        Button deleteEvent = (Button) extraEventOptions.findViewById(R.id.btn_eventdialog_delete);
        Button addAuthorAsFriend = (Button) extraEventOptions.findViewById(R.id.btn_eventdialog_addFriend);

        if (getCurrentFirebaseUID().equals(getCurrentFirebaseUID())){
            addAuthorAsFriend.setVisibility(View.GONE);
        }else {
            editEvent.setEnabled(false);
            deleteEvent.setEnabled(false);
        }

        extraEventOptions.show();

        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"not yet functional", Toast.LENGTH_SHORT).show();
                extraEventOptions.dismiss();
            }
        });

        deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataBase_events.child(postID).removeValue();
                mDataBase_users.child(getCurrentFirebaseUID()).child("user_posts").child(postID).removeValue();
                updateFriendsFeedList.run();
                extraEventOptions.dismiss();
            }
        });

        addAuthorAsFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUID = getCurrentFirebaseUID();
                DatabaseReference user1_friendsRef = mDataBase_users.child(currentMarkerAuthorID).child("userFriends"); //Author ref
                DatabaseReference user2_friendsRef = mDataBase_users.child(currentUID).child("userFriends"); // Viewer ref

                //For author
                Map<String, Object> userFiend = new HashMap<>();
                userFiend.put(currentUID,currentUID);

                //For viewer
                Map<String, Object> newFriend = new HashMap<>();
                newFriend.put(currentMarkerAuthorID,currentMarkerAuthorID);

                user1_friendsRef.updateChildren(userFiend);
                user2_friendsRef.updateChildren(newFriend);

                Snackbar.make(view, "You just made a new friend!", Snackbar.LENGTH_SHORT).show();
                extraEventOptions.dismiss();
            }
        });
    }

    //Google place picker callback. When location selected, open newPostActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){

            Place place = PlacePicker.getPlace(data, getActivity());
            LatLng placePickerLatLng = place.getLatLng();

            Intent intent = new Intent(getActivity(),NewPostActivity.class);

            Bundle infoBundle = new Bundle();
            infoBundle.putDouble("eventLatitude",placePickerLatLng.latitude);
            infoBundle.putDouble("eventLongitude",placePickerLatLng.longitude);

            intent.putExtras(infoBundle);
            startActivity(intent);
        }
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

