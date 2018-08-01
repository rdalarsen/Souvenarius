package me.worric.souvenarius.ui.add;

import android.text.TextUtils;

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

}
