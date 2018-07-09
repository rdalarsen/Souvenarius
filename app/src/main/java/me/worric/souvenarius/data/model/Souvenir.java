package me.worric.souvenarius.data.model;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.ArrayList;
import java.util.List;

public class Souvenir {

    private long id;
    private List<String> mImages = null;
    private long mTimestamp;
    private String mPlace;
    private String mTitle;

    public Souvenir() {
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

    public List<String> getImages() {
        return mImages == null ? new ArrayList<>() : mImages;
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

    public String getFormattedTimestamp() {
        return ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(getTimestamp()), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public String getFirstPhoto() {
        return (getImages().size() > 0 ? getImages().get(0) : null );
    }

}
