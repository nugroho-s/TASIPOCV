package com.example.nugsky.tasip.utils;

import android.content.Intent;
import android.support.v4.app.Fragment;

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
}
