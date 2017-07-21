package mx.com.redcup.redcup.myNavigationFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyPosts;

public class PostFragment extends Fragment {

    private final Runnable revealAnimationRunnable = new Runnable() {
        @Override
        public void run() {

            cx = (myContainer.getRight() -10);
            cy = (myContainer.getBottom() - 10);
            finalRadius = Math.max(myContainer.getWidth(), myContainer.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(myContainer, cx, cy, 0, finalRadius);

            myContainer.setVisibility(View.VISIBLE);
            anim.start();

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    window.setStatusBarColor(getResources().getColor(R.color.colorAccentDark));
                    myContainer.setBackgroundColor(getResources().getColor(R.color.white));
                }
            });
        }
    };

    private final Runnable animationHideDrawable = new Runnable() {
        @Override
        public void run() {


            int cx = (myContainer.getRight() -10);
            int cy = (myContainer.getBottom() - 10);

            int initialRadius = myContainer.getWidth();

            Animator anim = ViewAnimationUtils.createCircularReveal(myContainer, cx, cy, initialRadius, 0);

            myContainer.setVisibility(View.VISIBLE);
            anim.start();

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myContainer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    myContainer.setVisibility(View.INVISIBLE);
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            });
        }
    };

    LinearLayout myContainer;
    Button sendPost;
    EditText postContent;

    int cx;
    int cy;
    int finalRadius;
    int initialRadius;

    Window window;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Events_parent");


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newpost_screen, container, false);

        window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        myContainer = (LinearLayout) view.findViewById(R.id.container_newpost);
        sendPost = (Button) view.findViewById(R.id.btn_newpost_send);
        postContent = (EditText) view.findViewById(R.id.et_newpost_postcontent);

        myContainer.post(revealAnimationRunnable);

        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = postContent.getText().toString();
                String pushID = mDatabase.push().getKey();
                MyPosts newPost = new MyPosts(content,getCurrentFirebaseUID(),pushID);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    public String getCurrentFirebaseUID(){
        String UID = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            UID = user.getUid();
        } else {
            Log.e("TAG","User is unexpectedly null.");
        }
        return UID;
    }

}