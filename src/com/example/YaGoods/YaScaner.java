package com.example.YaGoods;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;

public class YaScaner extends Activity {
    /**
     * Called when the activity is first created.
     */
    private static boolean al = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(!al){
            dispatchTakePictureIntent(111);
            al = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 111){
            try{
                doOcr(handleSmallCameraPhoto(data));
            } catch (Exception ex){
                ((TextView)findViewById(R.id.textfield)).setText("ex: " + ex.getMessage());
            }
        }
    }

    private Bitmap handleSmallCameraPhoto(Intent intent) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile("/mnt/sdcard/1");
        ExifInterface exif = new ExifInterface("/mnt/sdcard/1");
        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        int rotate = 0;

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
        }

        if (rotate != 0) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            // Setting pre rotate
            Matrix mtx = new Matrix();
            mtx.preRotate(rotate);

            // Rotating Bitmap & convert to ARGB_8888, required by tess
            try{
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        int w = bitmap.getWidth()/2;
        int h = bitmap.getHeight()/2;
        bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);


        return bitmap;//Bitmap.createScaledBitmap(bitmap, w, h, true);
    }

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri mImageUri = Uri.fromFile(new File("/mnt/sdcard/1"));

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(takePictureIntent, actionCode);
    }


    private void doOcr(Bitmap bitmap){
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.init("/mnt/sdcard/tesseract", "eng");
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();
        ((TextView)findViewById(R.id.textfield)).setText(recognizedText);
    }

}
