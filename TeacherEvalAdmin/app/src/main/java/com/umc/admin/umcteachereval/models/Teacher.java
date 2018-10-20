package com.umc.admin.umcteachereval.models;

public class Teacher {
    private String name;
    private String email;
    private String id;
    private int votes;

    public Teacher(String name, String email) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    @Override
    public String toString() {
        return name + ": " + email;
    }
}
