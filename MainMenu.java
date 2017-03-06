package moodify.moodify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.R.attr.value;

public class MainMenu extends AppCompatActivity {
    TextView textView;
    boolean flag = true;
    Service service;
    public static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.imageDisplay);
        textView = (TextView) findViewById(R.id.fullscreen_content);
        gifImageView.setGifImageResource(R.drawable.colourcircle);
        final float[] from = new float[3],
                to =   new float[3];
        Color.colorToHSV(Color.parseColor("#88000066"), from);   // from blue
        Color.colorToHSV(Color.parseColor("#88660000"), to);     // to red

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
        anim.setRepeatCount(Animation.INFINITE);
        int colourTrans=10000;
        anim.setDuration(colourTrans);

        final float[] hsv  = new float[3];                  // transition color
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                // Transition along each axis of HSV (hue, saturation, value)
                hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

                textView.setTextColor(Color.HSVToColor(hsv));
            }
        });
        anim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationRepeat(Animator animation)
            {
                hsv[0] = to[0];
                hsv[1] = to[1];
                hsv[2] = to[2];
                if(flag){
                    Color.colorToHSV(Color.parseColor("#88660000"), from); //red
                    Color.colorToHSV(Color.parseColor("#88000066"), to); // blue
                    flag = !flag;
                }
                else {
                    Color.colorToHSV(Color.parseColor("#88000066"), from); //blue
                    Color.colorToHSV(Color.parseColor("#88660000"), to); //red
                    flag = !flag;
                }
            }
        });

        anim.start();


        Button choose_from_gallery_button = (Button)findViewById(R.id.rightButton);
        Button take_picture_button = (Button)findViewById(R.id.leftButton);

        OkHttpClient client = new OkHttpClient.Builder().build();

        //need to add url
        service = new Retrofit.Builder().baseUrl("http://138.197.159.124/").client(client).build().create(Service.class);



        take_picture_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(View v) {
                dispatchTakePictureIntent();

            }
        });


        choose_from_gallery_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

            }
        });
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
            }
        }
    }

    String mCurrentPhotoPath;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            Uri selectedImage = data.getData();
            String path = selectedImage.getPath();

            File file = new File(getPath(this, selectedImage));

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");


            retrofit2.Call<okhttp3.ResponseBody> req = service.postImage(body);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                    try {
                        httpRequest(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }

        if(requestCode == 2){
            File file = new File(mCurrentPhotoPath);

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
            //RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");


            retrofit2.Call<okhttp3.ResponseBody> req = service.postImage(body);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                    try {
                        httpRequest(response);
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    public void openPlaylist(String mood){
        Uri uri;
        Intent intent;

        if (mood.equals("happy")){
            uri = Uri.parse("https://play.spotify.com/user/myplay.com/playlist/1h90L3LP8kAJ7KGjCV2Xfd?play=true&utm_source=open.spotify.com&utm_medium=open");
        }
        else if (mood.equals("sad")){
            uri = Uri.parse("https://play.spotify.com/user/sanik007/playlist/7ABD15iASBIpPP5uJ5awvq?play=true&utm_source=open.spotify.com&utm_medium=open");
        }
        else if (mood.equals("angry")){
            uri = Uri.parse("https://play.spotify.com/user/digster.fm/playlist/5TSQxc8pYO0pALA92qGXSh");
        }
        else if (mood.equals("calm")){
            uri = Uri.parse("https://play.spotify.com/user/omagazine/playlist/54eyA9DewdUPaWRhvY7xYQ?play=true&utm_source=open.spotify.com&utm_medium=open");
        }
        else if (mood.equals("love")){
            uri = Uri.parse("https://play.spotify.com/user/unionsquaremusic/playlist/4x0ucOjg6M55edJ5UtHqzZ?play=true&utm_source=open.spotify.com&utm_medium=open");
        }
        else if (mood.equals("electronic")){
            uri = Uri.parse("https://play.spotify.com/user/mz1030/playlist/39XTbWTKsEFFqv3nPHSxf0?play=true&utm_source=open.spotify.com&utm_medium=open");
        }
        else{
            uri = Uri.parse("https://play.spotify.com/user/spotifycharts/playlist/37i9dQZEVXbMDoHDwVN2tF?play=true&utm_source=open.spotify.com&utm_medium=open");
        }
        intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void httpRequest(Response<ResponseBody> response) throws Exception{
        String s = response.body().string();
        System.out.println(s);
        JSONObject mainObject = new JSONObject(s);
        String mood = mainObject.getString("mood");
        openPlaylist(mood);
    }
}
