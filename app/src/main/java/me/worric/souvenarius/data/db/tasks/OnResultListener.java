package me.worric.souvenarius.data.db.tasks;

public interface OnResultListener<T> {

    void onSuccess(T t);

    void onFailure();

}
