package com.clymbra.clymbralpha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class selectGymActivity extends AppCompatActivity {

    private static final String TAG = "selectGymActivity";

    private Context mContext = selectGymActivity.this;

    private Spinner gym_spinner;

    private ArrayList<String> mGyms;

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gym);

        Button selectgym_button = findViewById(R.id.selectGym_Button);
        gym_spinner = findViewById(R.id.selectGym_Spinner);

        mGyms = new ArrayList<>();

        setupFirebaseAuth();

        populateSpinner();

        selectgym_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // update database with value
                // updateUserGym(gym_spinner.getSelectedItem().toString());
                updateUser(gym_spinner.getSelectedItem().toString());
                updateLeaderboard(gym_spinner.getSelectedItem().toString());
                // go to next activity
                Intent intent = new Intent(selectGymActivity.this, selectLevelActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void updateUser(String gym) {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String Id = currentUser.getUid();

        myRef.child("users").child(Id).child("active_gym").setValue(gym);
        myRef.child("users").child(Id).child("available_gyms").child("gym1").setValue(gym);
    }

    private void updateLeaderboard(String local_gym) {

        String username = getIntent().getExtras().getString("username");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String Id = currentUser.getUid();

        String bio = getString(R.string.nulll);
        Integer points = 0;
        String grade = getString(R.string.nulll);
        Integer total_climbs = 0;
        Integer avg_tries = 0;
        String profile_url = "https://firebasestorage.googleapis.com/v0/b/clymbr-app.appspot.com/o/profile_photos%2Fdefault-profile-picture.png?alt=media&token=22517a56-a09c-4871-b05c-3eb3463870c6";
        Integer image = 0;Leaderboards leaderboards = new Leaderboards(image, Id, username, bio, points, grade, total_climbs, avg_tries, profile_url);
        myRef.child(getString(R.string.dbname_leaderboards)).child(local_gym).child(Id).setValue(leaderboards);
    }

    private void populateSpinner() {

        FirebaseDatabase.getInstance().getReference().child("gym").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: ds: " + ds);
                    String gym = ds.getKey();
                    mGyms.add(gym);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(selectGymActivity.this, android.R.layout.simple_spinner_item, mGyms);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                gym_spinner.setAdapter(adapter);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
