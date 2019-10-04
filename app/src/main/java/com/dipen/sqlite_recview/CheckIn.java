package com.dipen.sqlite_recview;

public class CheckIn {
    public String title;
    public String place;
    public String details;
    public long date;
    public String location;
    public byte[] image;

    public CheckIn(String title, String place, String details, long date, String location, byte[] image) {
        this.title = title;
        this.place = place;
        this.details = details;
        this.date = date;
        this.location = location;
        this.image = image;
    }

    public CheckIn() {
    }
}
