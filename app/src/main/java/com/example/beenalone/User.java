package com.example.beenalone;

public class User {
    private enum Gender {
        MALE, FEMALE
    }
    private enum RelationshipStat{
        ALONE, IN_LOVE, FREE
    }

    private String email = "";
    private String name = "";
    private String gender = "";
    private int avatar ;

    public User(String email, String name, String gender, int avatar) {
        this.email = email;
        this.name = name;
        this.gender = gender;
//        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public int getAvatar() {
        return avatar;
    }
}
