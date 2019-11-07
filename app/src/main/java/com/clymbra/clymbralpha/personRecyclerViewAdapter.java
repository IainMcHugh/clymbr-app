package com.clymbra.clymbralpha;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class personRecyclerViewAdapter extends RecyclerView.Adapter<personRecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "personRecyclerViewAdapt";

    private ArrayList<String> mImage = new ArrayList<>();
    private ArrayList<String> mRank = new ArrayList<>();
    private ArrayList<String> mUsername = new ArrayList<>();
    private ArrayList<String> mUid = new ArrayList<>();
    private ArrayList<String> mPoints = new ArrayList<>();

    private OnUserListener mOnUserListener;

    public personRecyclerViewAdapter(ArrayList<String> mImage,
                                     ArrayList<String> mRank,
                                     ArrayList<String> mUsername,
                                     ArrayList<String> mUid,
                                     ArrayList<String> mPoints,
                                     OnUserListener onUserListener) {
        this.mImage = mImage;
        this.mRank = mRank;
        this.mUsername = mUsername;
        this.mUid = mUid;
        this.mPoints = mPoints;


        this.mOnUserListener = onUserListener;

        //Log.d(TAG, "personRecyclerViewAdapter: mUsername = " + mUsername);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapterview_person, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, mOnUserListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called");
        Picasso.get().load(mImage.get(i)).into(viewHolder.image);
        viewHolder.rank.setText(mRank.get(i));
        viewHolder.username.setText(mUsername.get(i));
        viewHolder.points.setText(mPoints.get(i));
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + mUsername.size());
        return mUsername.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image;
        TextView rank, username, points;

        OnUserListener onUserListener;

        public ViewHolder(@NonNull View itemView, OnUserListener onUserListener) {
            super(itemView);

            image = itemView.findViewById(R.id.userimage_Imageview_adapter);
            rank = itemView.findViewById(R.id.rank_textview_adapter);
            username = itemView.findViewById(R.id.username_textview_adapter);
            points = itemView.findViewById(R.id.points_textview_adapter);

            this.onUserListener = onUserListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserListener.onUserClick(getAdapterPosition());
        }
    }

    public interface OnUserListener{
        void onUserClick(int i);
    }
}

