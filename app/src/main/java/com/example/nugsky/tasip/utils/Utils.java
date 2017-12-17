package com.example.nugsky.tasip.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.example.nugsky.tasip.HistogramActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by nugsky on 17/12/17.
 */

public class Utils {
    public static final int PICK_IMAGE = 1;

    public static void pickImage(Fragment fragment){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        fragment.startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public static void passImage(Fragment fragment,Context context,Intent data,Class<?> dstCls){
        Toast.makeText(context, "berhasil", Toast.LENGTH_SHORT).show();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
            Bitmap b = BitmapFactory.decodeStream(inputStream);
            LastPhotoWrapper.bitmap = b;
            Intent intent = new Intent(context, dstCls);
            fragment.startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
