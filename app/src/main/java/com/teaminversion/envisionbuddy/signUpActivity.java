package com.teaminversion.envisionbuddy;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class signUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etUsername, etPassword;
    private Button btnSignup;
    private TextView tvLoginRedirect;
    private ProgressBar progressBarSignup;
    private CardView cardSignup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.signup_name);
        etEmail = findViewById(R.id.signup_email);
        etUsername = findViewById(R.id.signup_username);
        etPassword = findViewById(R.id.signup_password);
        btnSignup = findViewById(R.id.signup_button);
        tvLoginRedirect = findViewById(R.id.loginRedirectText);
        progressBarSignup = findViewById(R.id.progressBarSignup);
        cardSignup = findViewById(R.id.cardSignup);

        btnSignup.setOnClickListener(view -> signupUser());

        tvLoginRedirect.setOnClickListener(view -> {
            cardSignup.setAlpha(0.5f);
            progressBarSignup.setVisibility(View.VISIBLE);
            startActivity(new Intent(signUpActivity.this, loginActivity.class));
            finish();
        });
    }

    private void signupUser() {
        cardSignup.setAlpha(0.5f);
        progressBarSignup.setVisibility(View.VISIBLE);
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Create a new document with the user's username as document ID
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("username", username);
                        userData.put("quiz_scores", new ArrayList<Integer>());

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        db.collection("users")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + userId);
                                    startActivity(new Intent(signUpActivity.this, loginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    cardSignup.setAlpha(1f);
                                    progressBarSignup.setVisibility(View.GONE);
                                    Log.w(TAG, "Error adding document", e);
                                    Toast.makeText(signUpActivity.this, "Error signing up, please try again", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // If sign up fails, display a message to the user.
                        cardSignup.setAlpha(1f);
                        progressBarSignup.setVisibility(View.GONE);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(signUpActivity.this, "User already exists.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}