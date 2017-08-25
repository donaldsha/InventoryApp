package com.example.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Min-Pc on 7/24/2017.
 * Built this class based on the project I delivered about habit tracker
 */

public class ProductDatabase extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "inventory.db";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ProductTable.ProductDetails.TABLE_NAME;
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductTable.ProductDetails.TABLE_NAME + " ( " +
            ProductTable.ProductDetails._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProductTable.ProductDetails.COLUMN_NAME + " TEXT NOT NULL, " +
            ProductTable.ProductDetails.COLUMN_PRICE + " REAL NOT NULL, " +
            ProductTable.ProductDetails.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
            ProductTable.ProductDetails.COLUMN_IMAGE + " BLOB NOT NULL ) " + ";";

    public ProductDatabase(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        Log.i("SQLENTRIES", SQL_CREATE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
