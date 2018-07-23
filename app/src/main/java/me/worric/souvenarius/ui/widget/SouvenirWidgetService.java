package me.worric.souvenarius.ui.widget;

import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class SouvenirWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new SouvenirRemoteViewsFactory();
    }

    public static class SouvenirRemoteViewsFactory implements RemoteViewsFactory {


        @Override
        public void onDataSetChanged() {

        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDestroy() {

        }
    }

}
