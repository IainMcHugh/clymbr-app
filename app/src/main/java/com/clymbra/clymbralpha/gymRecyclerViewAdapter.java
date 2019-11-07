package com.clymbra.clymbralpha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class gymRecyclerViewAdapter extends RecyclerView.Adapter<gymRecyclerViewAdapter.gymViewHolder> {
    private ArrayList<String> mGyms_id;
    private ArrayList<String> mGyms;
    private OnItemClickListener mListener;

    public gymRecyclerViewAdapter(ArrayList<String> gyms, ArrayList<String> gyms_id, OnItemClickListener listener){
        this.mGyms = gyms;
        this.mGyms_id = gyms_id;
        this.mListener = listener;
    }


    @NonNull
    @Override
    public gymViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapterview_gym, viewGroup, false);
        gymViewHolder viewHolder = new gymViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull gymViewHolder gymViewHolder, int i) {
        // mGyms.get(i) = THE LOGO URL IN FIREBASE STORAGE
        // gymViewHolder.mImageButton.setBackground();
        gymViewHolder.mGymTextView.setText(mGyms_id.get(i));
        // THIS STRING IS A URL LINK TO THE CURRENT GYM LOGO, GET IMAGE FROM FIREBASE STORAGE AND SET AS SOURCE TO THE IMAGE BUTTON
        Picasso.get().load(mGyms.get(i)).fit().into(gymViewHolder.mImageButton);
    }

    @Override
    public int getItemCount() {
        return mGyms.size();
    }

    public static class gymViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageButton mImageButton;
        public TextView mGymTextView;

        OnItemClickListener listener;

        public gymViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            mImageButton = itemView.findViewById(R.id.gym_ImageButton_adapter);
            mGymTextView = itemView.findViewById(R.id.gym_Textview_adapter);
            this.listener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) { listener.onItemClick(getAdapterPosition());

        }
    }



    public interface OnItemClickListener {
        void onItemClick(int i);
    }


}

