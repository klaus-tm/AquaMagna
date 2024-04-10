package com.example.aquamagna;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;

public class ProfileFragment extends Fragment {
    private static final String DATABASE_URL = "https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/";
    private Button signOut, save, delete;
    private TextInputEditText name, email, phone, birth;
    private TextView message;
    private RadioGroup gender;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    boolean isMale = false;
    private MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select birth date").build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        message = view.findViewById(R.id.mainTextProfile);
        name = view.findViewById(R.id.nameTextProfile);
        email = view.findViewById(R.id.emailTextProfile);
        phone = view.findViewById(R.id.phoneTextProfile);
        birth = view.findViewById(R.id.birthTextProfile);
        gender = view.findViewById(R.id.genderRadio);

        signOut = view.findViewById(R.id.signOut);
        save = view.findViewById(R.id.saveDetails);
        delete = view.findViewById(R.id.deleteAcc);
        auth = FirebaseAuth.getInstance();

        setInfo(auth.getCurrentUser().getUid(), view);

        birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show(getActivity().getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selection);
                birth.setText(date);
            }
        });

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.maleRadio) {
                    isMale = true;
                } else if(i == R.id.femaleRadio) {
                    isMale = false;
                }
            }
        });
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
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
                    User newUser = new User(name.getText().toString(), email.getText().toString(), phone.getText().toString(), birth.getText().toString(), isMale);
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

            }
        });

        return view;
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
                    if (user.getBirth() != null)
                        birth.setText(user.getBirth().toString());
                    if (user.getMale() != null) {
                        if (user.getMale())
                            gender.check(R.id.maleRadio);
                        else gender.check(R.id.femaleRadio);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}