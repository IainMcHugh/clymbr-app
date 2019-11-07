package com.clymbra.clymbralpha;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class testGym3Activity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "testGym3Activity";

    /* TODO: Tries box needs option to recommend grade change for selected climb
    *   This option will only be available for intermediate climbers (novice: false)
    *   Recommendation is recorded in gym->boulder-> new node:
    *   {..{'6b+': 2, '6c': 4}}
    *   Displayed to user as "4 votes for '6c', (5-4 = ) 1 more vote needed for change
    *   If grade changed then all users with this complete get extra points,
    *   recommendation node is removed,
    *   new value = grade_change set to true.
    *   This means no more recommendations can be made*/

    // Firebase stuff
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    private ImageButton goBack;

    private String gym, zone, colour;
    private String zone_url;
    private Integer zone_height, zone_width, zone_height_test;
    private double dim_ratio;

    private RelativeLayout route_RelativeLayout;
    private HorizontalScrollView route_HorizontalScrollview;
    private ImageView zone_ImageView;
    private ProgressBar progressBar;

    private ArrayList<String> mRoute_colour;
    private ArrayList<String> mRoute_colour1;
    private ArrayList<String> mRoute_colour2;
    private ArrayList<String> mRoute_id;
    private ArrayList<Integer> mRoute_xpos;

    private Button routeBtn;

    private Button completeBoulder;
    private ImageButton updateTries, updateGrade;
    private boolean novice;
    private TextView boulder_points, boulder_class, boulder_grade, boulder_completed;
    private TextView tries_title, boulder_avgTries, boulder_progress;

    private RelativeLayout tries;
    private ImageView increment, decrement, confirmTries, declineTries;
    public int selected_boulder = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gym3);

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
                        Intent intent1 = new Intent(testGym3Activity.this, testGym2Activity.class);
                        startActivity(intent1);
                        break;

                    case R.id.nav_stats :
                        Intent intent2 = new Intent(testGym3Activity.this, StatsActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.nav_train :
                        Intent intent3 = new Intent(testGym3Activity.this, TrainActivity.class);
                        startActivity(intent3);
                        break;

                    case R.id.nav_profile:
                        Intent intent4 = new Intent(testGym3Activity.this, ProfileActivity.class);
                        startActivity(intent4);
                        break;

                }

                return false;
            }
        });

        //------------------------------------------------------------------------------------------

        gym = getIntent().getExtras().getString("gym"); // THIS IS THE FULL NAME
        zone = getIntent().getExtras().getString("zone"); // THIS IS A NUMBER

        route_HorizontalScrollview = findViewById(R.id.horziontal_scrollview);
        route_RelativeLayout = findViewById(R.id.route_RelativeLayout);
        zone_ImageView = findViewById(R.id.zone_Imageview);
        progressBar = findViewById(R.id.progress_bar);

        updateGrade = findViewById(R.id.updateGrade_imageButton);
        completeBoulder = findViewById(R.id.topout_Button);
        updateTries = findViewById(R.id.updateTries_Button);
        boulder_points = findViewById(R.id.thepoints_textView);
        boulder_class = findViewById(R.id.theclass_textView);
        boulder_grade = findViewById(R.id.thegrade_textView);
        boulder_completed = findViewById(R.id.thecompleted_textView);
        boulder_avgTries = findViewById(R.id.theavgTries_textView);
        boulder_progress = findViewById(R.id.theprogress_textView);
        tries_title = findViewById(R.id.goodjob_Textview);
        tries = findViewById(R.id.tries_Relativelayout);
        increment = findViewById(R.id.increment_Imagebutton);
        decrement = findViewById(R.id.decrement_Imagebutton);
        confirmTries = findViewById(R.id.confirmtries_Imagebutton);
        declineTries = findViewById(R.id.declinetries_Imagebutton);

        mRoute_colour = new ArrayList<>();
        mRoute_colour1 = new ArrayList<>();
        mRoute_colour2 = new ArrayList<>();
        mRoute_id = new ArrayList<>();
        mRoute_xpos = new ArrayList<>();

        int h = route_HorizontalScrollview.getHeight();
        Log.d(TAG, "onCreate: SCROLLVIEW HEIGHT: " + h);

        // setZoneView();
        // setZoneButtons();

        route_HorizontalScrollview.post(new Runnable() {
            @Override
            public void run() {
                zone_height_test = route_HorizontalScrollview.getHeight();
                Log.d(TAG, "run: ZONE HEIGHT TEST: " + zone_height_test);
                setZoneView();
            }
        });

        // INITIALIZING WIDGETS

        goBack = findViewById(R.id.goBack_imageButton);

        // RUNNING METHODS AND ONCLICKS

        setupFirebaseAuth();


        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(testGym3Activity.this, testGym2Activity.class).putExtra("gym", gym);
                startActivity(intent);
            }
        });

        route_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if tries box is visible, do nothing
                if (tries.getVisibility() == View.VISIBLE) {
                    // do nothing

                } else {

                    boulder_points.setText(R.string.nulll);
                    boulder_grade.setText(R.string.nulll);
                    boulder_class.setText(R.string.nulll);
                    boulder_completed.setText(R.string.nulll);
                    boulder_avgTries.setText(R.string.nulll);
                    boulder_progress.setText(R.string.nulll);
                    completeBoulder.setVisibility(View.INVISIBLE);
                    updateTries.setVisibility(View.INVISIBLE);
                    updateGrade.setVisibility(View.GONE);
                }

            }

        });

        updateGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Recommend a grade change
                //make visible spinner dialog
            }
        });

        updateTries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tries_title.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                tries_title.setText(R.string.update_tries);
                showTriesBox();
            }
        });

        completeBoulder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // first need to check if this boulder is already topped out
                String checkBoulder = (String) completeBoulder.getText();
                // if button says "Topped out!"
                if (checkBoulder.equals(getResources().getString(R.string.topout_complete))){
                    //do nothing
                }
                else {
                    tries_title.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                    tries_title.setText(R.string.good_job);
                    showTriesBox();
                }
            }
        });

    }



    private void showTriesBox() {

        // get the numerical value of string in tries textview
        final TextView tries_tv = findViewById(R.id.notries_Textview);
        final TextView points_tv = findViewById(R.id.thepoints_textView);;

        //reset the amount of tries to 1
        tries_tv.setText("1");
        //make RelativeLayout visible for amount of tries taken
        tries.setVisibility(View.VISIBLE);

        // should only be able to click on tries Relative Layout now
        // tries box is now visible, four options for user {increment, decrement, confirm, decline}
        // if increment is clicked
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // converting value to integer and adding 1
                Integer amount_tries = (Integer.parseInt(tries_tv.getText().toString()) + 1);
                // putting it back as a String
                String new_tries = amount_tries.toString();
                // updating the tries textview to incremented value
                tries_tv.setText(new_tries);

            }
        });
        // if decrement is clicked
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the numerical value of string in tries textview
                TextView tries_tv;
                tries_tv = findViewById(R.id.notries_Textview);
                //check to see if tries is 1
                if (tries_tv.getText().equals("1")){
                    //do nothing
                } else{
                    // converting value to integer and subtracting 1
                    Integer amount_tries = (Integer.parseInt(tries_tv.getText().toString()) - 1);
                    // putting it back as a String
                    String new_tries = amount_tries.toString();
                    // updating the tries textview to decremented value
                    tries_tv.setText(new_tries);
                }
            }
        });

        // if go back is clicked
        declineTries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the numerical value of string in tries textview
                TextView tries_tv;
                tries_tv = findViewById(R.id.notries_Textview);
                // set this back to 1
                tries_tv.setText(R.string.default_tries);
                // set the RelativeLayout invisible again
                tries.setVisibility(View.GONE);
                // reset the "Press to top out text"
                completeBoulder.setText(getResources().getString(R.string.topout));
            }
        });

        // if confirm is clicked
        confirmTries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tries_title.getText() == getString(R.string.update_tries)){
                    Log.d(TAG, "onClick: ADDING TRIES");
                    //just update user tries but don't complete route
                    updateTries(tries_tv.getText().toString(), gym, "zone_" + zone, colour); // 2
                    // set the RelativeLayout invisible again
                    tries.setVisibility(View.GONE);
                    //update text fields with done and tries amount
//                     int curr_tries = Integer.parseInt(boulder_progress.getText().toString()); // 0 or 3
//                     Log.d(TAG, "onClick: curr_tries2: " + curr_tries);
//                     int new_tries = curr_tries + Integer.parseInt(tries_tv.getText().toString()); // (0 or 3) + 2 = (2 or 5)
//                     Log.d(TAG, "onClick: new_tries2:" + new_tries);
//                     boulder_progress.setText(Integer.toString(new_tries)); // (2 or 5)

                } else {
                    Log.d(TAG, "onClick: COMPLETE ROUTE");
                    // update user tries and points
                    updateRoute(tries_tv.getText().toString(), points_tv.getText().toString(), gym, "zone_" + zone, colour); //2
                    // set the RelativeLayout invisible again
                    tries.setVisibility(View.GONE);

                    // set the text to "TOPPED OUT!"
                    completeBoulder.setText(getResources().getString(R.string.topout_complete));
                    //update text fields with done and tries amount
                    boulder_completed.setText(R.string.dbname_boulder_completed);
                    boulder_completed.setTextColor(getResources().getColor(R.color.colorGreen));
//                    boulder_progress.setText(tries_tv.getText().toString());
                    Log.d(TAG, "onClick: ANOTHER TEST: " + boulder_progress.getText().toString());
                    // update the average tries for that boulder
                    updateAverageTries(tries_tv.getText().toString(), gym, "zone_" + zone, colour); //2 should be 2+4 = 6
                }


            }
        });

    }

    private void setZoneView() {

        FirebaseDatabase.getInstance().getReference().child("gym").child(gym).child("zone_" + zone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    // Get the original image dimensions from URL

                    if (ds.getKey().equals("zone_height")){
                        zone_height = Integer.parseInt(ds.getValue().toString());
                        Log.d(TAG, "onDataChange: Zone Height: " + zone_height);
                    }
                    if (ds.getKey().equals("zone_width")){
                        zone_width = Integer.parseInt(ds.getValue().toString());
                    }
                    if (ds.getKey().equals("zone_url")){
                        zone_url = ds.getValue().toString();
                        // Log.d(TAG, "onDataChange: Zone_URL: " + zone_url);


                        // Load the image into zone ImageView

//                        Picasso.get().load(zone_url).fit().into(zone_ImageView, new Callback() {
//                            @Override
//                            public void onSuccess() {
//                                progressBar.setVisibility(View.GONE);
//                                int h2 = route_HorizontalScrollview.getHeight();
//                                Log.d(TAG, "onDataChange: h2: " + h2);
//                                setZoneButtons();
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                Log.e(TAG, "onError: Error loading image:", e);
//
//                            }
//                        });

                    }
                }
                double factor = (double) zone_height_test/zone_height;
                int zone_width_test = (int) (zone_width * factor);
                Log.d(TAG, "onDataChange: ZONE WIDTH: " + zone_width);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(zone_width_test, zone_height_test);
                zone_ImageView.setLayoutParams(params);
                Log.d(TAG, "onDataChange: ZONE HEIGHT TEST: " + zone_height_test);
                Log.d(TAG, "onDataChange: ZONE WIDTH TEST: " + zone_width_test);
                // Picasso.get().load(zone_url).fit().into(zone_ImageView);

                Picasso.get().load(zone_url).fit().into(zone_ImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: SUCCESS!");
                        progressBar.setVisibility(View.GONE);
                        setZoneButtons();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: Error loading image", e);
                    }
                });
