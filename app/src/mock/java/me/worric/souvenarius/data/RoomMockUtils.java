package me.worric.souvenarius.data;

import android.content.ContentValues;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import androidx.annotation.NonNull;
import androidx.room.OnConflictStrategy;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class RoomMockUtils {

    private RoomMockUtils() {}

    private static final String MY_ID = "myId";
    private static final String UID = "HFnz2Pc627Qv06f5RVso6Y8QAcq1";
    private static final String TABLE = "souvenirs";
    public static final String MY_PLACE = "myPlace";
    public static final String MY_TITLE = "myTitle";
    public static final String MY_STORY = "myStory";
    public static final String NUM_PHOTOS = "Photos: 2";
    private static final String PHOTOS = "JPEG_20181010_121653_499228986664047607.jpg" +
            ":" +
            "JPEG_20181010_121724_3628200275035515048.jpg";
    private static final long TIMESTAMP = 1534459136474L;
    public static final String FORMATTED_DATE = ZonedDateTime
            .ofInstant(Instant.ofEpochMilli(TIMESTAMP), ZoneId.systemDefault())
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));

    public static RoomDatabase.Callback sCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            cv.put("id", MY_ID);
            cv.put("place", MY_PLACE);
            cv.put("title", MY_TITLE);
            cv.put("story", MY_STORY);
            cv.put("timestamp", TIMESTAMP);
            cv.put("uid", UID);
            cv.put("photos", PHOTOS);
            try {
                db.beginTransaction();
                db.insert(TABLE, OnConflictStrategy.REPLACE, cv);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    };

}
