package com.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ProductAdapter productAdapter;
    private static final int URI_LOAD = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLoaderManager().initLoader(URI_LOAD, null, this);
        initaliseListView();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        try{
            productAdapter.swapCursor(cursor);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    //built based on my previous project
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case URI_LOAD:
                String proj[] = {
                        ProductTable.ProductDetails._ID,
                        ProductTable.ProductDetails.COLUMN_NAME,
                        ProductTable.ProductDetails.COLUMN_PRICE,
                        ProductTable.ProductDetails.COLUMN_QUANTITY

                };
            String sortOrder = ProductTable.ProductDetails._ID + " DESC";
                return new CursorLoader(this, ProductTable.ProductDetails.URL_CONTENT,
                        proj, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productAdapter.swapCursor(null);
    }

    private void initaliseListView(){
        ListView listView = (ListView) findViewById(R.id.list_view);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        productAdapter = new ProductAdapter(this, null, false);
        listView.setAdapter(productAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.setData(ContentUris.withAppendedId(ProductTable.ProductDetails.URL_CONTENT, l));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_item_main:
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