//                 Picasso.get().load(zone_url).placeholder(R.mipmap.thewall_leftzone).fit().into(zone_ImageView);


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setZoneButtons() {

        FirebaseDatabase.getInstance().getReference().child("gym").child(gym).child("zone_" + zone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: dataSnapshot: " + dataSnapshot);
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.hasChildren()){
                        Log.d(TAG, "onDataChange: ds: " + ds.getKey());
                        String colour = ds.getKey(); // new
//                        mRoute_colour.add(ds.getKey());
                        for (DataSnapshot ds2: ds.getChildren()){
                            mRoute_id.add(ds2.getKey());
                            mRoute_colour.add(colour);
                            Log.d(TAG, "onDataChange: ds2: " + ds2.getKey());
                            for (DataSnapshot ds3: ds2.getChildren()){
                                if (ds3.getKey().equals("x_pos")){
                                    mRoute_xpos.add(Integer.parseInt(ds3.getValue().toString()));
                                    Log.d(TAG, "onDataChange: x_pos: " + ds3.getValue().toString());
                                }
                                if (ds3.getKey().equals("colour1")){
                                    mRoute_colour1.add(ds3.getValue().toString());
                                }
                                if (ds3.getKey().equals("colour2")){
                                    mRoute_colour2.add(ds3.getValue().toString());
                                }
                            }
                        }
                    }
                }
                Log.d(TAG, "onDataChange: mRoute_colour.size:" + mRoute_id.size());
                // For each button
                for (int i = 0; i < mRoute_id.size(); i ++){
                    Log.d(TAG, "onDataChange: i" + i);
                    // Create a new button
                    routeBtn = new Button(testGym3Activity.this);
                    // Define width and height of 30 for relative layout parameters
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)convertDptoPx(30), (int)convertDptoPx(30));
                    // id is equal to Resource ID of generic circle
