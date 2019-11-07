package com.clymbra.clymbralpha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class StatsActivity extends AppCompatActivity implements personRecyclerViewAdapter.OnUserListener{

    private static final String TAG = "StatsActivity";

    private RelativeLayout clkRelativeLayout;
    private TextView clkUsername,clkBio, clkGrade, clkClimb, clkTries;
    private TextView gymName;
    private ImageView clkProfile;
    private String mUser, mBio;
    private ImageButton clkExit;

    private RecyclerView mRecyclerView;

    private ArrayList<String> profileArray = new ArrayList<>();
    private ArrayList<String> rankArray = new ArrayList<>();
    private ArrayList<String> userArray = new ArrayList<>();
    private ArrayList<String> uidArray = new ArrayList<>();
    private ArrayList<String> pointsArray = new ArrayList<>();

    private String user_gym;

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        //---------------------------INITIALIZE NAV BAR---------------------------------------------

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_view);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.nav_gym :
                        Intent intent1 = new Intent(StatsActivity.this, testGym2Activity.class);
                        startActivity(intent1);
                        break;

                    case R.id.nav_stats :
                        break;

                    case R.id.nav_train :
                        Intent intent3 = new Intent(StatsActivity.this, TrainActivity.class);
                        startActivity(intent3);
                        break;

                    case R.id.nav_profile:
                        Intent intent4 = new Intent(StatsActivity.this, ProfileActivity.class);
                        startActivity(intent4);
                        break;

                }

                return false;
            }
        });


        //------------------------------------------------------------------------------------------

        clkRelativeLayout = findViewById(R.id.user_click_Relativelayout);
        clkProfile = findViewById(R.id.clkProfile_Imageview);
        clkUsername = findViewById(R.id.clkprofileUsername_Textview);
        clkBio = findViewById(R.id.clkprofileBio_Textview);
        clkGrade = findViewById(R.id.clkprofileGrade_Textview);
        clkClimb = findViewById(R.id.clkprofileClimbs_Textview);
        clkTries = findViewById(R.id.clkprofileTries_Textview);
        gymName = findViewById(R.id.gymname_Textview);

        clkExit = findViewById(R.id.clkExit_Button);

        setupFirebaseAuth();
        getUserGym();
        getLeaderboardData();

    }

    private void getUserGym() {

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_gym =  dataSnapshot.child("users").child(mAuth.getCurrentUser().getUid()).child("active_gym").getValue().toString();
                gymName.setText(user_gym);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getLeaderboardData() {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // retreieves the leaderboard data from leaderboard node
                // have to order data by child points - value
                // return username, points

                Query usersOrderedByPoints = myRef.child("leaderboards").child(user_gym).orderByChild("points");

                usersOrderedByPoints.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: ds is: " + ds);
                            Leaderboards leaderboard = ds.getValue(Leaderboards.class);

                            profileArray.add((leaderboard.getProfile_url()));

//                            Log.d(TAG, "USER: " + leaderboard.getUsername());
                            userArray.add(leaderboard.getUsername());

//                            Log.d(TAG, "UID: " + leaderboard.getUserId());
                            uidArray.add(leaderboard.getUserId());

//                            Log.d(TAG, "POINTS: " + leaderboard.getPoints());
                            pointsArray.add((leaderboard.getPoints()).toString());

                        }

                        for (int i = 1; i < (userArray.size()+1);i++){
                            rankArray.add(Integer.toString(i));
                        }

                        Collections.reverse(profileArray);
                        Collections.reverse(userArray);
                        Collections.reverse(uidArray);
                        Collections.reverse(pointsArray);

                        Log.d(TAG, "onDataChange: userArray size: " + userArray.size());
                        Log.d(TAG, "onDataChange: uidArray size: " + uidArray.size());
                        Log.d(TAG, "onDataChange: pointsArray size: " + pointsArray.size());

                        RecyclerView recyclerView = findViewById(R.id.leaderboard_recyclerview);
                        personRecyclerViewAdapter adapter = new personRecyclerViewAdapter(profileArray, rankArray,
                                userArray, uidArray, pointsArray, StatsActivity.this);
//                        personRecyclerViewAdapter adapter = new personRecyclerViewAdapter(imageArray, rankArray,
//                                userArray, uidArray, pointsArray, gradesArray, totalClimbsArray, triesArray, StatsActivity.this);

                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(StatsActivity.this));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

//                Log.d(TAG, "usersOrderedByPoints: " + usersOrderedByPoints.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onUserClick(int i) {
        Toast.makeText(StatsActivity.this, "User: " + (userArray.get(i)), Toast.LENGTH_SHORT).show();
        //when user is clicked on open a fragment with user image, name, bio, points
        clkRelativeLayout.setVisibility(View.VISIBLE);
        mUser = uidArray.get(i);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String clk_url = dataSnapshot.child("leaderboards").child(user_gym).child(mUser).child("profile_url").getValue().toString();
                Picasso.get().load(clk_url).into(clkProfile);
                clkUsername.setText(dataSnapshot.child(getString(R.string.dbname_leaderboards)).child(user_gym).child(mUser).child("username").getValue().toString());
                clkBio.setText(dataSnapshot.child(getString(R.string.dbname_leaderboards)).child(user_gym).child(mUser).child("bio").getValue().toString());
                clkGrade.setText("Climbing Grade: " + dataSnapshot.child(getString(R.string.dbname_leaderboards)).child(user_gym).child(mUser).child("grade").getValue());
                clkClimb.setText("Total Climbs: " + dataSnapshot.child(getString(R.string.dbname_leaderboards)).child(user_gym).child(mUser).child("total_climbs").getValue().toString());
                clkTries.setText("Average Tries: " + dataSnapshot.child(getString(R.string.dbname_leaderboards)).child(user_gym).child(mUser).child("avg_tries").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        clkExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if exit hide the relative layout
                clkRelativeLayout.setVisibility(View.GONE);
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

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

}
