package mx.com.redcup.redcup.myNavigationFragments;


import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;

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

import java.util.HashMap;
import java.util.Map;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import mx.com.redcup.redcup.NewPostActivity;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.AttendanceStatus;
import mx.com.redcup.redcup.myDataModels.MyEvents;

import static android.app.Activity.RESULT_OK;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    String TAG = "MapsFragment";

    static GoogleMap googleMap_fragment;
    DatabaseReference mDataBase_events;

    FloatingActionButton fabNewEvent;
    BottomSheetBehavior bottomSheetBehavior;
    TextView bottomSheetTitle;
    TextView bottomSheetContent;
    FrameLayout eventNamecontainer;

    LatLng mDefaultLocation = new LatLng(0, 0);
    Location mCurrentLocation;

    //Variables for holding the event id and others from the currently clicked marker
    String currentMarkerEventID;
    String currentMarkerEventName;

    //Create custom listners
    View.OnClickListener clickExpanded = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            DatabaseReference attendance_listRef = mDataBase_events.child(currentMarkerEventID).child("attendance_list");
            String userUid = getCurrentFirebaseUID();

            Map<String, Object> attendanceUpdate = new HashMap<>();
            attendanceUpdate.put(userUid, AttendanceStatus.ATTENDANCE_CONFIRMED);

            attendance_listRef.updateChildren(attendanceUpdate);

            //TODO: Activate geofencing for the event

            //Toast.makeText(getApplicationContext(),(userUid+" was added to "+postID),Toast.LENGTH_LONG).show();
            Snackbar.make(v,String.format("You are now in %s attendance list", currentMarkerEventName),Snackbar.LENGTH_SHORT).show();

        }
    };

    View.OnClickListener clickCollapsed = new View.OnClickListener() {
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

    BottomSheetBehavior.BottomSheetCallback bottomFragmentCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState){
                case BottomSheetBehavior.STATE_DRAGGING:
                    fabNewEvent.setOnClickListener(clickExpanded);
                    fabNewEvent.setImageResource(R.drawable.ic_attend);

                    eventNamecontainer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    bottomSheetTitle.setTextColor(getResources().getColor(R.color.white));

                    getEventDataOnMarkerClick(currentMarkerEventID);
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    fabNewEvent.setOnClickListener(clickCollapsed);
                    fabNewEvent.setImageResource(R.drawable.button_add);

                    eventNamecontainer.setBackgroundColor(getResources().getColor(R.color.white));
                    bottomSheetTitle.setTextColor(getResources().getColor(R.color.black));
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    fabNewEvent.setOnClickListener(clickCollapsed);
                    fabNewEvent.setImageResource(R.drawable.button_add);

                    eventNamecontainer.setBackgroundColor(getResources().getColor(R.color.white));
                    bottomSheetTitle.setTextColor(getResources().getColor(R.color.black));
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

        //Get UI Elements
        fabNewEvent = (FloatingActionButton) view.findViewById(R.id.fab_create_event);
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));
        bottomSheetTitle = (TextView) view.findViewById(R.id.tv_modalsheet_title);
        bottomSheetContent = (TextView) view.findViewById(R.id.tv_modalsheet_content);
        eventNamecontainer = (FrameLayout) view.findViewById(R.id.header_container);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(bottomFragmentCallback);
        fabNewEvent.setOnClickListener(clickCollapsed);


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
                    } //End if statement

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getEventDataOnMarkerClick(String eventId){
        mDataBase_events.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyEvents newEvent = dataSnapshot.getValue(MyEvents.class);
                bottomSheetContent.setText(newEvent.getEventContent());


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
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

