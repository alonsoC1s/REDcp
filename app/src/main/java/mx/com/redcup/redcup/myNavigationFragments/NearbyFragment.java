package mx.com.redcup.redcup.myNavigationFragments;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mx.com.redcup.redcup.Holders_extensions.BottomSheetInviteFriends;
import mx.com.redcup.redcup.Holders_extensions.EventsRecyclerHolder;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyEvents;

public class NearbyFragment extends Fragment  {

    public DatabaseReference mDatabase;
    FloatingActionButton createPost;

    LinearLayout myContainer;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);


        // Getting handle of the Recycler view on the layout
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);
        createPost = (FloatingActionButton) rootView.findViewById(R.id.fab_create_post);
        myContainer = (LinearLayout) rootView.findViewById(R.id.container_newpost);
        rv.setHasFixedSize(false);



        //Using Firebase-UI library: FirebaseAdapter to create a recycler view getting data straight from firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Events_parent");
        // TODO Filter the events and show only those at a certain distance
        RecyclerView.Adapter adapter = new FirebaseRecyclerAdapter<MyEvents, EventsRecyclerHolder>(
                MyEvents.class, R.layout.recyclerrow_events, EventsRecyclerHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(EventsRecyclerHolder viewHolder, MyEvents event, int position) {

                viewHolder.setTitle(event.getEventContent()); //Note: This switching is on purpose. Content and title were mixed somewhere
                viewHolder.setContent(event.getEventName());
                viewHolder.setProfilePic(event.getUserID());
                viewHolder.setPostID(event.getEventID());

            }
        };

        rv.setAdapter(adapter);


        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);



        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostFragment map = new PostFragment();

                FragmentTransaction fm = getFragmentManager().beginTransaction().add(R.id.Nav_activity_content, map).addToBackStack("Fragment");
                fm.commit();

            }
        });


        return rootView;

    }

}
