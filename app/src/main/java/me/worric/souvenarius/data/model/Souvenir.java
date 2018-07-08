package me.worric.souvenarius.data.model;

import java.util.ArrayList;
import java.util.List;

public class Souvenir {

    private long id;
    private List<String> mImages = null;
    private long mTimestamp;
    private String mPlace;

    public Souvenir() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getImages() {
        return mImages;
    }

    public void setImages(List<String> images) {
        mImages = images;
    }

    public void addImage(String image) {
        if (mImages == null) mImages = new ArrayList<>();

        mImages.add(image);
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        mPlace = place;
    }

}
