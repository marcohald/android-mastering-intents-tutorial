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
 */
package com.mamlambo.sample.goodintentions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mamlambo.sample.goodintentions.utils.Utils;

public class DisplayActivity extends Activity {
    private static final String DEBUG_TAG = "DisplayActivity";

    public static final String INTENT_ACTION_MOD = "com.mamlambo.sample.goodintentions.ACTION_MOD";

    public static final String EXTRA_MOD_FILENAME = "com.mamlambo.sample.goodintentions.EXTRA_MOD_FILENAME";
    public static final int RESULT_FAILED = Activity.RESULT_FIRST_USER + 1;

    @Override
    protected void onPause() {
        ImageView imageView = (ImageView)findViewById(R.id.displayImage);
        Drawable image = imageView.getDrawable();
        image.setCallback(null);
        super.onPause();
        Log.v(DEBUG_TAG, "onPause");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);

        Intent detailsIntent = getIntent();

        String action = detailsIntent.getAction();
        if (Intent.ACTION_VIEW.equalsIgnoreCase(action)) {
            Uri dataPath = detailsIntent.getData();
            try {
                Toast.makeText(this,
                        "Received VIEW action\n" + dataPath.getPath(),
                        Toast.LENGTH_LONG).show();
                ImageView imageView = (ImageView) findViewById(R.id.displayImage);
                imageView.setImageURI(dataPath);                
                
                Button doMod = (Button) findViewById(R.id.doModification);
                Button cancel = (Button) findViewById(R.id.cancelButton);
                doMod.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to draw image", e);
                finish();
            }
        } else if (Intent.ACTION_SEND.equalsIgnoreCase(action)) {
            Bundle info = detailsIntent.getExtras();
            try {
                Uri location = (Uri) info.get(Intent.EXTRA_STREAM);
                Toast.makeText(this,
                        "Received SEND action\n" + location.toString(),
                        Toast.LENGTH_LONG).show();

                Bitmap image = BitmapFactory.decodeFile(Utils
                        .getFilePathFromMediaUri(this, location));
                ImageView imageView = (ImageView) findViewById(R.id.displayImage);
                imageView.setImageBitmap(image);
                Button doMod = (Button) findViewById(R.id.doModification);
                doMod.setVisibility(View.GONE);
                Button doSend = (Button) findViewById(R.id.doSend);
                doSend.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed");
                finish();
            }
        } else if (INTENT_ACTION_MOD.equalsIgnoreCase(action)) {
            try {
                this.setResult(RESULT_CANCELED);

                Uri dataPath = detailsIntent.getData();
                Toast.makeText(this,
                        "Received custom MODIFY action\n" + dataPath.getPath(),
                        Toast.LENGTH_LONG).show();
                ImageView imageView = (ImageView) findViewById(R.id.displayImage);
                imageView.setImageURI(dataPath);
                imageToModifyPath = dataPath;
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to draw image.", e);
                finish();
            }
        }
    }

    Uri imageToModifyPath = null;

    public void doModification(View view) {
        if (imageToModifyPath == null) {
            this.setResult(RESULT_FAILED);
            finish();
        }
        Bitmap newBitmap = null;

        try {
            Bitmap imageToModify= BitmapFactory.decodeFile(imageToModifyPath.getPath());

            Bitmap.Config srcConfig = imageToModify.getConfig();

            newBitmap = Bitmap.createBitmap(imageToModify.getWidth(),
                    imageToModify.getHeight(), srcConfig);

            Canvas canvas = new Canvas(newBitmap);
            Paint paint = new Paint();
            paint.setColorFilter(new PorterDuffColorFilter(Color.RED,
                    PorterDuff.Mode.MULTIPLY));
            canvas.drawBitmap(imageToModify, 0, 0, paint);
            imageToModify.recycle();

            Paint textPaint = new Paint();
            textPaint.setColor(Color.GREEN);
            textPaint.setTextSize(75);
            textPaint.setTypeface(Typeface.SERIF);
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setAntiAlias(true);

            canvas.drawText("Hello Modification!", canvas.getWidth() / 10,
                    canvas.getHeight() / 2, textPaint);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Failed to create new bitmap.", e);
            Toast.makeText(this, "Failed to create modified bitmap",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        FileOutputStream output;
        try {
            File picDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            picDir.mkdirs();
            Date date = new Date();
            File imageFile = new File(picDir, "mod" + date.getTime() + ".jpg");

            output = new FileOutputStream(imageFile);

            newBitmap.compress(CompressFormat.JPEG, 85, output);

            output.close();

            // add to the media database
            MediaScannerConnection.scanFile(this, new String[] { imageFile
                    .toString() }, null, null);
            Intent resultIntent = new Intent();

            resultIntent.putExtra(EXTRA_MOD_FILENAME, imageFile.toString());
            this.setResult(RESULT_OK, resultIntent);

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Failed to output modified image", e);
            this.setResult(RESULT_FAILED);

        }

        finish();
    }

    public void onCancelButton(View view) {
        finish();
    }

    public void onSendButton(View view) {
        Toast.makeText(this, "TODO: implement doSend()", Toast.LENGTH_SHORT)
                .show();
    }

}
