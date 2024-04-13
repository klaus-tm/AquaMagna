package com.example.aquamagna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.aquamagna.dataClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText passwordEditText;
    private Button signIn;
    private Button signUp;
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            searchUserInDatabase(currentUser.getUid(), getWindow().getDecorView().findViewById(android.R.id.content));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailTextLogin);
        emailInputLayout = findViewById(R.id.emailLayoutLogin);
        passwordEditText = findViewById(R.id.passwordTextLogin);
        signIn = findViewById(R.id.signInLogin);
        signUp = findViewById(R.id.signUpLogin);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailInputLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
                if (!isValidEmail) {
                    emailInputLayout.setError("Invalid email address!");
                } else if(charSequence.toString().isEmpty()){
                    emailInputLayout.setError(null); // Clear the error message
                } else emailInputLayout.setError(null); // Clear the error message
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish();
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailEditText.getText().toString().isEmpty()){
                    Snackbar.make(view, "Enter an email!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (passwordEditText.getText().toString().isEmpty()){
                    Snackbar.make(view, "Enter a password!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                searchUserInAuth(email, password, view);
            }
        });
    }

    private void searchUserInAuth(String email, String password, View view) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            searchUserInDatabase(user.getUid(), view);
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void searchUserInDatabase(String uid, View view) {
        databaseReference = FirebaseDatabase.getInstance("https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null){
                    Toast.makeText(Login.this, "Welcome " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}