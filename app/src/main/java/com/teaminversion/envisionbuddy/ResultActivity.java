package com.teaminversion.envisionbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    TextView textView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("UID",userId);

// Fetch current user's document
        DocumentReference userDocRef = db.collection("users").document(userId);
        textView = findViewById(R.id.textView);
        int score = getIntent().getIntExtra("Result",0);

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Retrieve quiz_scores arraylist from Firestore
                    ArrayList<Integer> quizScores = (ArrayList<Integer>) documentSnapshot.get("quiz_scores");

                    // Add new result to quiz_scores arraylist
                    quizScores.add(score);

                    // Update the document with the new quiz_scores arraylist
                    userDocRef.update("quiz_scores", quizScores)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Document updated successfully
                                    Log.d("Quiz score", "Added successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure
                                    Log.d("Quiz score", "failed to add");
                                    Toast.makeText(ResultActivity.this, "Failed to add score to DB", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Document does not exist
                    Log.d("Quiz score", "Document not found");
                    Toast.makeText(ResultActivity.this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textView.setText("Score : " + score + "/5");

        findViewById(R.id.btn_restart).setOnClickListener(
                restart->finish()
        );
    }
}