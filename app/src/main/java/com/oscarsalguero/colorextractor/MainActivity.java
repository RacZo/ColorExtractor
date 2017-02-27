/***
 * Copyright (c) 2015 Oscar Salguero www.oscarsalguero.com
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oscarsalguero.colorextractor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oscarsalguero.colorextractor.utils.ImageUtils;
import com.oscarsalguero.colorextractor.utils.PermissionUtils;

import java.io.File;

/**
 * Sample ppp to demonstrate how Android's new Palette class can be used to extract colors from an image to change the color of UI elements.
 * <p/>
 * Created by RacZo on 9/1/15.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();

    private ImageView imageViewInput;
    private TextView textViewColorVibrant;
    private TextView textViewColorMutedDark;
    private TextView textViewColorMutedLight;
    private TextView textViewSwatchVibrant;
    private TextView textViewSwatchVibrantLight;
    private TextView textViewSwatchVibrantDark;
    private TextView textViewSwatchMuted;
    private TextView textViewSwatchMutedLight;
    private TextView textViewSwatchMutedDark;

    private static final String EMPTY_SPACE = " ";

    public static final String FILE_NAME = "temp.jpg";
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        supportInvalidateOptionsMenu();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        imageViewInput = (ImageView) findViewById(R.id.image_view_input);
        textViewColorVibrant = (TextView) findViewById(R.id.text_view_vibrant);
        textViewColorMutedDark = (TextView) findViewById(R.id.text_view_muted_dark);
        textViewColorMutedLight = (TextView) findViewById(R.id.text_view_muted_light);
        textViewSwatchVibrant = (TextView) findViewById(R.id.text_view_swatch_vibrant);
        textViewSwatchVibrantLight = (TextView) findViewById(R.id.text_view_swatch_vibrant_light);
        textViewSwatchVibrantDark = (TextView) findViewById(R.id.text_view_swatch_vibrant_dark);
        textViewSwatchMuted = (TextView) findViewById(R.id.text_view_swatch_muted);
        textViewSwatchMutedLight = (TextView) findViewById(R.id.text_view_swatch_muted_light);
        textViewSwatchMutedDark = (TextView) findViewById(R.id.text_view_swatch_muted_dark);

        // Converting the default image into a bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_flag_sv);

        // Updating UI
        updateUI(bitmap);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.git_hub_repo_url)));
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                // Getting image from gallery
                Uri selectedImage = data.getData();
                Bitmap bitmap = ImageUtils.decodeBitmap(this, selectedImage);
                setImage(bitmap);
            } catch (Exception e) {
                Log.e(LOG_TAG, "An error has occurred getting the image from gallery", e);
            }
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            try {
                // Getting image from camera
                Log.d(LOG_TAG, "Reading image captured via camera from URI: " + photoUri.toString());
                Bitmap bitmapFromIntent = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                Bitmap bitmapWithCorrectOrientation = ImageUtils.getBitmapWithCorrectOrientation(bitmapFromIntent, photoUri);
                if (bitmapWithCorrectOrientation != null) {
                    setImage(bitmapWithCorrectOrientation);
                } else {
                    setImage(bitmapFromIntent);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "An error has occurred getting the image from camera", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    /**
     * Starts gallery chooser
     */
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    /**
     * Starts the camera
     */
    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    /**
     * Gets file
     * @return
     */
    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    /**
     * Sets the image
     * @param bitmap
     */
    private void setImage(Bitmap bitmap) {
        updateUI(bitmap);
    }

    /**
     * Updates the UI
     * @param bitmap
     */
    private void updateUI(Bitmap bitmap) {
        // Setting image
        imageViewInput.setImageBitmap(bitmap);
        //Generating palette
        Palette.Builder paletteBuilder = Palette.from(bitmap);
        // Use generated palette instance
        paletteBuilder.generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                // Getting the different types of colors from the Image and adding the colors to the TextViews.
                // Colors
                int vibrantColor = palette.getVibrantColor(Color.BLACK); // Will return BLACK if it was not available
                textViewColorVibrant.setBackgroundColor(vibrantColor);
                textViewColorVibrant.setText(getString(R.string.color_vibrant) + EMPTY_SPACE + Integer.toHexString(vibrantColor));
                int lightMutedColor = palette.getLightMutedColor(Color.BLACK);
                textViewColorMutedLight.setBackgroundColor(lightMutedColor);
                textViewColorMutedLight.setText(getString(R.string.color_muted_light) + EMPTY_SPACE + Integer.toHexString(lightMutedColor));
                int darkMutedColor = palette.getDarkMutedColor(Color.BLACK);
                textViewColorMutedDark.setBackgroundColor(darkMutedColor);
                textViewColorMutedDark.setText(getString(R.string.color_muted_dark) + EMPTY_SPACE + Integer.toHexString(darkMutedColor));
                // Swatches
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    textViewSwatchVibrant.setBackgroundColor(vibrantSwatch.getRgb());
                    textViewSwatchVibrant.setText(getString(R.string.swatch_vibrant) + EMPTY_SPACE + Integer.toHexString(vibrantSwatch.getRgb()));
                }
                Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
                if (vibrantLightSwatch != null) {
                    textViewSwatchVibrantLight.setBackgroundColor(vibrantLightSwatch.getRgb());
                    textViewSwatchVibrantLight.setText(getString(R.string.swatch_vibrant_light) + EMPTY_SPACE + Integer.toHexString(vibrantLightSwatch.getRgb()));
                }
                Palette.Swatch vibrantDarkSwatch = palette.getDarkVibrantSwatch();
                if (vibrantDarkSwatch != null) {
                    textViewSwatchVibrantDark.setBackgroundColor(vibrantDarkSwatch.getRgb());
                    textViewSwatchVibrantDark.setText(getString(R.string.swatch_vibrant_dark) + EMPTY_SPACE + Integer.toHexString(vibrantDarkSwatch.getRgb()));
                }
                Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                if (mutedSwatch != null) {
                    textViewSwatchMuted.setBackgroundColor(mutedSwatch.getRgb());
                    textViewSwatchMuted.setText(getString(R.string.swatch_muted) + EMPTY_SPACE + Integer.toHexString(mutedSwatch.getRgb()));
                }
                Palette.Swatch mutedLightSwatch = palette.getLightMutedSwatch();
                if (mutedLightSwatch != null) {
                    textViewSwatchMutedLight.setBackgroundColor(mutedLightSwatch.getRgb());
                    textViewSwatchMutedLight.setText(getString(R.string.swatch_muted_light) + EMPTY_SPACE + Integer.toHexString(mutedLightSwatch.getRgb()));
                }
                Palette.Swatch mutedDarkSwatch = palette.getDarkMutedSwatch();
                if (mutedDarkSwatch != null) {
                    textViewSwatchMutedDark.setBackgroundColor(mutedDarkSwatch.getRgb());
                    textViewSwatchMutedDark.setText(getString(R.string.swatch_muted_dark) + EMPTY_SPACE + Integer.toHexString(mutedDarkSwatch.getRgb()));
                }
                // Changing status bar color
                // The method setStatusBarColor only works above API 21!
                if (mutedDarkSwatch != null) {
                    getWindow().setStatusBarColor(mutedDarkSwatch.getRgb());
                }
                // Changing the background color of the toolbar
                if (mutedSwatch != null) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mutedSwatch.getRgb()));
                    }
                }
            }
        });
    }

}