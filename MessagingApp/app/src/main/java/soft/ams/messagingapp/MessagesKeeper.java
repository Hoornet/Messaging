package soft.ams.messagingapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.List;

public class MessagesKeeper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "contacts.db";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MESSAGE = "message";
    private static final String KEY_SENDER = "sender";

    private String tableName;

    public MessagesKeeper(Context context, final String tableName) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.tableName = tableName.toLowerCase();

        onCreate(getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " + COLUMN_MESSAGE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    /**
     * Insert message in the user table
     *
     * @param message message to be inserted
     * @return the inserted message id
     */
    public long insert(String message) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE, message);
        long rowId = db.insert(tableName, null, values);
        db.close();

        return rowId;
    }

    /**
     * Count all the messages in the table
     *
     * @return number of items in the table
     */
    public int count() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * Gets all messages in the table
     *
     * @param list List of messages
     */
    public void getAll(List<HashMap<String, String>> list) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        } catch (Exception e) {
            return;
        }
        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put(KEY_SENDER, tableName);
                map.put(COLUMN_MESSAGE, cursor.getString(1));
                list.add(map);
            } while (cursor.moveToNext());
            cursor.close();
        }

        deleteAllRows();
    }

    /**
     * Deletes all messages in the table
     */
    private void deleteAllRows() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }
}
