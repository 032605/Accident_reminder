package com.example.test;

public class AlertDTO {
    private String UID;
    private double latitude;
    private double longitude;
    private String time;
    private String comment;

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public AlertDTO(String UID, double latitude, double longitude, String time, String comment) {
        this.UID = UID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.comment = comment;
    }
    public AlertDTO()
    {

    }
}