//                    int id = getResources().getIdentifier("circle", "drawable", testGym3Activity.this.getPackageName());
                    int id = getResources().getIdentifier("tick_circle", "drawable", testGym3Activity.this.getPackageName());
                    // set background of button created above to this resource
                    routeBtn.setBackgroundResource(id);


                    LayerDrawable shape = (LayerDrawable) ContextCompat.getDrawable(testGym3Activity.this,R.drawable.tick_circle);
                    GradientDrawable tick1 = (GradientDrawable) shape.findDrawableByLayerId(R.id.tick1);
                    GradientDrawable circle = (GradientDrawable) shape.findDrawableByLayerId(R.id.tick_circle);

                    circle.setColor(Color.parseColor((mRoute_colour1.get(i))));
                    circle.setStroke(5, Color.parseColor(mRoute_colour2.get(i)));

                    // go to users node and check if this route has been tried/completed
                    // if so, little mark, else no mark

                    tick1.setAlpha(0);

                    routeBtn.setBackground(shape);

                    // Get the background of the button as is and store in a drawable
//                    StateListDrawable drawable = (StateListDrawable)routeBtn.getBackground();
                    // Define drawable container for the drawable above
//                    DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
                    // Get children of this drawable container and put in drawable array
//                    Drawable[] drawableItems = dcs.getChildren();
                    // Not sure
