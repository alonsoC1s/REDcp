package mx.com.redcup.redcup.myNavigationFragments;


import android.app.Fragment;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mx.com.redcup.redcup.NewPostActivity;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyEvents;

import static android.R.attr.permission;
import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    String TAG = "MapsFragment";

    static GoogleMap googleMap_fragment;
    DatabaseReference mDataBase;
    FloatingActionButton fabNewEvent;
    LocationManager locationManager;
    double currentLat;
    double getCurrentLng;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);


        //Get UI Elements
        fabNewEvent = (FloatingActionButton) view.findViewById(R.id.fab_create_event);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        return  view;
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
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));


        //TODO: Zoom on the current location
        LatLng currentLatLng = new LatLng(currentLat,getCurrentLng);

        googleMap_fragment.addMarker(new MarkerOptions().position(currentLatLng).title("KASA"));
        googleMap_fragment.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

        //Draw markers from event objects in firebase database
        drawMarkersFromFirebaseDB();


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                fabNewEvent.setVisibility(View.GONE);
                return false;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                fabNewEvent.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        fabNewEvent.setVisibility(View.GONE);
        fabNewEvent.animate();

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        fabNewEvent.setVisibility(View.VISIBLE);
    }


    public void drawMarkersFromFirebaseDB(){
        mDataBase = FirebaseDatabase.getInstance().getReference().child("Events_parent");
        mDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot myDataSnapshot:dataSnapshot.getChildren()) {
                    MyEvents newEvent = myDataSnapshot.getValue(MyEvents.class);
                    //TODO This is useless. Make this only draw if userID is within invitee list or if the event is public
                    if (newEvent.eventIsPublic()) {
                        LatLng coords = new LatLng(newEvent.getEventLatitude(), newEvent.getEventLongitude());
                        MarkerOptions options = new MarkerOptions().position(coords).title(newEvent.getEventName());
                        googleMap_fragment.addMarker(options);
                    } //End if statement

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


}

