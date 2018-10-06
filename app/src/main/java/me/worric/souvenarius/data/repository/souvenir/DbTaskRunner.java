package me.worric.souvenarius.data.repository.souvenir;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.tasks.OnResultListener;
import me.worric.souvenarius.data.model.SouvenirDb;

public interface DbTaskRunner {

    void runInsertTask(SouvenirDb souvenirDb,
                       AppDatabase appDatabase,
                       OnResultListener<SouvenirDb> listener);

    void runUpdateTask(SouvenirDb souvenir,
                       AppDatabase appDatabase,
                       OnResultListener<SouvenirDb> listener);

    void runDeleteTask(SouvenirDb souvenir,
                       AppDatabase appDatabase,
                       OnResultListener<SouvenirDb> listener);

}
