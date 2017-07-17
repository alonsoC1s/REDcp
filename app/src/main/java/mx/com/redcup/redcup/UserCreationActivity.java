package mx.com.redcup.redcup;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import mx.com.redcup.redcup.myDataModels.MyUsers;

public class UserCreationActivity extends AppCompatActivity {

    public StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    public DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users_parent");

    EditText userNameField;
    EditText userLastNameField;
    TextInputLayout passwordContainer;
    EditText userPasswordField;
    ImageView userProfilePic;
    ImageView userCoverImage;
    Button completeSignUp;

    Boolean facebookUser;
    String facebookID;

    String userEmail;
    String userPassword;

    View.OnClickListener coverImagePickerBuilder = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }
    };

    View.OnClickListener profileImagePickerBuilder = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 2);
        }
    };

    View.OnClickListener completeSignUpProcess = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userName = userLastNameField.getText().toString();
            String userLastName = userLastNameField.getText().toString();

            if (TextUtils.isEmpty(userName)){
                userNameField.setError("Required");
            } else if (TextUtils.isEmpty(userLastName)){
                userLastNameField.setError("Required");
            }


            if (facebookUser){
                write_new_user_fb(getCurrentFirebaseUID(),userName,userLastName,facebookID, v);
                goMainScreen();
            }else {
                write_new_user_email(getCurrentFirebaseUID(),userName,userLastName,v);
                goMainScreen();
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_creation);

        Bundle userInfo = getIntent().getExtras();
        facebookUser = userInfo.getBoolean("facebook_user");

        userNameField = (EditText)findViewById(R.id.et_username);
        userLastNameField = (EditText)findViewById(R.id.et_userLastname);
        userPasswordField = (EditText) findViewById(R.id.et_userPassword);
        passwordContainer = (TextInputLayout) findViewById(R.id.password_container);
        userProfilePic = (ImageView) findViewById(R.id.signup_profilePicture);
        userCoverImage = (ImageView) findViewById(R.id.signup_coverimage);
        completeSignUp = (Button) findViewById(R.id.btn_SignUpForm);

        if (facebookUser){
            passwordContainer.setVisibility(View.GONE);
            facebookID = userInfo.getString("facebook_id");
            String userName = userInfo.getString("first_name");
            String userLastName = userInfo.getString("last_name");

            userNameField.setText(userName);
            userLastNameField.setText(userLastName);

        }else {
            passwordContainer.setVisibility(View.GONE);
            userEmail = userInfo.getString("email");
        }

        mStorageRef.child(getCurrentFirebaseUID());

        userProfilePic.setOnClickListener(profileImagePickerBuilder);
        userCoverImage.setOnClickListener(coverImagePickerBuilder);
        completeSignUp.setOnClickListener(completeSignUpProcess);
    }

    private void write_new_user_fb(String userID,String userName, String userLastName, String facebookID, View v){
        MyUsers newUser = new MyUsers(userID,userName,userLastName,facebookID);

        //Push to Firebase
        mDatabaseRef.child(userID).setValue(newUser);

        Snackbar.make(v,String.format("Welcome to Redcupa %s !!", userName),Snackbar.LENGTH_LONG).show();

    }

    private void write_new_user_email(String userID,String userName, String userLastName, View v){
        MyUsers newUser = new MyUsers(userID,userName,userLastName);

        //Push to Firebase
        mDatabaseRef.child(userID).setValue(newUser);

        Snackbar.make(v,String.format("Welcome to Redcupa %s !!", userName),Snackbar.LENGTH_LONG).show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnedImage) {
        if (resultCode == RESULT_OK){
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/png").setContentEncoding("png").build();
            UploadTask uploadTask;
            if (requestCode == 1) {
                Uri imageUri = returnedImage.getData();
                mStorageRef.child("cover_image").putFile(imageUri,metadata);

                //mDataBaseRef_users.child(getCurrentFirebaseUID()).child("cover_image_name").setValue("");
            } else if (requestCode == 2){
                Uri imageUri = returnedImage.getData();
                mStorageRef.child("profile_picture").putFile(imageUri,metadata);
            }
        }
    }

    private void goMainScreen(){
        Intent intent = new Intent(this,NavActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public String getCurrentFirebaseUID(){
        String UID = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            UID = user.getUid();
        } else {
            Log.e("UserCreationActivity","User is unexpectedly null.");
        }
        return UID;
    }
}
