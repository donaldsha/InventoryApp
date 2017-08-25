package com.example.inventoryapp;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import static android.R.attr.id;

/**
 * Created by Min-Pc on 7/24/2017.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Text field to enter product name, price and show current quantity
     */
    private EditText nameText;
    private EditText priceText;
    private TextView quantityText;

    /**
     * Product information
     */
    private String productName;
    private int productQuantity;

    /**
     * Quantity modifier buttons
     */
    private Button plusOneButton;
    private Button minusOneButton;

    private final static int SELECT_PIC = 200;
    private Button selectPictureButton;
    private ImageView productImage;
    private Bitmap productBitmap;//store and retrieve from database

    private Button orderButton;//order from provider
    private Uri productUri;
    private final static int URI_LOAD = 0;

    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 666;

    private boolean changedProduct;//Check whether the register changed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        productUri = intent.getData();

        if (productUri == null) {
            setTitle(R.string.new_product_add);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_product);
            getLoaderManager().initLoader(URI_LOAD, null, this);
        }
        initialiseViews();
        setOnTouchListener();
    }

    private void initialiseViews() {
        if (productUri != null) {
            orderButton = (Button) findViewById(R.id.order_from_provider_button);

            orderButton.setVisibility(View.VISIBLE);
            orderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mail:to"));
                    intent.setType("text/plain");

                    intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.provider_mail));
                    intent.putExtra(Intent.EXTRA_SUBJECT, productName);
                    startActivity(Intent.createChooser(intent, "Sending mail...."));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }

        nameText = (EditText) findViewById(R.id.edit_name_text);
        quantityText = (EditText) findViewById(R.id.edit_quantity_text);
        priceText = (EditText) findViewById(R.id.edit_price_text);

        plusOneButton = (Button) findViewById(R.id.plus_one_button);
        plusOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productQuantity++;
                quantityText.setText(String.valueOf(productQuantity));
            }
        });

        minusOneButton = (Button) findViewById(R.id.minus_one_button);
        minusOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stop decreasing under 0
                if (productQuantity > 0) {
                    productQuantity--;
                    quantityText.setText(String.valueOf(productQuantity));
                } else {
                    Toast.makeText(DetailsActivity.this, "Can not decrease more than zero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        productImage = (ImageView) findViewById(R.id.product_image);

        selectPictureButton = (Button) findViewById(R.id.select_pic_button);
        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                        return;
                    }
                    Intent getIntent = new Intent(Intent.ACTION_PICK);
                    getIntent.setType("image/*");
                    startActivityForResult(getIntent, SELECT_PIC);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int codeRequest, int codeResult, Intent intent) {
        super.onActivityResult(codeRequest, codeResult, intent);

        if (codeRequest == SELECT_PIC && codeResult == RESULT_OK && intent != null) {
            Uri selectedImage = intent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int colIndex = cursor.getColumnIndex(filePathColumn[0]);

            String picPath = cursor.getString(colIndex);
            productBitmap = BitmapFactory.decodeFile(picPath);
            productImage.setImageBitmap(productBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (productUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_item_menu);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item:
                if (changedProduct) {
                    saveProduct();
                } else {
                    Toast.makeText(this, getString(R.string.product_update_failed), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.delete_item_menu:
                showDeleteConfirmDialog();
                return true;
            case android.R.id.home:
                if (!changedProduct) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                } else {
                    DialogInterface.OnClickListener discardButtonClick =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                                }
                            };
                    showUnsavedChangesDialog(discardButtonClick);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!changedProduct) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClick =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClick);
    }
    
    private void saveProduct(){
        boolean emptyName = checkEmptyField(nameText.getText().toString().trim());
        boolean emptyPrice = checkEmptyField(priceText.getText().toString().trim());
        
        if (emptyName){
            Toast.makeText(this, "Please write a valid product name!", Toast.LENGTH_SHORT).show();
        } else if (productQuantity<=0){
            Toast.makeText(this, "Please write a valid product quantity!", Toast.LENGTH_SHORT).show();
        } else if (emptyPrice){
            Toast.makeText(this, "Please write a valid price!", Toast.LENGTH_SHORT).show();
        }else if (productBitmap == null){
            Toast.makeText(this, "Please, select a valid image!", Toast.LENGTH_SHORT).show();
        } else {
            String name = nameText.getText().toString().trim();
            double price = Double.parseDouble(priceText.getText().toString().trim());

            ContentValues values = new ContentValues();
            values.put(ProductTable.ProductDetails.COLUMN_NAME, name);
            values.put(ProductTable.ProductDetails.COLUMN_QUANTITY, productQuantity);
            values.put(ProductTable.ProductDetails.COLUMN_PRICE, price);
            byte[] imageSelected = getBytes(productBitmap);
            values.put(ProductTable.ProductDetails.COLUMN_IMAGE, imageSelected);

            if(productUri == null){
                Uri newProdUri = getContentResolver().insert(ProductTable.ProductDetails.URL_CONTENT, values);
                Toast.makeText(this, "New product added successfully", Toast.LENGTH_SHORT).show();
            }else{
                int newProdUri = getContentResolver().update(productUri, values, null,null);
                Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    //checks wheteher EditText fields are empty
    private boolean checkEmptyField(String string){
        return TextUtils.isEmpty(string) || string.equals(".");
    }

    //convert from bitmap to byte array so that we can store it into our database
    public static byte[] getBytes(Bitmap bit){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.PNG, 0, output);
        return output.toByteArray();
    }

    //convert from byte to Bitmap to display image
    public static Bitmap getImage(byte[] image){
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    //delete a product from database
    private void productDelete(){
        if (productUri != null){
            int deletedRows = getContentResolver().delete(productUri, null, null);
            if (deletedRows == 0){
                Toast.makeText(this, "Could not delete product! \n Please try again", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //delete confirmation before deleting product
    private void showDeleteConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_product));
        builder.setPositiveButton(getString(R.string.delete_button_alert), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                productDelete();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button_alert), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    //confirmation before exiting activity
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButOnClickList){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.leave_without_save_button));
        builder.setPositiveButton(getString(R.string.yes_button), discardButOnClickList);
        builder.setNegativeButton(getString(R.string.cancel_button_alert), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface == null){
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setOnTouchListener(){
        nameText.setOnTouchListener(touchListener);
        quantityText.setOnTouchListener(touchListener);
        priceText.setOnTouchListener(touchListener);
        plusOneButton.setOnTouchListener(touchListener);
        minusOneButton.setOnTouchListener(touchListener);
        selectPictureButton.setOnTouchListener(touchListener);
    }


    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            changedProduct = true;
            return false;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case URI_LOAD:
                return new CursorLoader(this, productUri, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()){
            productName = cursor.getString(cursor.getColumnIndex(ProductTable.ProductDetails.COLUMN_NAME));
            nameText.setText(productName);

            priceText.setText(cursor.getString(cursor.getColumnIndex(ProductTable.ProductDetails.COLUMN_PRICE)));
            productQuantity = cursor.getInt(cursor.getColumnIndex(ProductTable.ProductDetails.COLUMN_QUANTITY));

            quantityText.setText(String.valueOf(productQuantity));
            if (cursor.getBlob(cursor.getColumnIndex(ProductTable.ProductDetails.COLUMN_IMAGE)) != null){
                productImage.setImageBitmap(getImage(cursor.getBlob(cursor.getColumnIndex(ProductTable.ProductDetails.COLUMN_IMAGE))));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameText.getText().clear();
        quantityText.setText("");
    }
}

