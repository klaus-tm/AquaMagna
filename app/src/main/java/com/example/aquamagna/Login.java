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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText passwordEditText;
    private Button signIn;
    private Button signUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
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
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information

                                    Toast.makeText(Login.this, "Welcome!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}