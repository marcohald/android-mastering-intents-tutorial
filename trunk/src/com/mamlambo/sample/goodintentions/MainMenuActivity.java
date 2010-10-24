/*
 * Copyright (c) 2010, Lauren Darcey and Shane Conder
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of 
 *   conditions and the following disclaimer.
 *   
 * * Redistributions in binary form must reproduce the above copyright notice, this list 
 *   of conditions and the following disclaimer in the documentation and/or other 
 *   materials provided with the distribution.
 *   
 * * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific prior 
 *   written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * <ORGANIZATION> = Mamlambo
 */package com.mamlambo.sample.goodintentions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mamlambo.sample.goodintentions.utils.Utils;

public class MainMenuActivity extends Activity {
    private static final String DEBUG_TAG = "MainMenuActivity";
    private static final int GALLERY_PICKER_RESULT = 10001;
    private static final int GALLERY_PICKER_RESULT_MOD = 10002;
    private static final int MOD_RESULT = 10101;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void getImageFromGallery(View view) {
        Intent galleryPicker = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryPicker, GALLERY_PICKER_RESULT);
    }

    public void getModImage(View view) {
        Intent galleryPicker = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryPicker, GALLERY_PICKER_RESULT_MOD);
    }

    public void onBrowseWebsite(View view) {
        Uri address = Uri.parse("http://androidbook.blogspot.com");
        Intent surf = new Intent(Intent.ACTION_VIEW, address);
        startActivity(surf);
    }
    
    public void onDialPhone(View view) {
        Uri number = Uri.parse("tel:2125551212");
        Intent dial = new Intent(Intent.ACTION_DIAL, number);
        startActivity(dial);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.e(DEBUG_TAG, "Failed to get ok result.");
            return;
        }
        switch (requestCode) {
        case GALLERY_PICKER_RESULT:
            try {

                Uri imageUri = data.getData();
                Log.v(DEBUG_TAG, "Image URI: " + imageUri);
                Toast
                        .makeText(
                                this,
                                "Picked Image URI: "
                                        + imageUri.toString()
                                        + "\nLaunching DisplayActivity with VIEW action",
                                Toast.LENGTH_LONG).show();

                String imagePath = Utils
                        .getFilePathFromMediaUri(this, imageUri);
                Intent viewIntent = new Intent(this, DisplayActivity.class);
                viewIntent.setAction(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse(imagePath));
                startActivity(viewIntent);
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to pass picker result on");
            }
            break;
        case GALLERY_PICKER_RESULT_MOD:
            try {

                Uri imageUri = data.getData();

                Toast
                        .makeText(
                                this,
                                "Picked Image URI: "
                                        + imageUri.toString()
                                        + "\nLaunching DisplayActivity with MODIFY action",
                                Toast.LENGTH_LONG).show();
                Intent modIntent = new Intent(this, DisplayActivity.class);
                modIntent.setAction(DisplayActivity.INTENT_ACTION_MOD);
                modIntent.setData(Uri.parse(Utils.getFilePathFromMediaUri(this,
                        imageUri)));

                startActivityForResult(modIntent, MOD_RESULT);
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to pass picked image on for mod");
            }
            break;
        case MOD_RESULT:
            String filename = data
                    .getStringExtra(DisplayActivity.EXTRA_MOD_FILENAME);
            Toast.makeText(
                    this,
                    "Got result from DisplayActivity MODIFY action: "
                            + filename, Toast.LENGTH_LONG).show();
            break;
        }
    }

}