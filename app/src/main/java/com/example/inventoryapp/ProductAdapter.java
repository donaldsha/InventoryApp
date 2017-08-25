package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Min-Pc on 7/25/2017.
 */

public class ProductAdapter extends CursorAdapter{

    public ProductAdapter(Context context, Cursor cursor, boolean autoReq){
        super(context, cursor, autoReq);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.items_list, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView prodName = (TextView) view.findViewById(R.id.view_name);
        TextView prodPrice = (TextView) view.findViewById(R.id.view_price);
        TextView prodQuantity = (TextView) view.findViewById(R.id.view_quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductTable.ProductDetails.COLUMN_NAME));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductTable.ProductDetails.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductTable.ProductDetails.COLUMN_QUANTITY));
        final Uri uri = ContentUris.withAppendedId(ProductTable.ProductDetails.URL_CONTENT, cursor.getInt(cursor.getColumnIndexOrThrow(ProductTable.ProductDetails._ID)));

        prodName.setText(name);
        prodPrice.setText(context.getString(R.string.price_label) + " " + price);
        prodQuantity.setText(quantity + " " + " available in stock");

        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 0){
                    int newQuantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductTable.ProductDetails.COLUMN_QUANTITY, newQuantity);
                    context.getContentResolver().update(uri, values, null, null);
                }else {
                    Toast.makeText(context, "Product out of stock, try to order more!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
