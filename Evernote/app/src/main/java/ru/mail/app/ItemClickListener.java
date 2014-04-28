package ru.mail.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by vanik on 27.04.14.
 */
public class ItemClickListener implements AdapterView.OnItemClickListener {
    private static final String LOGTAG = "ItemClickListener";
    private Context context;
    private Cursor cursor;
    public ItemClickListener(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOGTAG, "note list view clicked");
        Log.d(LOGTAG, "id of clicked item = " + view.getId());
        Log.d(LOGTAG, "position of clicked item = " + position);
        cursor.moveToPosition(position);
        int _idIndex = cursor.getColumnIndex("_id");
        int titleIndex = cursor.getColumnIndex("title");
        int contentIndex = cursor.getColumnIndex("content");
        String _id = cursor.getString(_idIndex);
        Intent intent = new Intent(context, NoteShowingActivity.class);
        intent.putExtra("_id", _id);
        intent.putExtra("title", cursor.getString(titleIndex));
        intent.putExtra("content", cursor.getString(contentIndex));
        context.startActivity(intent);
    }
}
