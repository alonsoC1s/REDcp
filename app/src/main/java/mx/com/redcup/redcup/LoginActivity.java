package mx.com.redcup.redcup;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;


public class LoginActivity extends AppCompatActivity {
    public String TAG = "LoginActivity";
    AnimationDrawable animationDrawable;
    RelativeLayout relativeLayout;

    Profile fbProfile;

    Boolean firstLogin = true;
    String email;
    String emailPassword;

    Button signUpSelector;
    Button logInSelector;
    Button FBsignUp;
    Button FBlogIn;
    Button emailSignUp;
    Button emailLogIn;
    FrameLayout signUpButtons;
    FrameLayout logInButtons;

    Boolean isFacebookUser;

    //Declaring FB components
    private CallbackManager callbackManager;
    ProfileTracker mProfileTracker;

    //Declaring Firebase components
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users_parent");

    ValueEventListener checkIfName = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!dataSnapshot.hasChild("displayName")) {
                Toast.makeText(getApplicationContext(),"Are you new to Redcupa? We are creating a new user for you", Toast.LENGTH_LONG).show();
                firstLogin = true;
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(),"Are you new to Redcupa? We are creating a new user for you", Toast.LENGTH_LONG).show();
            firstLogin = true;
        }
    };


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

        //UI Components
        signUpSelector = (Button) findViewById(R.id.btn_signUp);
        logInSelector = (Button) findViewById(R.id.btn_logIn);
        FBlogIn = (Button) findViewById(R.id.button_fb_LogIn);
        FBsignUp = (Button) findViewById(R.id.button_fb_signUp);
        emailLogIn = (Button)findViewById(R.id.button_email_LogIn);
        emailSignUp = (Button) findViewById(R.id.button_email_SignUp);
        signUpButtons = (FrameLayout) findViewById(R.id.buttons_SignUp);
        logInButtons = (FrameLayout) findViewById(R.id.buttons_LogIn);

        isFacebookUser = false;


        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
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
                if (user != null) {
                    mDatabaseRef.child(user.getUid()).addListenerForSingleValueEvent(checkIfName);
                    Log.i(TAG,"Login to Firebase was successful. Redirecting to Sign up activity");
                    if (firstLogin) {
                        if (isFacebookUser){
                            goUserCreationScreen(user.getUid(),true);
                        }else{
                            goUserCreationScreen(user.getUid(),false);
                        }
                    } else{
                        goMainScreen();
                    }
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

    private void goUserCreationScreen(String userID, Boolean isFacebookUser) {
        Intent intent = new Intent(this, UserCreationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle userData = new Bundle();
        userData.putBoolean("facebook_user", isFacebookUser);

        if (isFacebookUser) {
            userData.putString("first_name", fbProfile.getFirstName());
            userData.putString("last_name", fbProfile.getLastName());
            userData.putString("facebook_id", fbProfile.getId());
            userData.putString("firebase_id", userID);
            intent.putExtras(userData);
        }else {
            userData.putString("email",email);
            userData.putString("password",emailPassword);
            intent.putExtras(userData);
        }
        startActivity(intent);
    }

    private void goMainScreen(){
        Intent intent = new Intent(this,NavActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

    public void initFBlogin(View view){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","user_birthday"));
        isFacebookUser = true;
    }

    public void showSignUpButtons(View view){
        logInButtons.setVisibility(View.GONE);
        signUpButtons.setVisibility(View.VISIBLE);
        firstLogin = true;
    }

    public void showLogInButtons(View view){
        logInButtons.setVisibility(View.VISIBLE);
        signUpButtons.setVisibility(View.GONE);
        firstLogin = false;
    }

    public void showEmailSignUp(View view){
        isFacebookUser = false;
        Dialog loginDialog = new Dialog(this);
        loginDialog.setContentView(R.layout.dialog_email_login);

        final EditText emailField;
        final EditText passwordField;
        final Button continueButton;

        //Get UI elements
        emailField = (EditText) loginDialog.findViewById(R.id.et_dialog_email);
        passwordField = (EditText) loginDialog.findViewById(R.id.et_dialog_password);
        continueButton = (Button) loginDialog.findViewById(R.id.btn_dialog_continue);

        loginDialog.show();



        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String inputEmail = emailField.getText().toString();
                final String inputPassword = passwordField.getText().toString();

                if (firstLogin) {
                    firebaseAuth.createUserWithEmailAndPassword(inputEmail, inputPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.i("UserCreationAnonymous", "User successfully created with email. Redirecting");
                            email = inputEmail;
                            emailPassword = inputPassword;
                        }
                    });
                }else {
                    firebaseAuth.signInWithEmailAndPassword(inputEmail,inputPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                goMainScreen();
                            }else {
                                Toast.makeText(getApplicationContext(),"Are you new to Redcupa? We are creating a new user for you", Toast.LENGTH_LONG).show();
                                firstLogin = true;
                                continueButton.callOnClick();
                            }
                        }
                    });
                }
            }
        });

    }

}
