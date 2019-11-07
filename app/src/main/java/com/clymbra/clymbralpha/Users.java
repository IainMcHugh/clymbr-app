package com.clymbra.clymbralpha;

public class Users {

    private String userId, username, email, bio, local_gym, active_gym, grade, profile_url;

    private Integer points, total_climbs, total_tries, avg_tries, rank;

    public Users(){

    }

    public Users(String userId, String username, String email, String bio, String local_gym,
                 String active_gym, Integer points, String grade, Integer total_climbs,
                 Integer total_tries, Integer avg_tries, Integer rank, String profile_url) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.local_gym = local_gym;
        this.active_gym = active_gym;
        this.points = points;
        this.grade = grade;
        this.total_climbs = total_climbs;
        this.total_tries = total_tries;
        this.avg_tries = avg_tries;
        this.rank = rank;
        this.profile_url = profile_url;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() { return bio; }

    public String getLocal_gym() { return local_gym; }

    public String getActive_gym() { return active_gym; }

    public Integer getPoints() { return points; }

    public String getGrade() { return grade; }

    public Integer getTotal_climbs() {return total_climbs; }

    public Integer getTotal_tries() { return total_tries; }

    public Integer getAvg_tries() { return avg_tries; }

    public Integer getRank() { return rank; }

    public String getProfile_url() { return profile_url; }

}
