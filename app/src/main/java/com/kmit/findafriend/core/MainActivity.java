package com.kmit.findafriend.core;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
// <snippet_imports>
import java.io.*;
import java.util.UUID;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;
// </snippet_imports>

// <snippet_face_imports>
import com.kmit.findafriend.R;
import com.kmit.findafriend.utils.Utils;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;


import androidx.core.app.ActivityCompat;
// </snippet_face_imports>

public class MainActivity extends Activity {
    public static final int RequestPermissionCode = 1;
    // <snippet_mainactivity_fields>
    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(Utils.apiEndpoint, Utils.subscriptionKey);
    private final int PICK_IMAGE = 1;
    Button btnTakePhoto,btnsls;
    ImageView imageView;
    private ProgressDialog detectionProgressDialog,identifyProgressDialog;
    private Bitmap bitmapimage;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    // </snippet_mainactivity_fields>
    String loggedin="";
    // <snippet_drawrectangles>
    private static Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    // <snippet_mainactivity_methods>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kmit.findafriend.R.layout.activity_main);

        btnTakePhoto = (Button) findViewById(com.kmit.findafriend.R.id.capture);
        imageView = (ImageView) findViewById(com.kmit.findafriend.R.id.imageView1);
        btnsls =(Button) findViewById(com.kmit.findafriend.R.id.compare);
        btnsls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        EnableRuntimePermission();
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 7);
            }
        });
        Button button1 = findViewById(com.kmit.findafriend.R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select Picture"), PICK_IMAGE);
            }
        });

        detectionProgressDialog = new ProgressDialog(this);
        this.prefs = getApplicationContext().getSharedPreferences("face", 0);
        this.editor = this.prefs.edit();
        try {
            loggedin = this.prefs.getString("loggedin", "");
            if(!loggedin.equals("")){
                Intent i = new Intent(MainActivity.this, Dashboard.class);
                i.putExtra("personid",loggedin);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public void EnableRuntimePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA)) {
            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app",     Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] result) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (result.length > 0 && result[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7 && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);

            detectFaces(bitmap);
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);

                ImageView imageView = findViewById(com.kmit.findafriend.R.id.imageView1);
                imageView.setImageBitmap(bitmap);

                detectFaces(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // </snippet_mainactivity_methods>
    }
    // <snippet_detection_methods>
    private void findIfPersonExists(final UUID[] u){
        identifyProgressDialog = new ProgressDialog(this);
        AsyncTask<UUID[], String, IdentifyResult[] > identifyPerson =
                new AsyncTask<UUID[], String, IdentifyResult[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected IdentifyResult[] doInBackground(UUID[]... params) {
                        try {
                            publishProgress("Identifying...");

                            IdentifyResult[] result=faceServiceClient.identity("teleparadigm",params[0],10);
                            if (result == null){

                                publishProgress(
                                        "Identifying Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(String.format(
                                    "Identifying Finished. %d face(s) detected",
                                    result.length));
                            System.err.println(String.format(
                                    "Identifying Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Identifying failed: %s", e.getMessage());
                            System.err.println(exceptionMessage);
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        identifyProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        identifyProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(IdentifyResult[] result) {

                        if(result[0].candidates.size()==0){
                            Intent i = new Intent(MainActivity.this, Registration.class);

                            i.putExtra("BitmapImage", bitmapimage);
                            startActivity(i);
                            finish();
                        }else {
                            editor.putString("loggedin", result[0].candidates.get(0).personId.toString());
                            editor.commit();
                            Intent i = new Intent(MainActivity.this, Dashboard.class);
                            i.putExtra("personid",result[0].candidates.get(0).personId.toString());
                            i.putExtra("BitmapImage", bitmapimage);
                            startActivity(i);
                            finish();
                        }
                        for (IdentifyResult a:result) {

                            for (Candidate c:a.candidates
                                 ) {

                                System.err.println(c.personId);

                            }
                        }
                        //TODO: update face frames
                        identifyProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;


                    }
                };

        identifyPerson.execute(u);

    }

    // Detect faces by uploading a face image.
    // Frame faces after detection.
    private void detectFaces(final Bitmap imageBitmap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null          // returnFaceAttributes:
                                    /* new FaceServiceClient.FaceAttributeType[] {
                                        FaceServiceClient.FaceAttributeType.Age,
                                        FaceServiceClient.FaceAttributeType.Gender }
                                    */
                            );
                            if (result == null){
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //

                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        UUID[] u=new UUID[1
                                ];
                        u[0]=result[0].faceId;
                        findIfPersonExists(u);
                        ImageView imageView = findViewById(R.id.imageView1);
                        imageView.setImageBitmap(
                                drawFaceRectanglesOnBitmap(imageBitmap, result));
                        bitmapimage=imageBitmap.copy(imageBitmap.getConfig(),true);
                        imageBitmap.recycle();

                    }
                };

        detectTask.execute(inputStream);
    }
    // </snippet_detection_methods>

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }
    // </snippet_drawrectangles>
}
