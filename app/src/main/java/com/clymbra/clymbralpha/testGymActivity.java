package com.clymbra.clymbralpha;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class testGymActivity extends AppCompatActivity implements gymRecyclerViewAdapter.OnItemClickListener{

    private static final String TAG = "testGymActivity";

    private ProfileSettingsFragment profileSettingsFragment;

    private RecyclerView mRecyclerView;
    private gymRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> mGym_logo_url;
    private ArrayList<String> mGym_id;

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gym);

        ImageButton goBack = findViewById(R.id.goBack_imageButton);
        profileSettingsFragment = new ProfileSettingsFragment();

        mGym_logo_url = new ArrayList<>();
        mGym_id = new ArrayList<>();

        setupFirebaseAuth();

        getGymLogos();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fr = getSupportFragmentManager();
                fr.beginTransaction().replace(R.id.layout_testGym, profileSettingsFragment).commit();
            }
        });
    }

    private void getGymLogos() {

        FirebaseDatabase.getInstance().getReference().child("gym").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: DS KEY: " + ds.getKey());
                    mGym_id.add(ds.getKey());
                    for (DataSnapshot ds2: ds.getChildren()){
                        Log.d(TAG, "onDataChange: ds2: " + ds2);
                        if (ds2.getKey().equals("logo_url")){
                            String gyms = ds2.getValue().toString();
                            Log.d(TAG, "onDataChange: Logo_url: " + gyms);
                            mGym_logo_url.add(gyms);
                        }
                    }

                    RecyclerView recyclerView = findViewById(R.id.gym_recyclerView);
                    recyclerView.setHasFixedSize(true);
                    gymRecyclerViewAdapter adapter = new gymRecyclerViewAdapter(mGym_logo_url, mGym_id, testGymActivity.this);

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(testGymActivity.this));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemClick(int i){
        Toast.makeText(testGymActivity.this, "Gym Selected: " + mGym_id.get(i), Toast.LENGTH_SHORT).show();
        // start intent to testGym2Activity, with putExtra(mGym_id.get(i)
//        Intent intent = new Intent(testGymActivity.this, testGym2Activity.class).putExtra("gym", mGym_id.get(i));
//        startActivity(intent);
    }

    // FIREBASE-------------------------------------------------------------------------------------

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
