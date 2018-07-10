package me.worric.souvenarius.data.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class SouvenirResponse {

    private String mFirebaseId;
    private String mPlace;
    private String mTitle;
    private String mStory;
    private List<String> mPhotos = null;
    private long mTimestamp;

    public SouvenirResponse(String firebaseId, String place, String title, String story, List<String> photos, long timestamp) {
        mFirebaseId = firebaseId;
        mPlace = place;
        mTitle = title;
        mStory = story;
        mPhotos = photos;
        mTimestamp = timestamp;
    }

    public SouvenirResponse() {
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getStory() {
        return mStory;
    }

    public void setStory(String story) {
        mStory = story;
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        mPlace = place;
    }

    public List<String> getPhotos() {
        if (mPhotos == null) {
            mPhotos = new ArrayList<>();
        }
        return mPhotos;
    }

    public void setPhotos(List<String> photos) {
        mPhotos = photos;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public String getFirebaseId() {
        return mFirebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        mFirebaseId = firebaseId;
    }

    public void addPhotoToList(String photoName) {
        getPhotos().add(photoName);
    }

    @Exclude
    public Souvenir toSouvenir() {
        Souvenir souvenir = new Souvenir();
        souvenir.setStory(getStory());
        souvenir.setTimestamp(getTimestamp());
        souvenir.setTitle(getTitle());
        souvenir.setPhotos(getPhotos());
        souvenir.setPlace(getPlace());
        return souvenir;
    }

}