//                    GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0]; // item 1

                    //solid color
//                    gradientDrawableChecked.setColor(Color.parseColor(mRoute_colour1.get(i)));

                    //outer stroke
//                    gradientDrawableChecked.setStroke(5, Color.parseColor(mRoute_colour2.get(i)));

                    // Set the top Margin to convertDptoPx(1)
                    params.topMargin = (int) convertPxtoDp(30); // 11 in dp
                    //params.topMargin = convertDptoPx(1);

                    int h = route_HorizontalScrollview.getHeight();
                    Log.d(TAG, "onDataChange: HEIGHT: " + h);
                    Log.d(TAG, "onDataChange: IMAGE HEIGHT: " + zone_height);

                    // Factor between orig H and new H is SCRL H/ ORIG H
                    dim_ratio = (double) h/zone_height;
                    Log.d(TAG, "onDataChange: dim_ratio: " + dim_ratio);
                    // Get x_pos in PIXELS, convert to DP, then convert back to PIXELS
                    params.leftMargin = (int)((double)(mRoute_xpos.get(i))*(dim_ratio));
                    // params.leftMargin = (int) convertPxtoDp((mRoute_xpos.get(i))*(dim_ratio));

                    // params.leftMargin = convertDptoPx(convertPxtoDp(mRoute_xpos.get(i)));

                    routeBtn.setClickable(true);
                    routeBtn.setTag(R.id.routeColour, mRoute_colour.get(i));
                    routeBtn.setTag(R.id.routeColour2, mRoute_colour2.get(i));
                    routeBtn.setTag(R.id.routeId, mRoute_id.get(i));
                    routeBtn.setOnClickListener(testGym3Activity.this);
                    route_RelativeLayout.addView(routeBtn, params);
                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void updateTries(final String tries, final String gym, final String zone,
                             final String colour){


        // set tries to be new value
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String currentUser = mAuth.getCurrentUser().getUid();
                // if current tries is 0
                if (boulder_progress.getText() == "0") {

                    myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                            .child(currentUser)
                            .child(gym)
                            .child(zone)
                            .child(colour)
                            .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                    +(Integer.toString(selected_boulder))
                                    +(testGym3Activity.this.
                                    getString(R.string.dbname_boulder_tries2)))
                            .setValue(Integer.parseInt(tries)); // set db value to 2
                }
                else {
                    //current tries is not 0, add
                    int curr_tries = Integer.parseInt(boulder_progress.getText().toString()); // 3
                    int new_tries = Integer.parseInt(tries) + curr_tries; // 2+3 = 5

                    myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                            .child(currentUser)
                            .child(gym)
                            .child(zone)
                            .child(colour)
                            .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                    +(Integer.toString(selected_boulder))
                                    +(testGym3Activity.this.
                                    getString(R.string.dbname_boulder_tries2)))
                            .setValue(new_tries); // 5
                }
                // check if true by checking if not done
                if (boulder_completed.getText() == getResources().getString(R.string.tries_not_done)){

                    myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                            .child(currentUser)
                            .child(gym)
                            .child(zone)
                            .child(colour)
                            .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                    +(Integer.toString(selected_boulder))+(testGym3Activity.this.getString(R.string.dbname_boulder_status2))).setValue(false);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //For when user info in firebase needs to be updated
    private void updateRoute(final String tries, final String points, final String gym, final String zone,
                             final String colour){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int curr_tries = Integer.parseInt(boulder_progress.getText().toString());
                int new_tries = Integer.parseInt(tries) + curr_tries; //5

                FirebaseUser currentUser = mAuth.getCurrentUser();

                myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder))+(testGym3Activity.this.getString(R.string.dbname_boulder_tries2))).setValue(new_tries);

                myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder))+(testGym3Activity.this.getString(R.string.dbname_boulder_status2))).setValue(true);

                // get the users current points
                int curr_points = Integer.parseInt(dataSnapshot.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid()).child(testGym3Activity.this
                                .getString(R.string.dbname_boulder_points)).getValue().toString()) ;
                //.setValue(Integer.parseInt(points));
                int new_points = 0;

                // if tries are 3 or less
                if (new_tries <= 3){
                    new_points = curr_points + Integer.parseInt(points);
                    Toast.makeText(testGym3Activity.this, "Points earned: " + Integer.parseInt(points), Toast.LENGTH_LONG).show();
                }
                // else if tries are between 6 and 3 including 6
                else if (new_tries <= 6) {
                    new_points = curr_points + (Integer.parseInt(points)-5);
                    Toast.makeText(testGym3Activity.this, "Points earned: " + (Integer.parseInt(points)-5), Toast.LENGTH_LONG).show();
                }
                // else if tries are greater than 6
                else {
                    new_points = curr_points + (Integer.parseInt(points)-10);
                    Toast.makeText(testGym3Activity.this, "Points earned: " + (Integer.parseInt(points)-10), Toast.LENGTH_LONG).show();
                }

                // get the boulder points and set it to user points
                myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_boulder_points))
                        .setValue(new_points);

                // get the boulder points and update users leader-boards
                myRef.child(testGym3Activity.this.getString(R.string.dbname_leaderboards))
                        .child(gym)
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_boulder_points))
                        .setValue(new_points);

                // increment the amount of total climbs for this user
                int curr_climbs = Integer.parseInt(dataSnapshot.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_climbs)).getValue().toString()) ;
                int new_climbs = curr_climbs + 1;

                // update the amount of total climbs for this user
                myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_climbs))
                        .setValue(new_climbs);

                // get the total climbs and update users leader-boards
                myRef.child(testGym3Activity.this.getString(R.string.dbname_leaderboards))
                        .child(gym)
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_climbs))
                        .setValue(new_climbs);

                // calculate the amount of total tries for this user
                int total_curr_tries = Integer.parseInt(dataSnapshot.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_tries)).getValue().toString()) ;
                int total_new_tries = total_curr_tries + new_tries;

                // update the amount of total tries for this user
                myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_tries))
                        .setValue(total_new_tries);

                // set the average tries for this user
                myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_avgtries))
                        .setValue(total_new_tries/new_climbs);

                // get the average tries and update the users leader-boards
                myRef.child(testGym3Activity.this.getString(R.string.dbname_leaderboards))
                        .child(gym)
                        .child(currentUser.getUid())
                        .child(testGym3Activity.this
                                .getString(R.string.dbname_avgtries))
                        .setValue(total_new_tries/new_climbs);

                // first check if climbs are greater than 3
                // then compare this grade (use points) with database current grade
                // if null then replace (1st grade for user)
                // if less than database current grade do nothing
                // if greater than database current grade update
                if (new_climbs >= 3) {
                    String curr_grade = dataSnapshot.child(testGym3Activity.this.getString(R.string.dbname_users))
                            .child(currentUser.getUid())
                            .child(testGym3Activity.this
                                    .getString(R.string.dbname_grade)).getValue().toString() ;
                    Integer curr_grade_points = Integer.parseInt(calculateFlashPoints(curr_grade));
                    Integer new_grade_points = Integer.parseInt(points);

                    if (curr_grade_points >= new_grade_points){
                        //do nothing
                    }
                    else if (new_grade_points == 80) {
                        //do nothing
                    }
                    else {

                        // update the database with grade of this climb
                        myRef.child(testGym3Activity.this.getString(R.string.dbname_users))
                                .child(currentUser.getUid())
                                .child(testGym3Activity.this
                                        .getString(R.string.dbname_grade))
                                .setValue(boulder_grade.getText());

                        // get the grade and update the users leaderboard
                        myRef.child(testGym3Activity.this.getString(R.string.dbname_leaderboards))
                                .child(gym)
                                .child(currentUser.getUid())
                                .child(testGym3Activity.this
                                        .getString(R.string.dbname_grade))
                                .setValue(boulder_grade.getText());
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateAverageTries(final String tries, final String gym, final String zone,
                                    final String colour) {

        Log.d(TAG, "pathtest: " + tries);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //the user is updating the amount of tries:

                FirebaseUser currentUser = mAuth.getCurrentUser();

                // get the average tries and include amount of tries for you
                int curr_sum_tries = Integer.parseInt(dataSnapshot.child("gym")
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder)))
                        .child("sum_tries").getValue().toString());

                int curr_sum_users = Integer.parseInt(dataSnapshot.child("gym")
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder)))
                        .child("sum_users").getValue().toString());

                int user_prev_tries = Integer.parseInt(boulder_progress.getText().toString());
                Log.d(TAG, "onDataChange: CURR SUM TRIES: " + curr_sum_tries);
                int user_curr_tries = user_prev_tries + Integer.parseInt(tries); // 2 + 1 = 3
                int new_sum_tries = user_curr_tries + curr_sum_tries; // 3 + 5 = 8
                int new_sum_users = curr_sum_users + 1;

                // update the avg_tries for this boulder in database
                myRef.child("gym")
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder)))
                        .child("avg_tries").setValue((float) (new_sum_tries/new_sum_users));

                myRef.child("gym")
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder)))
                        .child("sum_tries").setValue(new_sum_tries);

                myRef.child("gym")
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child((testGym3Activity.this.getString(R.string.dbname_boulder_tries1))
                                +(Integer.toString(selected_boulder)))
                        .child("sum_users").setValue(new_sum_users);


                boulder_avgTries.setText(Float.toString(new_sum_tries/new_sum_users));



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private double convertDptoPx(double dp) {

        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    private double convertPxtoDp(double px){
        return (px / Resources.getSystem().getDisplayMetrics().density);
    }


    @Override
    public void onClick(View v) {
        colour = (String) v.getTag(R.id.routeColour);
        String id = (String) v.getTag(R.id.routeId);
        String colour1 = (String) v.getTag(R.id.routeColour2);
        boulder_class.setTextColor(Color.parseColor(colour1));
        boulder_grade.setTextColor(Color.parseColor(colour1));
        boulder_points.setTextColor(Color.parseColor(colour1));
        // Set class, grade, and points to this colour
        final String seperateId[] = id.split("r");
        selected_boulder = Integer.parseInt(seperateId[1]);
        // Toast.makeText(this, colour + " " + id, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onClick: test: " + seperateId[1]);
        // completeBoulder.setVisibility(View.VISIBLE);
        completeBoulder.setVisibility(View.VISIBLE);
        updateTries.setVisibility(View.VISIBLE);
        updateGrade.setVisibility(View.VISIBLE);
        // get the boulder stats
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // retrieve user data from the database
                getBoulderData(dataSnapshot, selected_boulder, gym, "zone_" + zone, colour);
                getUserData(dataSnapshot, selected_boulder, gym, "zone_" + zone, colour);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData(DataSnapshot dataSnapshot, int j, String gym, String zone,
                             String colour) {
        Log.d(TAG, "getUserData: retreiving user data for selected boulder");
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if ((dataSnapshot.child("users").child(currentUser.getUid())
                .child(gym)
                .child(zone)
                .child(colour)
                .child("boulder" + j + "_status")).exists()) {

            if ((dataSnapshot.child("users").child(currentUser.getUid())
                    .child(gym)
                    .child(zone)
                    .child(colour)
                    .child("boulder" + j + "_status")).getValue().equals(true)) {
                // set text of status to "DONE"
                boulder_completed.setText(testGym3Activity.this.getString(R.string.tries_done));
                boulder_completed.setTextColor(testGym3Activity.this.getResources().getColor(R.color.colorGreen));
                completeBoulder.setText("TOPPED OUT");
                boulder_progress.setText((dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child("boulder" + j + "_tries")).getValue().toString());
                Log.d(TAG, "getUserData: TESTEST: " + (dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child("boulder" + j + "_tries")).getValue().toString());

        } else {
                // set text of status to "NOT DONE"
                boulder_completed.setText(testGym3Activity.this.getString(R.string.tries_not_done));
                boulder_completed.setTextColor(testGym3Activity.this.getResources().getColor(R.color.colorRed));
                completeBoulder.setText("PRESS TO TOP OUT!");
                boulder_progress.setText((dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child("boulder" + j + "_tries")).getValue().toString());
                Log.d(TAG, "getUserData: TESTEST: " + (dataSnapshot.child("users")
                        .child(currentUser.getUid())
                        .child(gym)
                        .child(zone)
                        .child(colour)
                        .child("boulder" + j + "_tries")).getValue().toString());
            }


        } else {
            // set text of status to "NOT DONE"
            boulder_completed.setText(testGym3Activity.this.getString(R.string.tries_not_done));
            boulder_completed.setTextColor(testGym3Activity.this.getResources().getColor(R.color.colorRed));
            completeBoulder.setText("PRESS TO TOP OUT!");
            boulder_progress.setText("0");
        }
    }

    // Retrieves the boulder data for the selected boulder, from firebase database
    // Database: boulders node
    private void getBoulderData(DataSnapshot dataSnapshot, int j, String gym, String zone,
                                String colour){

        // set the boulder class
        boulder_class.setText(dataSnapshot.child("gym")
                .child(gym)
                .child(zone)
                .child(colour)
                .child("boulder" + j)
                .child("classs")
                .getValue().toString());
        // set the text colour based on class
        Log.d(TAG, "preSetTextColor");
        // setTextColor(boulder_class.getText().toString());

        // set the boulder grade
        boulder_grade.setText(dataSnapshot.child("gym")
                .child(gym)
                .child(zone)
                .child(colour)
                .child("boulder" + j)
                .child("grade")
                .getValue().toString());

        // calculate flash points with boulder grade
        Log.d(TAG, "preCalculateFlashPoints");
        String points = calculateFlashPoints(boulder_grade.getText().toString());
        boulder_points.setText(points);

        // set the average tries

        boulder_avgTries.setText(dataSnapshot.child("gym")
                .child(gym)
                .child(zone)
                .child(colour)
                .child("boulder" + j)
                .child("avg_tries")
                .getValue().toString());

    }

    private String calculateFlashPoints(String grade) {
        Log.d(TAG, "calculateFlashPoints: starting");
        String[] grades = {"4a", "4b", "4c", "5a", "5b", "5c", "6a", "6a+", "6b", "6b+", "6c", "6c+",
                "7a", "7a+", "7b", "7b+", "7c", "7c+", "8a", "8a+", "8b", "8b+", "8c", "8c+", "9a", "9a+",
                "9b", "9b+", "9c", "9c+", "?"};
        int len = grades.length;
        int pos = 0;
        String[] points = {"42", "45", "47", "52", "55", "57", "62", "64", "65", "66",
                "67", "69", "72", "74", "75", "76", "77", "79", "82", "84", "85", "86", "87",
                "89", "92", "94", "95", "96", "97", "99", "80"};

        // for i search through grades for grade, when when found return i. Then return points[i]
        // from function
        for (int i = 0; i <= (len-1); i++) {
            if (grades[i].equals(grade)) {
                Log.d(TAG, "calculateFlashPoints: grade = " + grades[i]);
                pos = i;

            }
        }
        Log.d(TAG, "calculateFlashPoints: returning points: " + points[pos]);
        return points[pos];
    }

    // ------------------------------------ FIREBASE -----------------------------------------------

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
                if(user != null) {
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
