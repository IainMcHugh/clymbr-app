package com.clymbra.clymbralpha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    TextView username, bio, localgym, points, grade, climbs, tries, rank;
    ImageView profile;

    private ProfileSettingsFragment profileSettingsFragment;

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //---------------------------INITIALIZE NAV BAR---------------------------------------------

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_view);
        bottomNavigationView.setVisibility(View.VISIBLE);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.nav_gym :
                        Intent intent1 = new Intent(ProfileActivity.this, testGym2Activity.class);
                        startActivity(intent1);
                        break;

                    case R.id.nav_stats :
                        Intent intent2 = new Intent(ProfileActivity.this, StatsActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.nav_train :
                        Intent intent4 = new Intent(ProfileActivity.this, TrainActivity.class);
                        startActivity(intent4);
                        break;

                    case R.id.nav_profile:
                        break;

                }

                return false;
            }
        });


        //------------------------------------------------------------------------------------------

        ImageButton profile_settings = findViewById(R.id.profilesettings_Button);
        username = findViewById(R.id.profileUsername_Textview);
        bio = findViewById(R.id.profileBio_Textview);
        localgym = findViewById(R.id.gym_Textview);
        points = findViewById(R.id.profile_points_Textview);
        grade = findViewById(R.id.profileGrade_Textview);
        climbs = findViewById(R.id.profileClimbs_Textview);
        tries = findViewById(R.id.profileTries_Textview);
        rank = findViewById(R.id.profileRank_Textview);
        profile = findViewById(R.id.Profile_Imageview);

        profileSettingsFragment = new ProfileSettingsFragment();

        setupFirebaseAuth();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                setUserInfo(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        profile_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigationView.setVisibility(View.GONE);
                FragmentManager fr = getSupportFragmentManager();
                fr.beginTransaction().replace(R.id.layout_profile, profileSettingsFragment).commit();
            }
        });

    }

    private void setUserInfo(DataSnapshot dataSnapshot) {

        Log.d(TAG, "setUserInfo: retrieving user information");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                FirebaseUser currentUser = mAuth.getCurrentUser();

                // get the Profile picture
                String profile_url = dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("profile_url")
                        .getValue().toString();
                Picasso.get().load(profile_url).into(profile);

                // get the username
                username.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("username")
                        .getValue().toString());

                // get the bio
                bio.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("bio")
                        .getValue().toString());

                // get the local gym
                localgym.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("active_gym")
                        .getValue().toString());

                // get the points
                points.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("points")
                        .getValue().toString());

                // get the grade and set it if not null
                if ((dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("grade")
                        .getValue().toString()).equals("")){
                    // do nothing
                } else {
                    grade.setText(dataSnapshot.child("users")
                            .child(currentUser.getUid())
                            .child("grade")
                            .getValue().toString());
                }

                // get the total climbs
                climbs.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("total_climbs")
                        .getValue().toString());

                // get the average tries
                tries.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("avg_tries")
                        .getValue().toString());

                // get the rank
                rank.setText(dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child("rank")
                        .getValue().toString());

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

    // This initially checks to see if the user is currently signed in
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

}
