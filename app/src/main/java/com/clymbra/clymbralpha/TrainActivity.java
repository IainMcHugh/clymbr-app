package com.clymbra.clymbralpha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "TrainActivity";

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private RecyclerView mRecyclerView;
    private trainRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<Exercises> exampleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        //---------------------------INITIALIZE NAV BAR---------------------------------------------
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_view);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.nav_gym :
                        Intent intent1 = new Intent(TrainActivity.this, testGym2Activity.class);
                        startActivity(intent1);
                        break;

                    case R.id.nav_stats :
                        Intent intent2 = new Intent(TrainActivity.this, StatsActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.nav_train :

                        break;

                    case R.id.nav_profile:
                        Intent intent4 = new Intent(TrainActivity.this, ProfileActivity.class);
                        startActivity(intent4);
                        break;

                }

                return false;
            }
        });


        //------------------------------------------------------------------------------------------

        // define the Button within the include for expanding the training day module

//        Button fingerTrain = findViewById(R.id.fingerTrain_Button);
//        Button flexibility = findViewById(R.id.flexibility_Button);
//        Button strength = findViewById(R.id.strength_Button);


//        exampleList.add(new Exercises("Pull up", 5, 5));
//        exampleList.add(new Exercises("Push up", 3, 7));
//        exampleList.add(new Exercises("Chin up", 3, 7));

        //go to db and for each child in user/train add the desc, sets, reps

        setupFirebaseAuth();

        getExercises();

        final Button save = (Button) findViewById(R.id.save_Button);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update the database with the edited exercises

                updatedExercises();

            }
        });
    }

    private void updatedExercises() {

        final String currentUser = mAuth.getCurrentUser().getUid();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("users").child(currentUser).child("train").exists()){
                    //clear the train child database
                    myRef.child("users").child(currentUser).child("train").removeValue();
                }

                // add everything in ExampleList to the database
                // for exercise in exercise array { create node[i], key[desc] = exer[i] etc
                Log.d(TAG, "onDataChange: exampleList size: " + exampleList.size());
                for (int i = 0; i < exampleList.size(); i++) {

                    View view = mRecyclerView.getChildAt(i);
                    TextView etdescription = view.findViewById(R.id.exercise_Edittext);
                    TextView etsets = view.findViewById(R.id.sets_Edittext);
                    TextView etreps = view.findViewById(R.id.reps_Edittext);
                    String description = etdescription.getText().toString();
                    String sets = etsets.getText().toString();
                    String reps = etreps.getText().toString();

                    if (description.equals("") && exampleList.size() == 1){
                        break;
                    }else {
                        // String description = exampleList.get(i).getDescription();
                        // int sets = exampleList.get(i).getSets();
                        // int reps = exampleList.get(i).getSets();

                        // upload the 3 params into train/i
                        myRef.child("users").child(currentUser).child("train").child("Exercise_" + Integer.toString(i)).child("description").setValue(description);
                        myRef.child("users").child(currentUser).child("train").child("Exercise_" + Integer.toString(i)).child("sets").setValue(sets);
                        myRef.child("users").child(currentUser).child("train").child("Exercise_" + Integer.toString(i)).child("reps").setValue(reps);
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getExercises() {

        Log.d(TAG, "getExercises: gettin Exercises from Database");

        final String currentUser = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("train")){

                    for (DataSnapshot ds: dataSnapshot.child("train").getChildren()){
                        Log.d(TAG, "onDataChange: ds= " + ds);
                        Exercises exercises = ds.getValue(Exercises.class);
                        Log.d(TAG, "onDataChange: desc: " + exercises.getDescription());
                        String desc = exercises.getDescription();
                        Log.d(TAG, "onDataChange: sets: " + exercises.getSets());
                        String sets = exercises.getSets();
                        Log.d(TAG, "onDataChange: reps: " + exercises.getReps());
                        String reps = exercises.getReps();
                        exampleList.add(new Exercises(desc, sets, reps));
                    }

                } else {
                    exampleList.add(new Exercises("", "", ""));
                }

                Log.d(TAG, "onCreate: Example List: " + exampleList);

                mRecyclerView = findViewById(R.id.train_recyclerview);
                // remove this later
                mRecyclerView.setHasFixedSize(true);
                mLayoutManager = new LinearLayoutManager(TrainActivity.this);
                mAdapter = new trainRecyclerViewAdapter(exampleList);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnItemClickListener(new trainRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onAddClick(int i) {
                        insertItem(i+1);
                    }

                    @Override
                    public void onDeleteClick(int i) {
                        removeItem(i);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void insertItem(int position) {
        exampleList.add(position, new Exercises("", "", ""));
        mAdapter.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        //if there is greater than 1 left
        if (exampleList.size() > 1){
            exampleList.remove(position);
            mAdapter.notifyItemRemoved(position);
        }
    }


    //---------------------------------FIREBASE-----------------------------------------------------

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
