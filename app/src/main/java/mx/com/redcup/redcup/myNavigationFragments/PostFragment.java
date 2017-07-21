package mx.com.redcup.redcup.myNavigationFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import mx.com.redcup.redcup.R;

public class PostFragment extends Fragment {

    LinearLayout myContainer;

    int cx;
    int cy;
    int finalRadius;
    int initialRadius;

    Window window;

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



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newpost_screen, container, false);

        window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        myContainer = (LinearLayout) view.findViewById(R.id.container_newpost);

        myContainer.post(revealAnimationRunnable);

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


}