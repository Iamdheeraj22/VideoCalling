package com.example.videocalling.Classes;

public class Contacts
{
    private String firstname;
    private String lastname;
    private String imageurl;
    private String about;
    private String id;

    public Contacts() {
    }

    public Contacts(String firstname,String lastname, String imageurl, String about, String id) {
        this.firstname = firstname;
        this.lastname=lastname;
        this.imageurl = imageurl;
        this.about = about;
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
