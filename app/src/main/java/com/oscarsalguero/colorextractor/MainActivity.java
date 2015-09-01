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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
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

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String LOG_TAG = MainActivity.class.getName();
    private ImageView imageViewInput;
    private TextView textViewVibrant;
    private TextView textViewVibrantLight;
    private TextView textViewVibrantDark;
    private TextView textViewMuted;
    private TextView textViewMutedLight;
    private TextView textViewMutedDark;
    private ActionBar actionBar;
    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_launcher);
        }
        imageViewInput = (ImageView) findViewById(R.id.image_view_input);
        textViewVibrant = (TextView) findViewById(R.id.text_view_vibrant);
        textViewVibrantLight = (TextView) findViewById(R.id.text_view_vibrant_light);
        textViewVibrantDark = (TextView) findViewById(R.id.text_view_vibrant_dark);
        textViewMuted = (TextView) findViewById(R.id.text_view_muted);
        textViewMutedLight = (TextView) findViewById(R.id.text_view_muted_light);
        textViewMutedDark = (TextView) findViewById(R.id.text_view_muted_dark);
        // Converting the default image into a bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_test_image);
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
        switch (id){
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

    private void openImagePicker(){
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
                        Log.d(LOG_TAG, "Reading camera captured image from URI: " + outputFileUri.toString());
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
        imageViewInput.setImageBitmap(bitmap);
        imageViewInput.setVisibility(View.VISIBLE);
        updateUI(bitmap);
    }

    private void updateUI(Bitmap bitmap){
        //Generating palette
        Palette palette = Palette.generate(bitmap);
        // Getting the different types of colors from the Image and adding the colors to the TextViews.
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        if(vibrantSwatch!=null){
            float[] vibrant = vibrantSwatch.getHsl();
            textViewVibrant.setBackgroundColor(Color.HSVToColor(vibrant));
            // The method setStatusBarColor only works above API 21!
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.HSVToColor(vibrant));
            }
        }
        Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
        if(vibrantLightSwatch!=null){
            textViewVibrantLight.setBackgroundColor(Color.HSVToColor(vibrantLightSwatch.getHsl()));
            // Changing the background color of the toolbar to Vibrant Light Swatch
            if(actionBar!=null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.HSVToColor(vibrantLightSwatch.getHsl())));
            }
        }
        Palette.Swatch vibrantDarkSwatch = palette.getDarkVibrantSwatch();
        if(vibrantDarkSwatch != null){
            textViewVibrantDark.setBackgroundColor(Color.HSVToColor(vibrantDarkSwatch.getHsl()));
        }
        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        if(mutedSwatch!=null) {
            textViewMuted.setBackgroundColor(Color.HSVToColor(mutedSwatch.getHsl()));
        }
        Palette.Swatch mutedLightSwatch = palette.getLightMutedSwatch();
        if(mutedLightSwatch!=null) {
            textViewMutedLight.setBackgroundColor(Color.HSVToColor(mutedLightSwatch.getHsl()));
        }
        Palette.Swatch mutedDarkSwatch = palette.getDarkMutedSwatch();
        if(mutedDarkSwatch!=null) {
            textViewMutedDark.setBackgroundColor(Color.HSVToColor(mutedDarkSwatch.getHsl()));
        }
    }

}
