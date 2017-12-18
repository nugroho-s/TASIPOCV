package com.example.nugsky.tasip.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nugsky.tasip.HistogramActivity;
import com.example.nugsky.tasip.R;

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

    public static void setPhotoSelector(Button button, final Fragment fragment){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(fragment);
            }
        });
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

    public static void setTitle(Fragment fragment,String title){
        android.app.ActionBar actionBar = fragment.getActivity().getActionBar();
        actionBar.setTitle(title);
    }

    public static void setTextTitle(TextView textView, String text){
        textView.setText(String.format("Select an image for %s",text));
    }

    public static void setTextTitle(Fragment fragment,View view){
        TextView textView = view.findViewById(R.id.select_message);
        textView.setText("Select an image for "+fragment.getClass().getSimpleName());
    }

    public static void initSelectorFragment(Fragment fragment, View view){
        setTextTitle(fragment,view);
        setPhotoSelector((Button)view.findViewById(R.id.photo_selector),fragment);
    }
}
