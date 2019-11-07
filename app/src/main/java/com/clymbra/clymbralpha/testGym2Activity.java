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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class testGym2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "testGym2Activity";

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    RelativeLayout relativeLayout;
    ImageButton btnTag;
    ImageButton gym_image;

    String logo_url;

    String gym_selected;
    ImageView clk_gym_image;
    TextView gym_name, gym_bio, active_users;
    RelativeLayout gym_relativeLayout;
    ImageButton goBack;

    long user_count = 0;

    ImageView birdseye;
    ArrayList<String> coordinates;
    ArrayList<String> mGym_zones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gym2);

        //---------------------------INITIALIZE NAV BAR---------------------------------------------

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_view);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.nav_gym :
                        break;

                    case R.id.nav_stats :
                        Intent intent2 = new Intent(testGym2Activity.this, StatsActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.nav_train :
                        Intent intent3 = new Intent(testGym2Activity.this, TrainActivity.class);
                        startActivity(intent3);
                        break;

                    case R.id.nav_profile:
                        Intent intent4 = new Intent(testGym2Activity.this, ProfileActivity.class);
                        startActivity(intent4);
                        break;

                }

                return false;
            }
        });

        //------------------------------------------------------------------------------------------



        coordinates = new ArrayList<>();
        mGym_zones = new ArrayList<>();

        relativeLayout = findViewById(R.id.testGym2_Relativelayout);

        gym_image = findViewById(R.id.gym_imageButton);
        birdseye = findViewById(R.id.birdseye_Imageview);

        gym_relativeLayout = findViewById(R.id.gym_click_Relativelayout);
        clk_gym_image = findViewById(R.id.gymLogo_Imageview);
        gym_name = findViewById(R.id.gymName_Textview);
        gym_bio = findViewById(R.id.gymBio_Textview);
        active_users = findViewById(R.id.gymActiveUsers_Textview);
        goBack = findViewById(R.id.clkExit_Button);

        setupFirebaseAuth();

        getUserGym();

        gym_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gym_relativeLayout.bringToFront();
                gym_relativeLayout.setVisibility(View.VISIBLE);

                Picasso.get().load(logo_url).fit().into(clk_gym_image);
                gym_name.setText(gym_selected);
//                gym_bio.setText(some gym bio)

                myRef.child("leaderboards").child(gym_selected).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user_count = dataSnapshot.getChildrenCount();
                        active_users.setText(getResources().getString(R.string.active_users, user_count));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                goBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gym_relativeLayout.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    private void getUserGym() {

        // NEED TO GET THE ACTIVE GYM
        String currentUser = mAuth.getCurrentUser().getUid();

        DatabaseReference activeGymRef = myRef.child("users").child(currentUser).child("active_gym");

        activeGymRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gym_selected = dataSnapshot.getValue().toString();
                Log.d(TAG, "onDataChange: Gym Selected: " + gym_selected);

                setGymView();
                setGymButtons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void setGymView() {

        Log.d(TAG, "setGymView: TEST: " + gym_selected);
        FirebaseDatabase.getInstance().getReference().child("gym").child(gym_selected).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: ds2: " + ds);
                    if (ds.getKey().equals("birdseye_url")){
                        String birdseye_url = ds.getValue().toString();
                        Log.d(TAG, "onDataChange: birdseye: " + birdseye_url);
                        Picasso.get().load(birdseye_url).fit().into(birdseye);
                    }
                    if (ds.getKey().equals("logo_url")){
                        logo_url = ds.getValue().toString();
                        Log.d(TAG, "onDataChange: logo: " + logo_url);
                        Picasso.get().load(logo_url).fit().into(gym_image);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setGymButtons() {


        FirebaseDatabase.getInstance().getReference().child("gym").child(gym_selected).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: ds:    " + ds);
                    for (DataSnapshot ds2: ds.getChildren()){
                        Log.d(TAG, "onDataChange: ds2:       " + ds2);
                        if (ds2.getKey().equals("x_coordinate") || ds2.getKey().equals("y_coordinate")){
                            Log.d(TAG, "onDataChange: CHILD KEY: " + ds.getKey());
                            Log.d(TAG, "onDataChange: KEY:    " + ds2.getKey());
                            Log.d(TAG, "onDataChange: VALUE:   " + ds2.getValue());
                            if (!(mGym_zones.contains(ds.getKey()))){
                                mGym_zones.add(ds.getKey());
                            }
                            coordinates.add(ds2.getValue().toString());
                        }
                    }
                }
                Log.d(TAG, "onDataChange: coordinates:   " + coordinates);
                Log.d(TAG, "onDataChange: Gym Zones: " + mGym_zones);
                int tag = 0;
                for (int i = 0;i<coordinates.size();i++){
                    if (i % 2 == 0){


                        // ImageBtnTag[tag] = new ImageButton(testGym2Activity.this);
                        btnTag = new ImageButton(testGym2Activity.this);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(convertDptoPx(50), convertDptoPx(50));

//                        ImageBtnTag[tag].setBackground(getResources().getDrawable(R.drawable.circleorange));
                        btnTag.setBackground(getResources().getDrawable(R.drawable.circleorange));

                        Log.d(TAG, "onDataChange: leftMargin: " + coordinates.get(i));
                        params.leftMargin = convertDptoPx(Integer.parseInt(coordinates.get(i)));
                        Log.d(TAG, "onDataChange: TopMargin: " + coordinates.get(i+1));
                        params.topMargin = convertDptoPx(Integer.parseInt(coordinates.get(i+1)));

//                        ImageBtnTag[tag].setClickable(true);
//                        ImageBtnTag[tag].setId(tag);
//                        ImageBtnTag[tag].setTag(tag);
                        btnTag.setClickable(true);
                        btnTag.setId(tag+1);
                        btnTag.setTag(tag+1);
                        btnTag.setOnClickListener(testGym2Activity.this);

                        Log.d(TAG, "onDataChange: tag: " + tag);
                        tag = tag + 1;

//                        relativeLayout.addView(ImageBtnTag[tag], params);
                        relativeLayout.addView(btnTag, params);
                    }

                }



            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int convertDptoPx(int dp){

        float d = testGym2Activity.this.getResources().getDisplayMetrics().density;
        int margin = (int)(dp * d); // margin in pixels
        return margin;
    }

    @Override
    public void onClick(View v) {

        String zone_selected = v.getTag().toString();
        // Toast.makeText(testGym2Activity.this, "Zone: " + zone_selected, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(testGym2Activity.this, testGym3Activity.class);
        Bundle extras = new Bundle();
        extras.putString("gym", gym_selected);
        extras.putString("zone", zone_selected);
        intent.putExtras(extras);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------

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
