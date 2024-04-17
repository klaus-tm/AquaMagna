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