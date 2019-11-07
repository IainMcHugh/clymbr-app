package com.clymbra.clymbralpha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class selectLevelActivity extends AppCompatActivity {

    private static final String TAG = "selectLevelActivity";

    RadioGroup radioGroup;
    RadioButton novice_RadioButton, intermediate_RadioButton;
    Button continue_Button;

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_level);

        radioGroup = findViewById(R.id.level_radioGroup);
        novice_RadioButton = findViewById(R.id.novice_radioButton);
        intermediate_RadioButton = findViewById(R.id.intermediate_radioButton);
        continue_Button = findViewById(R.id.continue_button);

        setupFirebaseAuth();

        continue_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if neither radiobuttons are active do nothing
                boolean novice = novice_RadioButton.isChecked();
                boolean intermediate = intermediate_RadioButton.isChecked();

                if (!novice && !intermediate){
                    Toast.makeText(selectLevelActivity.this, "Please choose one of the above options", Toast.LENGTH_SHORT).show();
                }
                else if (novice && !intermediate){
                    // update database as novice = true
                    updateUser(true);
                    progressToNextActivity();
                }
                else if (!novice && intermediate){
                    // update database as novice = false
                    updateUser(false);
                    progressToNextActivity();
                }
            }
        });

    }

    public void updateUser(boolean novice){

        String Id = mAuth.getCurrentUser().getUid();

        myRef.child("users").child(Id).child("novice").setValue(novice);

    }

    public void progressToNextActivity(){
        Intent intent = new Intent(selectLevelActivity.this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up Firebase Auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged: commencing");
                //check if the user is already logged in
                FirebaseUser user = firebaseAuth.getCurrentUser();

                Log.d(TAG, "Test 2");
                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

}
