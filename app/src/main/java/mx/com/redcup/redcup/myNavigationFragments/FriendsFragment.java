package mx.com.redcup.redcup.myNavigationFragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mx.com.redcup.redcup.MyHolders.NearbyEventsHolder;
import mx.com.redcup.redcup.R;
import mx.com.redcup.redcup.myDataModels.MyEvents;

public class FriendsFragment extends Fragment {

    public String TAG = "FriendsFragment";

    public DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_friends, container, false);

        // Getting handle of the Recycler view on the layout
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv_friends_recycler_view);
        rv.setHasFixedSize(false);




        //Using Firebase-UI library: FirebaseAdapter to create a recycler view getting data straight from firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Events_parent");
        // TODO Filter the events and show only those created by friends. If statement that checks MyEvents object author attribute and compare against user friends
        RecyclerView.Adapter adapter = new FirebaseRecyclerAdapter<MyEvents, NearbyEventsHolder>(MyEvents.class, R.layout.card_item, NearbyEventsHolder.class ,mDatabase){
            @Override
            protected void populateViewHolder(NearbyEventsHolder viewHolder, MyEvents event, int position) {
                viewHolder.setTitle(event.getEventContent()); //Note: This switching is on purpose. Content and title were mixed somewhere
                viewHolder.setContent(event.getEventName());
                viewHolder.setProfilePic(event.getUserID());
                viewHolder.setPostID(event.getEventID());
            }
        };

        rv.setAdapter(adapter);


        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}