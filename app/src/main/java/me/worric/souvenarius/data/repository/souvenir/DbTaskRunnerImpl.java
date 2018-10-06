package me.worric.souvenarius.data.repository.souvenir;

import javax.inject.Inject;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.tasks.OnResultListener;
import me.worric.souvenarius.data.db.tasks.SouvenirDeleteTask;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertTask;
import me.worric.souvenarius.data.db.tasks.SouvenirUpdateTask;
import me.worric.souvenarius.data.model.SouvenirDb;

public class DbTaskRunnerImpl implements DbTaskRunner {

    @Inject
    public DbTaskRunnerImpl() {
    }

    @Override
    public void runInsertTask(SouvenirDb souvenir,
                              AppDatabase appDatabase,
                              OnResultListener<SouvenirDb> listener) {
        new SouvenirInsertTask(appDatabase.souvenirDao(), listener).execute(souvenir);
    }

    @Override
    public void runUpdateTask(SouvenirDb souvenir,
                              AppDatabase appDatabase,
                              OnResultListener<SouvenirDb> listener) {
        new SouvenirUpdateTask(appDatabase.souvenirDao(), listener).execute(souvenir);
    }

    @Override
    public void runDeleteTask(SouvenirDb souvenir,
                              AppDatabase appDatabase,
                              OnResultListener<SouvenirDb> listener) {
        new SouvenirDeleteTask(appDatabase.souvenirDao(), listener).execute(souvenir);
    }

}
