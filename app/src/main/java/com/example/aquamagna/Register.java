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

public class Register extends AppCompatActivity {
    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private Button signIn;
    private Button signUp;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    /**
     * startup method which checks if an ongoing Auth instance exists.
     * If yes, searchUserInDatabase is called in order to navigate to the MainActivity
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            searchUserInDatabase(currentUser.getUid(), getWindow().getDecorView().findViewById(android.R.id.content));
        }
    }

    /**
     * main method which creates all the objects and UI elements
     * @param savedInstanceState - used to get the state of the instance when the user exits the app or it is rotated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        nameEditText = findViewById(R.id.nameTextRegister);
        emailEditText = findViewById(R.id.emailTextRegister);
        emailInputLayout = findViewById(R.id.emailLayoutRegister);
        passwordEditText = findViewById(R.id.passwordTextRegister);
        signIn = findViewById(R.id.signInRegister);
        signUp = findViewById(R.id.signUpRegister);

        emailEditText.addTextChangedListener(new TextWatcher() {
            /**
             * initial state of the email text field without errors
             * @param charSequence
             * @param i
             * @param i1
             * @param i2
             */
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailInputLayout.setError(null);
            }

            /**
             * check if the email text field has an actual email address
             * @param charSequence - actual char which gets analyzed
             * @param i
             * @param i1
             * @param i2
             */
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
                if (!isValidEmail) {
                    emailInputLayout.setError("Invalid email address!");
                } else if(charSequence.toString().isEmpty()){
                    emailInputLayout.setError(null);
                } else emailInputLayout.setError(null);
            }

            /**
             * leave it empty
             * @param editable
             */
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        /**
         * handler which navigates the user to the SignIp activity by Intent
         */
        signIn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        /**
         * handler which gets the name, email and password and sends them to the saveUserToAuth method
         */
        signUp.setOnClickListener(view -> {
            if (nameEditText.getText().toString().isEmpty()){
                Snackbar.make(view, "Enter a name!", Snackbar.LENGTH_SHORT).show();
                return;
            }
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
            String name = nameEditText.getText().toString();
            saveUserToAuth(name, email, password, view);
        });
    }

    /**
     * Method which creates an account in Auth using the email and password parameters.
     * When the message received is affirmative the method calls saveUserToDatabase.
     * When the message received is negative, an error Snack-bar is displayed.
     * @param name
     * @param email
     * @param password
     * @param view
     */
    private void saveUserToAuth(String name, String email, String password, View view) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null)
                                saveUserToDatabase(user.getUid(), email, name, view);
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Method which saves the user in the database using the reference and the user UID, email and name. It creates an User object and saves it to the child with the UID
     * When the message received is affirmative the method navigates the user to the MainActivity using Intent and displays a welcome Toast message.
     * When the message received is negative, an error Snack-bar is displayed.
     * @param uid
     * @param email
     * @param name
     * @param view
     */
    private void saveUserToDatabase(String uid, String email, String name, View view) {
        database = FirebaseDatabase.getInstance("https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference("users");
        DatabaseReference userRef = databaseReference.child(uid);
        User newUser = new User(name, email, "", "");
        userRef.setValue(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Register.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else Snackbar.make(view, "There has been a problem!", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * -USED ONLY BY onStart() METHOD-
     * Method which searches the user with the provided UID in the database reference using the provided link.
     * When the user gets found an Intent is created to the MainActivity and a welcome Toast message is displayed with the user name
     * If any problem persists, a Snack-bar is displayed.
     * @param uid
     * @param view
     */
    private void searchUserInDatabase(String uid, View view) {
        databaseReference = FirebaseDatabase.getInstance("https://aquamagna-77b9d-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null){
                    Toast.makeText(Register.this, "Welcome " + user.getName() + "!", Toast.LENGTH_SHORT).show();
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