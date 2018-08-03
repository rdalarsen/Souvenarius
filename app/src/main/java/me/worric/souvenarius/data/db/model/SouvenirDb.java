package me.worric.souvenarius.data.db.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.ArrayList;
import java.util.List;

import me.worric.souvenarius.ui.detail.DetailFragment;

@Entity(tableName = "souvenirs")
public class SouvenirDb {

    @Exclude
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String mId;

    @ColumnInfo(name = "timestamp")
    private long mTimestamp;

    @ColumnInfo(name = "place")
    private String mPlace;

    @ColumnInfo(name = "story")
    private String mStory;

    @ColumnInfo(name = "title")
    private String mTitle;

    @ColumnInfo(name = "photos")
    private List<String> mPhotos;

    @ColumnInfo(name = "uid")
    private String mUid;

    public SouvenirDb() {
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
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

    @NonNull
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

    @Exclude
    public String getValueFromTextType(@NonNull DetailFragment.TextType textType) {
        switch (textType) {
            case STORY:
                return getStory();
            case PLACE:
                return getPlace();
            case TITLE:
                return getTitle();
            default:
                throw new IllegalArgumentException("Unknown text type: " + textType.name());
        }
    }

}
