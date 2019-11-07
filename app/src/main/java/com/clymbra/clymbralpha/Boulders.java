package com.clymbra.clymbralpha;

public class Boulders {

    private String bname, bgrade;

    public Boulders(){

    }

    public Boulders(String bname, String bgrade) {
        this.bname = bname;
        this.bgrade = bgrade;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) { this.bname = bname; }

    public String getBgrade() {
        return bgrade;
    }

    public void setBgrade(String bgrade) { this.bgrade = bgrade; }

    @Override
    public String toString(){
        return "Boulder{" +
                "name='" + bname + '\'' +
                ", grade'" + bgrade + '\'' +
                '}';
    }

}