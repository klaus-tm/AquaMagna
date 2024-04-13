package com.example.aquamagna;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aquamagna.dataClasses.Company;
import com.example.aquamagna.dataClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String DATABASE_URL = "https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/";
    private Button signOut, save, delete;
    private TextInputEditText name, email, phone;
    private TextInputLayout company;
    private TextView message;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        message = view.findViewById(R.id.mainTextProfile);
        name = view.findViewById(R.id.nameTextProfile);
        email = view.findViewById(R.id.emailTextProfile);
        phone = view.findViewById(R.id.phoneTextProfile);
        company = view.findViewById(R.id.companyTextProfile);

        signOut = view.findViewById(R.id.signOut);
        save = view.findViewById(R.id.saveDetails);
        delete = view.findViewById(R.id.deleteAcc);
        auth = FirebaseAuth.getInstance();

        setInfo(auth.getCurrentUser().getUid(), view);
        populateCompanyNames();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Toast.makeText(getContext(), "See you next time!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(auth.getCurrentUser().getUid());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    User newUser = new User(name.getText().toString(), email.getText().toString(), phone.getText().toString(),company.getEditText().getText().toString());
                    databaseReference.setValue(newUser)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Snackbar.make((CoordinatorLayout)getActivity().findViewById(R.id.coordinator), "Details saved successfully!", Snackbar.LENGTH_SHORT).setAnchorView((BottomNavigationView)getActivity().findViewById(R.id.bottomNavView)).show();
                                    }
                                }
                            });
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUserFromDatabase(view);
            }
        });

        return view;
    }

    private void populateCompanyNames(){
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("companies");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> companyNames = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Company company = dataSnapshot.getValue(Company.class);
                    if (company != null) {
                        companyNames.add(company.getName());
                    }
                }
                String[] companyNameArray = companyNames.toArray(new String[0]);
                ((MaterialAutoCompleteTextView) company.getEditText()).setSimpleItems(companyNameArray);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void deleteUserFromAuth(View view) {
        if (auth.getCurrentUser().getUid() != null){
            auth.getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(), "Goodbye!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), Login.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                        else{
                            Snackbar.make(view, "Auth error!", Snackbar.LENGTH_SHORT).show();
                            Log.e("AUTH ERROR", String.valueOf(task.getException()));
                        }
                    });
        }
    }

    private void deleteUserFromDatabase(View view) {
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(auth.getCurrentUser().getUid());
        databaseReference.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        deleteUserFromAuth(view);
                    else
                        Snackbar.make(view, "Database error!", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void setInfo(String uid, View view) {
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null){
                    message.setText(user.getName() + "'s details:");
                    name.setText(user.getName());
                    email.setText(user.getEmail());
                    phone.setText(user.getPhone());
                    company.getEditText().setText(user.getCompany());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}