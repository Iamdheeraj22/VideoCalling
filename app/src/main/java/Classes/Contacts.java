package Classes;

public class Contacts
{
    private String username;
    private String imageurl;
    private String about;
    private String id;

    public Contacts() {
    }

    public Contacts(String username, String imageurl, String about, String id) {
        this.username = username;
        this.imageurl = imageurl;
        this.about = about;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
