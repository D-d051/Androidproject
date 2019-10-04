package com.dipen.sqlite_recview;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ListCursorAdapter extends CursorAdapter {

    private static final String TAG = "sam_bread";

    private ListSqliteOpenHelper mOpenHelper;

    public ListCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
        mOpenHelper = new ListSqliteOpenHelper(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView idHolder = ((TextView) view.findViewById(R.id.tv_id_holder));

        TextView title = ((TextView) view.findViewById(R.id.tv_title));
        TextView place = ((TextView) view.findViewById(R.id.tv_palce));

        TextView date = ((TextView) view.findViewById(R.id.tv_date));

        if (cursor == null) {
            Log.d(TAG, "ListCursorAdapter, bindView: Cursor is null");
            return;
        }

        int idIndex = cursor.getColumnIndex(ListSqliteOpenHelper.COL_1_ID);
        int titleIndex = cursor.getColumnIndex(ListSqliteOpenHelper.COL_2_TITLE);
        int placeIndex = cursor.getColumnIndex(ListSqliteOpenHelper.COL_3_PLACE);
        int dateIndex = cursor.getColumnIndex(ListSqliteOpenHelper.COL_5_DATE);

        String stringId = cursor.getString(idIndex);
        String stringTitle = cursor.getString(titleIndex);
        String stringPlace = cursor.getString(placeIndex);
        long unixTime = cursor.getLong(dateIndex);

        idHolder.setText(stringId);
        title.setText(stringTitle);
        place.setText(stringPlace);
        date.setText(mOpenHelper.formatDate(unixTime));

    }


}
