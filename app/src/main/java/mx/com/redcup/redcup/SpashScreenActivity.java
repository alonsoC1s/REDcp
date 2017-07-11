package mx.com.redcup.redcup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;

import mx.com.redcup.redcup.LoginActivity;
import mx.com.redcup.redcup.NavActivity;
import mx.com.redcup.redcup.R;

public class SpashScreenActivity extends AppCompatActivity {

    String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        //Checking if the user is logged in. Else, send him to LoginScreen
        if (AccessToken.getCurrentAccessToken() == null) {
            Log.i(TAG, "Facebook access token is null. User is not logged in. Redirecting user to LoginActivity");
            goLoginScreen();
        }else {
            Log.i(TAG, "Redirecting user to Main activity");
            goMainScreen();
        }
    }

    private void goLoginScreen() {
        Log.i(TAG,"User is not authenticated. Redirecting user to login screen");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goMainScreen() {
        Log.i(TAG,"User is not authenticated. Redirecting user to login screen");
        Intent intent = new Intent(this, NavActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
