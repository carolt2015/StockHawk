package com.udacity.stockhawk.widget;


import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.DetailStockActivity;

public class DetailWidgetRemoteViewsService extends RemoteViewsService {


    private static final String[] STOCK_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE
    };
    // these indices must match the projection
    private static final int INDEX_STOCK_ID = 0;
    private static final int INDEX_STOCK_DESC = 1;
    private static final int INDEX_STOCK_PRICE = 2;
    private static final int INDEX_STOCK_CHANGE = 3;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        Contract.Quote.URI,
                        STOCK_COLUMNS,
                        null,
                        null,
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }


            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);


                String description = data.getString(INDEX_STOCK_DESC);
                String price = data.getString(INDEX_STOCK_PRICE);
                String change = data.getString(INDEX_STOCK_CHANGE);
                views.setTextViewText(R.id.price, price);
                views.setTextViewText(R.id.symbol1,description);
                views.setTextViewText(R.id.change, change);
                if(Float.parseFloat(change)> 0){
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                }else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

               //Detail view from widget
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(DetailStockActivity.STOCK_SYMBOL, description);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
