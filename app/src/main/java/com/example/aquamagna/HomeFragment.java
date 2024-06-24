package com.example.aquamagna;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aquamagna.dataClasses.ScanData;
import com.example.aquamagna.dataClasses.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView title;
    private HomeAdapter homeAdapter;
    private List<ScanData> scans;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private static final String DATABASE_URL = "https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/";

    /**
     * main method which creates the fragment view, objects and UI elements
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return view To be used in the hierarchy if other fragments will come on top
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler);
        title = view.findViewById(R.id.welcomeMessage);
        auth = FirebaseAuth.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        scans = new ArrayList<>();

        setWelcomeMessage();
        getScanHistory(view);

        homeAdapter = new HomeAdapter(scans, requireContext());
        recyclerView.setAdapter(homeAdapter);
        return view;
    }

    /**
     * method which gets the scans from the database for the current user and flips it.
     * After that it populates the recycler view adapter
     * @param view
     */
    private void getScanHistory(View view) {
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("scans");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()){
                    ScanData scanData = child.getValue(ScanData.class);
                    if (scanData != null && scanData.getUser().equals(auth.getUid()))
                        scans.add(0, scanData);
                }
                if (!scans.isEmpty())
                    homeAdapter.notifyDataSetChanged();
                else
                    Snackbar.make(view, "No scans have been saved yet!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(view, "There has been a problem fetching the scan history!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
            }
        });

    }

    /**
     * Method which pulates the welcome message of the fragment with the name of the logged user
     */
    private void setWelcomeMessage() {
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(auth.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    title.setText("Scan history of " + user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}