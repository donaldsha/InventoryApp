package com.example.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

/**
 * Created by Min-Pc on 7/24/2017.
 */

public class ProductSupplier extends ContentProvider {
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String LOG_TAG = ProductSupplier.class.getSimpleName();
    private ProductDatabase prodDatabaseHelper;

    static {
        uriMatcher.addURI(ProductTable.CONTENT_AUTHORITY, ProductTable.PATH_PRODUCTS, PRODUCTS);

        uriMatcher.addURI(ProductTable.CONTENT_AUTHORITY, ProductTable.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        prodDatabaseHelper = new ProductDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectArgs, @Nullable String sortOrder) {
        SQLiteDatabase sqlDb = prodDatabaseHelper.getReadableDatabase();
        Cursor cursor = null;
        int matchUri = uriMatcher.match(uri);

        switch (matchUri) {
            case PRODUCTS:
                cursor = sqlDb.query(ProductTable.ProductDetails.TABLE_NAME, projection, null, null, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductTable.ProductDetails._ID + "=?";
                selectArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqlDb.query(ProductTable.ProductDetails.TABLE_NAME, projection, selection, selectArgs, null, null, sortOrder);

                break;
            default:
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case PRODUCTS:
                return ProductTable.ProductDetails.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductTable.ProductDetails.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + matchUri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case PRODUCTS:
                return insertProducts(uri, contentValues);

            default:
                throw new IllegalArgumentException("Can not query unknown URI" + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase sqlDb = prodDatabaseHelper.getWritableDatabase();
        int deletedRows;

        final int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case PRODUCTS:
                deletedRows = sqlDb.delete(ProductTable.ProductDetails.TABLE_NAME, s, strings);
                break;
            case PRODUCT_ID:
                s = ProductTable.ProductDetails._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = sqlDb.delete(ProductTable.ProductDetails.TABLE_NAME, s, strings);
                break;
            default:
                throw new IllegalArgumentException("Delete not supported for: " + uri);
        }
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    private Uri insertProducts(Uri uri, ContentValues contentValues) {
        SQLiteDatabase sqlDb = prodDatabaseHelper.getWritableDatabase();

        long id = sqlDb.insert(ProductTable.ProductDetails.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Row insert failed for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case PRODUCTS:
                getContext().getContentResolver().notifyChange(uri, null);
                return updateProducts(uri, contentValues, s, strings);
            case PRODUCT_ID:
                getContext().getContentResolver().notifyChange(uri, null);
                s = ProductTable.ProductDetails._ID + "=?";
                strings = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateProducts(uri, contentValues, s, strings);
            default:
                throw new IllegalArgumentException("Update not supported for: " + uri);
        }
    }

    private int updateProducts(Uri uri, ContentValues values, String selection, String[] strings){
        SQLiteDatabase sqlDb = prodDatabaseHelper.getWritableDatabase();

        if (values.size() == 0){
            return 0;
        }

        int updatedRows = sqlDb.update(ProductTable.ProductDetails.TABLE_NAME, values, selection, strings);

        if (updatedRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }
}
