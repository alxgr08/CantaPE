package pe.cantape;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

final class PaymentStore extends SQLiteOpenHelper {
    static final class Payment {
        final long at;
        final long cents;
        final String sender;
        Payment(long at, long cents, String sender) { this.at = at; this.cents = cents; this.sender = sender; }
    }

    PaymentStore(Context context) {
        super(context.getApplicationContext(), "payments.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE payments (id INTEGER PRIMARY KEY AUTOINCREMENT, source_key TEXT NOT NULL, at_ms INTEGER NOT NULL, cents INTEGER NOT NULL, sender TEXT)");
        db.execSQL("CREATE INDEX payments_recent ON payments(source_key, cents, at_ms)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE payments ADD COLUMN sender TEXT");
    }

    /** Returns false when a notification update repeats a recent payment. */
    boolean addIfNew(String sourceKey, long atMs, long cents, String sender) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor recent = db.rawQuery(
                    "SELECT 1 FROM payments WHERE source_key=? AND cents=? AND at_ms>=? LIMIT 1",
                    new String[]{sourceKey, Long.toString(cents), Long.toString(atMs - 10_000)})) {
                if (recent.moveToFirst()) {
                    db.setTransactionSuccessful();
                    return false;
                }
            }
            ContentValues values = new ContentValues();
            values.put("source_key", sourceKey);
            values.put("at_ms", atMs);
            values.put("cents", cents);
            values.put("sender", sender);
            db.insertOrThrow("payments", null, values);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    void clear() {
        getWritableDatabase().delete("payments", null, null);
    }

    long totalCents() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(cents), 0) FROM payments", null)) {
            cursor.moveToFirst();
            return cursor.getLong(0);
        }
    }

    int count() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM payments", null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    List<Payment> all() {
        List<Payment> payments = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT at_ms, cents, sender FROM payments ORDER BY at_ms DESC, id DESC", null)) {
            while (cursor.moveToNext()) payments.add(new Payment(cursor.getLong(0), cursor.getLong(1),
                    cursor.isNull(2) ? "" : cursor.getString(2)));
        }
        return payments;
    }
}
