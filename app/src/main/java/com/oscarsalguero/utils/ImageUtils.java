package com.oscarsalguero.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Image Utilities.
 * <p/>
 * Created by RacZo on 9/1/15.
 */
public class ImageUtils {

    private static final String LOG_TAG = ImageUtils.class.getName();

    public static Bitmap decodeBitmap(Activity activity, Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, o);
        final int REQUIRED_SIZE = 100;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, o2);
    }

    public static Bitmap getBitmapWithCorrectOrientation(Bitmap bitmap, Uri uri){
        Bitmap resizedBitmap = null;
        try {
            resizedBitmap = null;
            ExifInterface ei = new ExifInterface(uri.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int angle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }
            Matrix mat = new Matrix();
            mat.postRotate(angle);
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                resizedBitmap = Bitmap.createBitmap(
                        bitmap,
                        bitmap.getWidth() / 2
                                - bitmap.getHeight() / 2, 0,
                        bitmap.getHeight(), bitmap.getHeight(),
                        mat, true);

            } else {
                resizedBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        bitmap.getHeight() / 2
                                - bitmap.getWidth() / 2,
                        bitmap.getWidth(), bitmap.getWidth(),
                        mat, true);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return resizedBitmap;
    }

    public static Uri getOutputMediaFileUriUsingExternalStorageDirectory(){
        String folderAndFileNamePrefix = "colorextractor";
        File imagesFolder = new File(Environment.getExternalStorageDirectory() + File.separator + folderAndFileNamePrefix + File.separator);
        imagesFolder.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = folderAndFileNamePrefix + timeStamp + ".jpg";
        File imageFile = new File(imagesFolder, fileName);
        Uri outputFileUri = Uri.fromFile(imageFile);
        Log.d(LOG_TAG, "Output file URI: " + outputFileUri.toString());
        return outputFileUri;
    }

}