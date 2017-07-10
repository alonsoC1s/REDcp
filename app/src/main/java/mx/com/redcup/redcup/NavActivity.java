package mx.com.redcup.redcup;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import mx.com.redcup.redcup.myNavigationFragments.FriendsFragment;
import mx.com.redcup.redcup.myNavigationFragments.MapsFragment;
import mx.com.redcup.redcup.myNavigationFragments.NearbyFragment;
import mx.com.redcup.redcup.myNavigationFragments.UserSettingsFragment;
import mx.com.redcup.redcup.myNavigationFragments.UserProfileFragment;

public class NavActivity extends AppCompatActivity {
    private String TAG = "NavActivity (Main)";

    //Create Fragment objects to be switched to
    MapsFragment mapFragment = new MapsFragment();
    NearbyFragment nearbyFragment = new NearbyFragment();
    FriendsFragment friendsFragment = new FriendsFragment();
    UserProfileFragment profileFragment = new UserProfileFragment();
    UserSettingsFragment uSettingsFragment = new UserSettingsFragment();

    Location selectedLocation;

    //Bottom nav, fragment switcher handler
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchToFragment(mapFragment);
                    return true;
                case R.id.navigation_dashboard:
                    switchToFragment(nearbyFragment);
                    return true;
                case R.id.navigation_notifications:
                    switchToFragment(friendsFragment);
                    return true;
                case R.id.navigation_user_profile:
                    switchToFragment(profileFragment);
                    return true;
                case R.id.navigation_settings:
                    switchToFragment(uSettingsFragment);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);


        //Checking if the user is logged in. Else, send him to LoginScreen
        if (AccessToken.getCurrentAccessToken() == null) {
            Log.i(TAG, "Facebook access token is null. User is not logged in. Redirecting user to LoginActivity");
            goLoginScreen();
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Get UI Elements

        //Getting Map initialized
        final MapsFragment testFragment = new MapsFragment();
        getFragmentManager().beginTransaction().replace(R.id.Nav_activity_content, testFragment).commit();

    }



    //End of native and lifecycle methods. Begin created methods
    public void terminateAllSessions(View view) {
        Log.i(TAG,"User clicked logout bottom. Terminating Facebook and Firebase sessions");
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        goLoginScreen();
    }

    private void goLoginScreen() {
        Log.i(TAG,"User is not authenticated. Redirecting user to login screen");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void switchToFragment(Fragment destination){
        getFragmentManager().beginTransaction().replace( R.id.Nav_activity_content, destination ).commit();
    }



}
