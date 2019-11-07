package com.clymbra.clymbralpha;

public class Exercises {

    private String description;
    private String sets;
    private String reps;

    public Exercises() {
    }

    public Exercises(String description, String reps, String sets){

        this.description = description;
        this.reps = reps;
        this.sets = sets;
    }

    public String getDescription() { return description; }

    public String getReps() { return reps; }

    public String getSets() { return sets; }



}