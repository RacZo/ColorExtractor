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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oscarsalguero.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample ppp to demonstrate how Android's new Palette class can be used to extract colors from an image to change the color of UI elements.
 * <p/>
 * Created by RacZo on 9/1/15.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();

    private CoordinatorLayout coordinatorLayout;
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
    private Uri outputFileUri;
    private static final String EMPTY_SPACE = " ";

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    /**
     * Id to identify a camera permission request.
     */
    private static final int REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        supportInvalidateOptionsMenu();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_change_image:
                openImagePicker();
                break;
            case R.id.action_about:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.git_hub_repo_url)));
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openImagePicker() {
        outputFileUri = ImageUtils.getOutputMediaFileUriUsingExternalStorageDirectory();
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        captureIntent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_BACK);
        final PackageManager packageManager = this.getPackageManager();
        final List<ResolveInfo> listCam = packageManager
                .queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        // galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image Source");
        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

                if (intent != null && intent.getData() != null) {
                    try {
                        // Getting image from gallery
                        Uri selectedImage = intent.getData();
                        Bitmap bitmap = ImageUtils.decodeBitmap(this, selectedImage);
                        setImage(bitmap);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    try {
                        // Getting image from camera
                        Log.d(LOG_TAG, "Reading image captured via camera from URI: " + outputFileUri.toString());
                        Bitmap bitmapFromIntent = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputFileUri);
                        Bitmap bitmapWithCorrectOrientation = ImageUtils.getBitmapWithCorrectOrientation(bitmapFromIntent, outputFileUri);
                        if (bitmapWithCorrectOrientation != null) {
                            setImage(bitmapWithCorrectOrientation);
                        } else {
                            setImage(bitmapFromIntent);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setImage(Bitmap bitmap) {
        // Updating UI
        updateUI(bitmap);
    }

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
                if (Build.VERSION.SDK_INT >= 21) {
                    if (mutedDarkSwatch != null) {
                        getWindow().setStatusBarColor(mutedDarkSwatch.getRgb());
                    }
                }
                // Changing the background color of the toolbar
                if (mutedSwatch != null) {
                    if (actionBar != null) {
                        actionBar.setBackgroundDrawable(new ColorDrawable(mutedSwatch.getRgb()));
                    }
                }
            }
        });
    }

    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showCamera(View view) {
        Log.i(LOG_TAG, "Show camera button pressed. Checking permission.");
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            requestCameraPermission();

        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i(LOG_TAG,
                    "CAMERA permission has already been granted. Displaying camera preview.");
            // showCameraPreview();
        }
        // END_INCLUDE(camera_permission)

    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        Log.i(LOG_TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(LOG_TAG,
                    "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(coordinatorLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

}