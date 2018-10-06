package me.worric.souvenarius.data.repository.souvenir;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import me.worric.souvenarius.ui.main.SortStyle;

public final class QueryParameters {

    private String mUid;
    private SortStyle mSortStyle;

    QueryParameters(@Nullable String uid, @NonNull SortStyle sortStyle) {
        this.mUid = uid;
        this.mSortStyle = sortStyle;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public SortStyle getSortStyle() {
        return mSortStyle;
    }

    public void setSortStyle(SortStyle sortStyle) {
        this.mSortStyle = sortStyle;
    }

}
