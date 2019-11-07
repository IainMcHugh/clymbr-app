package com.clymbra.clymbralpha;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


public class ProfileSettingsFragment extends Fragment {

    private static final String TAG = "ProfileSettingsFragment";

    private static final int PICK_IMAGE_REQUEST = 1;

    //widget declarations
    private EditText username, bio, email;
    private Spinner gym_spinner;

    private Button changeProfileImage_Button;
    private ImageView profile_ImageView;
    private Uri mImageUri;

    private String active_gym;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> mGyms;

    // Firebase declarations
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference storageReference;


    public ProfileSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "FRAGMENT TEST ###############################");
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);


        Button signout = view.findViewById(R.id.signout_Button);
        Button updatesettings = view.findViewById(R.id.updatesettings_Button);
        changeProfileImage_Button = view.findViewById(R.id.changeProfileImage_Button);
        profile_ImageView = view.findViewById(R.id.settingsProfile_Imageview);
        ImageButton goBack = view.findViewById(R.id.goBack_imageButton);

        username = view.findViewById(R.id.settingsUsername_Edittext);
        bio = view.findViewById(R.id.settingsBio_Edittext);
        email = view.findViewById(R.id.settingsEmail_Edittext);
        gym_spinner = view.findViewById(R.id.activeGym_Spinner);

        storageReference = FirebaseStorage.getInstance().getReference("profile_photos");


        mGyms = new ArrayList<>();

        setupFirebaseAuth();


        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to sign out############################");
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        populateSpinner();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                populateUserFields(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gym_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selection = parent.getSelectedItem().toString();
                if (selection.equals("Add a new Gym")) {
                    Toast.makeText(getContext(), "ADD A NEW GYM", Toast.LENGTH_SHORT).show();
                    // IN HERE GO TO ADD NEW GYM FRAGMENTS WHERE
                    Intent intent = new Intent(getContext(), testGymActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        updatesettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update the user settings if they have been changed
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        updateUserData(dataSnapshot);
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        changeProfileImage_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open photos to choose a profile photo, save image to Firebase Storage
                // add url to user database branch
                openFileChooser();
            }
        });

        return view;
    }

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(profile_ImageView);
            uploadFile();
        }
    }

    // get file extension eg ".jpg"
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFile() {

        if (mImageUri != null) {
            final StorageReference fileReference = storageReference.child(mAuth.getCurrentUser().getUid()
                    + "." + getFileExtension(mImageUri));

            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // upload was success
                            Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Upload upload = new Upload(uri.toString());
                                    myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("profile_url").setValue(upload.getmImageUrl());

                                    // for each gym in available_gyms, go to leaderboards/uid/profile_url:upload.getImageUrl()
                                    for (int i=0;i<(mGyms.size())-1;i++){
                                        myRef.child("leaderboards")
                                                .child(mGyms.get(i))
                                                .child(mAuth.getCurrentUser().getUid()).child("profile_url").setValue(upload.getmImageUrl());
                                    }

                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // failure in uploading image to storage
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // during upload "progress bar"
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }


    }

    // This gets user data from firebase and populates the edit text fields with this info
    private void populateUserFields(DataSnapshot dataSnapshot) {

        Log.d(TAG, "populateUserFields: retrieving user information");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            if(ds.getKey().equals("users")) {
                Log.d(TAG, "Users-DataSnapshot: " + ds);
                for (DataSnapshot ds2: ds.getChildren()) {
                    if (ds2.getKey().matches(currentUser.getUid())){
                        Log.d(TAG, "DataSnapshot2: " + ds2);
                        Log.d(TAG, "user name: " + ds2.getKey());
                        try {

                            // get the profile picture
                            String profile_url = ds2.child("profile_url").getValue().toString();
                            Picasso.get().load(profile_url).into(profile_ImageView);

                            // Log.d(TAG, "username: " + ds2.child("username").getValue().toString());
                            username.setText((ds2.child("username").getValue().toString()), TextView.BufferType.EDITABLE);

                            // Log.d(TAG, "bio: " + ds2.child("bio").getValue().toString());
                            bio.setText((ds2.child("bio").getValue()).toString(), TextView.BufferType.EDITABLE);

                            // Log.d(TAG, "gym: " + ds2.child("local_gym").getValue().toString());
                            // gym_spinner.setText((ds2.child("active_gym").getValue()).toString());
                            // This set Selection has to be pos of active gym
                            setSpinText(gym_spinner, (ds2.child("active_gym").getValue()).toString());

                            active_gym = ds2.child("active_gym").getValue().toString();

                            // Log.d(TAG, "email: " + ds2.child(getContext().getString(R.string.dbname_email)).getValue());
                            email.setText((ds2.child("email").getValue()).toString(), TextView.BufferType.EDITABLE);

                        } catch (NullPointerException e) {
                            Log.e(TAG, "NullPointerException: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void populateSpinner() {

        String currentUser = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("users").child(currentUser).child("available_gyms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: ds: " + ds);
                    String gym = ds.getValue().toString();
                    mGyms.add(gym);


                }
                mGyms.add("Add a new Gym");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mGyms);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                gym_spinner.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setSpinText(Spinner spin, String text)
    {
        for(int i= 0; i < spin.getAdapter().getCount(); i++)
        {
            if(spin.getAdapter().getItem(i).toString().contains(text))
            {
                spin.setSelection(i);
            }
        }

    }

    // Will update the user information if there is any updates to be made
    private void updateUserData(DataSnapshot dataSnapshot) {

        String nUsername = username.getText().toString();
        String nBio = bio.getText().toString();
        String nActive_gym = gym_spinner.getSelectedItem().toString();
        String nEmail = email.getText().toString();

        Log.d(TAG, "populateUserFields: retrieving user information");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            if(ds.getKey().equals(getString(R.string.dbname_users))) {
                Log.d(TAG, "Users-DataSnapshot: " + ds);
                for (DataSnapshot ds2: ds.getChildren()) {
                    if (ds2.getKey().matches(currentUser.getUid())){
                        Log.d(TAG, "DataSnapshot2: " + ds2);
                        Log.d(TAG, "user name: " + ds2.getKey());
                        try {
                            //if username has changed
                            if (!nUsername.equals((ds2.child(getContext()
                                    .getString(R.string.dbname_username))
                                    .getValue()).toString())) {
                                // update database
                                myRef.child(getContext().getString(R.string.dbname_users))
                                        .child(currentUser.getUid())
                                        .child((getContext().getString(R.string.dbname_username))).setValue(nUsername);
                            };
                            //if bio has changed
                            if (!nBio.equals((ds2.child(getContext()
                                    .getString(R.string.dbname_bio))
                                    .getValue()).toString())) {
                                // update database
                                myRef.child(getContext().getString(R.string.dbname_users))
                                        .child(currentUser.getUid())
                                        .child((getContext().getString(R.string.dbname_bio))).setValue(nBio);
                                // update database - leaderboard
                                myRef.child(getContext().getString(R.string.dbname_leaderboards))
                                        .child(gym_spinner.getSelectedItem().toString())
                                        .child(currentUser.getUid())
                                        .child((getContext().getString(R.string.dbname_bio))).setValue(nBio);
                            };
                            //if active gym has changed
                            if (!nActive_gym.equals((ds2.child(getContext()
                                    .getString(R.string.dbname_activegym))
                                    .getValue()).toString())) {
                                // update database
                                myRef.child(getContext().getString(R.string.dbname_users))
                                        .child(currentUser.getUid().toString())
                                        .child((getContext().getString(R.string.dbname_activegym))).setValue(nActive_gym);
                            };
                            //if email has changed
                            if (!nEmail.equals((ds2.child(getContext().getString(R.string.dbname_email)).getValue()).toString())) {
                                // update database
                                myRef.child(getContext().getString(R.string.dbname_users))
                                        .child(currentUser.getUid().toString())
                                        .child((getContext().getString(R.string.dbname_email))).setValue(nEmail);
                            };

                        } catch (NullPointerException e) {
                            Log.e(TAG, "NullPointerException: " + e.getMessage());
                        }
                    }
                }
            }
        }
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
                    Intent intent2 = new Intent(getActivity(), MainActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent2);
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
