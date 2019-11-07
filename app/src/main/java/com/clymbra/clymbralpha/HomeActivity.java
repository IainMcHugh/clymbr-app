package com.clymbra.clymbralpha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        //autoSignOut();



        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_view);

        Intent intent = new Intent(HomeActivity.this, testGym2Activity.class);
        startActivity(intent);
    }

    private void autoSignOut() {

        setupFirebaseAuth();
        mAuth.signOut();
        Intent intent2 = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent2);

    }

    // -------------------------- Firebase ----------------------------------------------

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

                // checkCurrentUser(user);
                Log.d(TAG, "Test 2");
                if(user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                    // in the event a user logs out, then presses back button to get back to
                    // ProfileFragment, during setup this function will be called to check if
                    // user is signed in, and they will be brought to login screen as below:
                    Intent intent2 = new Intent(HomeActivity.this, MainActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent2);
                }
            }
        };
    }
}
