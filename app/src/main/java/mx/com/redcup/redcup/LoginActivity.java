package mx.com.redcup.redcup;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import mx.com.redcup.redcup.myDataModels.MyUsers;


public class LoginActivity extends AppCompatActivity {
    public String TAG = "LoginActivity";
    AnimationDrawable animationDrawable;
    RelativeLayout relativeLayout;

    Profile fbProfile;

    //Declaring FB components
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private ProfileTracker mProfileTracker;

    //Declaring Firebase components
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);

        relativeLayout = (RelativeLayout)findViewById(R.id.relative_layout);

        //Animate gradient login screen
        animationDrawable = (AnimationDrawable)relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(0);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        //Get reference to FB login button and assign callback manager to it
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton)findViewById(R.id.btn_fb_login);
        loginButton.setReadPermissions("public_profile","user_birthday");

        //Adding a callback manager to the Facebook login button
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG,"User logged in to facebook successfully");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.i(TAG,"User Cancelled login to Facebook operation.");
                Toast.makeText(getApplicationContext(), R.string.cancel_login, Toast.LENGTH_SHORT)
                        .show();
            }
            @Override
            public void onError(FacebookException error) {
                Log.e(TAG,"Error logging user in to Facebook. Error in Callback manager.");
                Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    fbProfile = currentProfile;
                }

            }
        };


        //End Facebook login functions

        //Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null ){
                    Log.i(TAG,"Login to Firebase was successful");

                    write_new_user(user.getUid());

                    Toast.makeText(getApplicationContext(),R.string.login_was_successfull,Toast.LENGTH_LONG)
                            .show();
                    goMainScreen();
                }
            }
        };
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Log.e(TAG,"There was an error logging the user to Firebase using the Facebook login token");
                    Toast.makeText(getApplicationContext(), R.string.error_login_firebase, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void goMainScreen() {
        Intent intent = new Intent(this, NavActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void write_new_user(String userID){

        MyUsers newUser = new MyUsers(userID,fbProfile.getFirstName(),fbProfile.getLastName(),fbProfile.getId());

        //Push to Firebase
        mDatabaseRef.child("Users_parent").child(userID).setValue(newUser);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }

}
