package com.clymbra.clymbralpha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class trainRecyclerViewAdapter extends RecyclerView.Adapter<trainRecyclerViewAdapter.trainViewHolder> {
    private ArrayList<Exercises> mExercises;

    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onAddClick(int i);
        void onDeleteClick(int i);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class trainViewHolder extends RecyclerView.ViewHolder {

        public EditText mDesc;
        public EditText mSets;
        public EditText mReps;
        public ImageView mAdd;
        public ImageView mRemove;


        public trainViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mDesc = itemView.findViewById(R.id.exercise_Edittext);
            mSets = itemView.findViewById(R.id.sets_Edittext);
            mReps = itemView.findViewById(R.id.reps_Edittext);
            mAdd = itemView.findViewById(R.id.add_Button);
            mRemove = itemView.findViewById(R.id.remove_Button);

            mAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int i = getAdapterPosition();
                        if (i != RecyclerView.NO_POSITION) {
                            listener.onAddClick(i);
                        }
                    }
                }
            });

            mRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int i = getAdapterPosition();
                        if (i != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(i);
                        }
                    }
                }
            });
        }
    }

    public trainRecyclerViewAdapter(ArrayList<Exercises> exercises) {
        mExercises = exercises;
    }



    @NonNull
    @Override
    public trainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapterview_train, viewGroup, false);
        trainViewHolder trainViewHolder = new trainViewHolder(v, mListener);
        return  trainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull trainViewHolder trainViewHolder, int i) {
        Exercises currentExercise = mExercises.get(i);
        if (currentExercise.getDescription().equals("")){
            trainViewHolder.mDesc.setHint("Enter Exercise: ");
            trainViewHolder.mSets.setHint("0");
            trainViewHolder.mReps.setHint("0");
        }else {
            trainViewHolder.mDesc.setText(currentExercise.getDescription());
            trainViewHolder.mSets.setText(currentExercise.getSets());
            trainViewHolder.mReps.setText(currentExercise.getReps());
        }

    }

    @Override
    public int getItemCount() {
        return mExercises.size();
    }
}

