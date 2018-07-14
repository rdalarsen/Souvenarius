package me.worric.souvenarius.ui.add;

import android.text.TextUtils;

import org.threeten.bp.Instant;

import java.io.File;

import me.worric.souvenarius.data.model.Souvenir;

public final class SouvenirSaveInfo {

    private String mStory;
    private String mTitle;
    private String mPlace;

    public SouvenirSaveInfo(String story, String title, String place) {
        mStory = story;
        mTitle = title;
        mPlace = place;
    }

    public boolean hasMissingValues() {
        return TextUtils.isEmpty(mStory) || TextUtils.isEmpty(mTitle) || TextUtils.isEmpty(mPlace);
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        mPlace = place;
    }

    public String getStory() {
        return mStory;
    }

    public void setStory(String story) {
        mStory = story;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Souvenir toSouvenir(File photo) {
        Souvenir souvenir = new Souvenir();
        souvenir.setPlace(mPlace);
        souvenir.setTimestamp(Instant.now().toEpochMilli());
        souvenir.setTitle(mTitle);
        souvenir.setStory(mStory);
        if (photo != null) {
            souvenir.addPhoto(photo.getName());
        }
        return souvenir;
    }
}
