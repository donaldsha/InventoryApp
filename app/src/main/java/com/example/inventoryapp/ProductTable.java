package com.example.inventoryapp;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductTable {


    public static final String CONTENT_AUTHORITY = "com.example.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    private ProductTable() {
    }

    //I am building this database based on the previous project
    public static final class ProductDetails {

        public static final Uri URL_CONTENT = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public static final String TABLE_NAME = "products";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_IMAGE = "image";

        //for a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //for a list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

    }
}
