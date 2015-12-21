package com.example.melih.mandalina;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private File pictureFile;
    private ImageView scanner , flashBtn;
    private Animation mAnimation;
    private boolean flash = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        scanner = (ImageView) findViewById(R.id.scanner);
        preview.addView(mCameraPreview);

        final ImageView captureButton = (ImageView) findViewById(R.id.button_capture);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);

        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFlashMode();
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
                captureButton.setClickable(false);
            }
        });



    }

    private void changeFlashMode(){

        Camera.Parameters p = mCamera.getParameters();
        if(flash == false){
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            flash=true;
            flashBtn.setImageResource(R.drawable.ic_flash_white_36dp);
        }else{
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            flashBtn.setImageResource(R.drawable.ic_flash_off_white_36dp);
            flash=false;
        }
        mCamera.setParameters(p);
    }


    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }

            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, blob);
            byte[] bitmapdata = blob.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bitmapdata);
                fos.close();

                mCameraPreview.getHolder().removeCallback(mCameraPreview);
                camera.release();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
            new RequestTask().execute(pictureFile);
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
    //imagei upload ediyor ve bitince sonuç için bekliyor.
    class RequestTask extends AsyncTask<File,Void,JSONObject> {

        @Override
        protected void onPreExecute() {
            scanner.setVisibility(View.VISIBLE);
            mAnimation = new TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, -0.5f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0.48f);
            mAnimation.setDuration(3500);
            mAnimation.setRepeatCount(-1);
            mAnimation.setRepeatMode(Animation.REVERSE);
            mAnimation.setInterpolator(new LinearInterpolator());
            scanner.setAnimation(mAnimation);
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(File... params) {
            JSONObject obj = CloudSight.getInstance().postImage(params[0]);
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                Log.d("INFO","asking for result for this token: " + jsonObject.getString("token"));
                new AskResult().execute(jsonObject.getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(jsonObject);
        }
    }
    //sonuç için sürekli request yapıyor.
    class AskResult extends AsyncTask<String,Void,JSONObject>{
        String token="";
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if(!jsonObject.getString("status").equals("completed")){
                    Log.d("INFO",jsonObject.toString());
                    new AskResult().execute(token);}
                else {
                    Log.d("INFO", jsonObject.getString("name"));
                    Toast.makeText(MainActivity.this,jsonObject.getString("name"),Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this,ProductListActivitiy.class);

                    String query = null;
                    try {
                        query = meaningful(jsonObject.getString("name"));
                        Log.d("meaningful",query);


                    } catch (Exception e) {
                        query = jsonObject.getString("name");
                        e.printStackTrace();
                    }
                    i.putExtra("query",query);
                    startActivity(i);
                }
                super.onPostExecute(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String meaningful(String noisy){
            noisy = noisy.toLowerCase().replace("ç", "c");
            noisy = noisy.replace("ğ", "g");
            noisy = noisy.replace("ı", "i");
            noisy = noisy.replace("ö", "o");
            noisy = noisy.replace("ş", "s");
            noisy = noisy.replace("ü", "u");
            noisy = noisy.replace("ş", "s");
            JSONArray color = null;
            JSONArray gender = null;
            JSONArray category = null;
            JSONArray pattern = null;
            JSONArray brand = null;
            try {
                color = new JSONArray(readFile("color.txt"));
                gender = new JSONArray(readFile("gender.txt"));
                category = new JSONArray(readFile("category.txt"));
                pattern = new JSONArray(readFile("pattern.txt"));
                brand = new JSONArray(readFile("brand.txt"));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            String colorString = "";
            String genderString = "";
            String categoryString = "";
            String patternString = "";
            String brandString = "";
            try {
                for (JSONObject o : bests(color, noisy)) {
                    colorString += o.get("title") + " ";
                }
            } catch (Exception e) {

            }
            try {
                for (JSONObject o : bests(pattern, noisy)) {
                    patternString += o.get("title") + " ";
                }
            } catch (Exception e) {

            }
            try {
                genderString = best(gender, noisy).get("title").toString()+" ";
            } catch (Exception e) {

            }
            try {
                categoryString = best(category, noisy).get("title").toString();
            } catch (Exception e) {

            }
            try {
                brandString = best(brand, noisy).get("title").toString() + " ";
            } catch (Exception e) {

            }
            if (categoryString.isEmpty()) {
                return noisy;
            } else if (genderString.isEmpty() && colorString.isEmpty()
                    && patternString.isEmpty() && brandString.isEmpty()) {
                return noisy;
            } else {
                String all = colorString + genderString + patternString
                        + brandString + categoryString;
                return all;
            }
        }

        JSONObject best(JSONArray array, String key) throws JSONException {
            JSONObject best = new JSONObject();
            int max = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                JSONArray objectArray = object.getJSONArray("keyword");
                int length = 0;
                for (int j = 0; j < objectArray.length(); j++) {
                    if (key.contains(objectArray.get(j).toString())) {
                        length++;
                    }
                }
                if (length > max) {
                    max = length;
                    best = object;
                }
            }
            return best;
        }

        ArrayList<JSONObject> bests(JSONArray array, String key) throws JSONException {
            ArrayList<JSONObject> arList = new ArrayList<JSONObject>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                JSONArray objectArray = object.getJSONArray("keyword");
                int length = 0;
                for (int j = 0; j < objectArray.length(); j++) {
                    if (key.contains(objectArray.get(j).toString())) {
                        length++;
                    }
                }
                if (length >= 1) {
                    arList.add(object);
                }
            }
            return arList;
        }



        @Override
        protected JSONObject doInBackground(String... params) {
            token=params[0];
            return CloudSight.getInstance().askResult(token);
        }
    }
    String readFile(String path) throws IOException {
        StringBuilder buf=new StringBuilder();
        InputStream json=getAssets().open(path);
        BufferedReader in=
                new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);

        }
        in.close();
        return buf.toString();
    }
}