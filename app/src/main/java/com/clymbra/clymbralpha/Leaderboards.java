package com.clymbra.clymbralpha;

public class Leaderboards {

    private int imageResource;

    private String userId, username, bio, grade, profile_url;

    private Integer points, total_climbs, avg_tries;

    public Leaderboards(){
    }

    public Leaderboards(int imageResource, String userId, String username, String bio,
                        Integer points, String grade, Integer total_climbs, Integer avg_tries,
                        String profile_url) {

        this.imageResource = imageResource;
        this.userId = userId;
        this.username = username;
        this.bio = bio;
        this.points = points;
        this.grade = grade;
        this.total_climbs = total_climbs;
        this.avg_tries = avg_tries;
        this.profile_url = profile_url;
    }

    public int getImageResource() {return imageResource; }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {return bio; }

    public Integer getPoints() { return points; }

    public String getGrade() { return grade; }

    public Integer getTotal_climbs() { return  total_climbs; }

    public Integer getAvg_tries() { return avg_tries; }

    public String getProfile_url() { return profile_url; }


}
