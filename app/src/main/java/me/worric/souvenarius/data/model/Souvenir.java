package me.worric.souvenarius.data.model;

import com.google.firebase.database.Exclude;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.ArrayList;
import java.util.List;

public class Souvenir {

    private long id;
    private List<String> mPhotos;
    private long mTimestamp;
    private String mPlace;
    private String mTitle;
    private String mStory;

    public Souvenir() {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void addPhoto(String image) {
        getPhotos().add(image);
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

    @Exclude
    public String getFormattedTimestamp() {
        return ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(getTimestamp()), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    @Exclude
    public String getFirstPhoto() {
        return (getPhotos().size() > 0 ? getPhotos().get(0) : null );
    }

}
