package com.kmit.findafriend.core;

import static com.kmit.findafriend.utils.Utils.apiEndpoint;
import static com.kmit.findafriend.utils.Utils.subscriptionKey;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.islamkhsh.CardSliderViewPager;
import com.google.android.material.navigation.NavigationView;
import com.kmit.findafriend.R;
import com.kmit.findafriend.cards.Match;
import com.kmit.findafriend.cards.MatchView;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.Person;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Dashboard extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    String currentUserHobbies;
    Uri imageUri;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    ProgressDialog detectionProgressDialog,identifyProgressDialog;
    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);
    TextView yourName, your_hobbies, person_hobbies, person_name;
    ImageView otherPerson;
    String personId;
    Button compareWith;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    NavigationView nv;
    LinearLayout resultlayout,initiallayout;
    ArrayList<Match> matches = new ArrayList<Match>();
    TextView no_of_matcing_hobbies;
    CardSliderViewPager cardSliderViewPager;
    Face[] identifiedfaces;
    Bitmap orgbitmap;
    String[] hobbies;
    JSONObject messagesuggestion=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        this.prefs = getApplicationContext().getSharedPreferences("face", 0);
        this.editor = this.prefs.edit();
        cardSliderViewPager = (CardSliderViewPager) findViewById(R.id.viewPager);
        Intent intent = getIntent();
        personId=(String)intent.getStringExtra("personid");
        if(personId== null) {
            try {
                personId = this.prefs.getString("loggedin", "");
            } catch (Exception ex) {
                System.out.println("Error");
            }
        }
        yourName =findViewById(R.id.your_name);
        your_hobbies =findViewById(R.id.your_hobbies);
        person_name =findViewById(R.id.person_name);
        person_hobbies =findViewById(R.id.person_hobbies);
        compareWith=findViewById(R.id.comparewith);
        otherPerson=findViewById(R.id.person_photo);
        resultlayout=findViewById(R.id.resultlayout);
        initiallayout=findViewById(R.id.initialscreen);
        no_of_matcing_hobbies =findViewById(R.id.no_of_matcing_hobbies);
        loadJSONFromAsset();
        loadJSONFromAssetSuggestion();
        compareWith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matches = new ArrayList<Match>();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File file = new File(directory,"friend.jpg");
                imageUri = FileProvider.getUriForFile(Dashboard.this, Dashboard.this.getApplicationContext().getPackageName() + ".provider", file);

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 7);
            }
        });

        fetchUser(personId,true,null);

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        nv=findViewById(R.id.navid);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                switch (id){
                    case R.id.nav_logout:
                        editor.putString("loggedin", "");
                        editor.commit();
                        Intent i = new Intent(Dashboard.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case R.id.profile:
                        Intent j = new Intent(Dashboard.this, Profile.class);
                        startActivity(j);
                        finish();
                        break;
                    case R.id.connectfriend:
                        Intent k = new Intent(Dashboard.this, Dashboard.class);
                        startActivity(k);
                        finish();
                        break;

                }
                return false;
            }
        });
        // to make the Navigation drawer icon always appear on the action bar
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }catch (Exception ex){
            alert("Exception",ex.getMessage());
        }


    }
    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"pic.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
    private Bitmap loadImageFromStorage()
    {

        try {

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f=new File(directory, "pic.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public final int PICTURE_RESULT=7;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case PICTURE_RESULT:
                if (requestCode == PICTURE_RESULT)
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            orgbitmap=MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), imageUri);
                            saveToInternalStorage(orgbitmap);
                            Bitmap bm = orgbitmap.copy(orgbitmap.getConfig(), true);
                            otherPerson.setImageBitmap(bm);
                            resultlayout.setVisibility(View.VISIBLE);
                            initiallayout.setVisibility(View.GONE);
                            compareWith.setText("RE-TAKE PHOTO");
                            detectFaces(bm);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
        }

    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    // Detect faces by uploading a face image.
    // Frame faces after detection.
    private void detectFaces(final Bitmap imageBitmap) {
        InputStream inputStream;
        try {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File initialFile=new File(directory, "friend.jpg");


            inputStream = new FileInputStream(initialFile);
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
                            identifiedfaces=result;
                            no_of_matcing_hobbies.setText("No. of persons: "+result.length);
                            if(result.length>0) {
                                for (Face f:result) {
                                    UUID[] u = new UUID[1];
                                    u[0] = f.faceId;
                                    findIfPersonExists(u,f);
                                }

                            }else if(result.length==0){
                                new AlertDialog.Builder(Dashboard.this).setTitle("Face not found").setMessage("Person may not have registered").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).show();
                            }
                            ImageView imageView = findViewById(R.id.person_photo);

                            imageView.setImageBitmap(imageBitmap);


                        }
                    };

            detectTask.execute(inputStream);
        }catch (Exception ex){
                ex.printStackTrace();
        }

    }
    public void loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = Dashboard.this.getAssets().open("hobbies.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            try {
                JSONArray j = new JSONArray(json);
                hobbies=new String[j.length()];
                for (int i=0;i<j.length();i++){
                    JSONObject b=(JSONObject) j.get(i);
                    hobbies[i]=b.getString("title");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void loadJSONFromAssetSuggestion() {
        String json = null;
        try {
            InputStream is = Dashboard.this.getAssets().open("messagesuggestion.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            try {
                messagesuggestion= new JSONObject(json);


            }catch (Exception ex){
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    // <snippet_detection_methods>
    private void findIfPersonExists(final UUID[] u, final Face f){
        if(identifyProgressDialog==null) {
            identifyProgressDialog = new ProgressDialog(this);
        }
        if(!identifyProgressDialog.isShowing()){
            identifyProgressDialog = new ProgressDialog(this);
        }
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
                            alert("User not found","No user exist with this face id");
                        }else {
                            fetchUser(result[0].candidates.get(0).personId.toString(),false,f);


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

    private Bitmap cropFaceRectangleOnBitmap(Face face) {

        Bitmap bitmap = loadImageFromStorage();

        FaceRectangle faceRectangle = face.faceRectangle;

        Bitmap resizedBmp = Bitmap.createBitmap(bitmap, faceRectangle.left,
                faceRectangle.top, faceRectangle.width, faceRectangle.height);

        return resizedBmp;
    }
    private void alert(String title,String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(Dashboard.this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }
    private Collection<String> compareHobbies(List<String> l1, List<String> l2){

        Collection<String> listOne = l1;

        Collection<String> listTwo = l2;

        Collection<String> similar = new HashSet<String>( listOne );
        Collection<String> different = new HashSet<String>();
        different.addAll( listOne );
        different.addAll( listTwo );

        similar.retainAll( listTwo );
        different.removeAll( similar );

        return similar;

    }
    private void fetchUser(String personId,boolean currentUser, Face f) {
        if(detectionProgressDialog ==null){
            detectionProgressDialog = new ProgressDialog(this);
        }
        if(!detectionProgressDialog.isShowing()){
            detectionProgressDialog = new ProgressDialog(this);
        }

        UUID u=UUID.fromString(personId);


        AsyncTask<UUID, String, Person> detectTask =
                new AsyncTask<UUID, String, Person>() {
                    String exceptionMessage = "";

                    @Override
                    protected Person doInBackground(UUID... params) {
                        try {
                            publishProgress("Logging in...");
                            Person result = faceServiceClient.getPerson("teleparadigm",params[0]);
                            if (result == null){
                                publishProgress(
                                        "Could not login");
                                return null;
                            }
//
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Login failed: %s", e.getMessage());
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
                    protected void onPostExecute(Person result) {

                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        if(currentUser) {
                            yourName.setText(result.name);
                            your_hobbies.setText(result.userData);
                            currentUserHobbies=result.userData;
                            cardSliderViewPager.setAdapter(new MatchView(matches));
                        }else{
                            person_name.setText(result.name);
                            person_hobbies.setText(result.userData);
                            String[] l1= your_hobbies.getText().toString().replaceAll("\\s", "").split(",");
                            String[] l2= person_hobbies.getText().toString().replaceAll("\\s", "").split(",");
                            List<String> l3= Arrays.asList(l1);
                            List<String> l4= Arrays.asList(l2);
                            Collection<String> similar=compareHobbies(l3,l4);
                            TextView txt1=findViewById(R.id.textView6);
                            txt1.setText("Matching hobbies ("+similar.size()+")" );
                            TextView txt=findViewById(R.id.textView12);
                            txt.setText(similar.toString());
                            ArrayList<String> convos=new ArrayList<>();

                            for (String s:
                                 similar) {
                                for (int i = 0; i< hobbies.length; i++){
                                    String k= hobbies[i];
                                    if(s.trim().equals(k)){
                                        try {
                                            JSONArray ss=messagesuggestion.getJSONArray(k);
                                            Random r=new Random();
                                            int randomNumber=r.nextInt(ss.length());
                                            convos.add(ss.get(randomNumber).toString());
                                        }catch (Exception ex){
                                        }

                                    }
                                }

                            }
                            TextView tx11=findViewById(R.id.textView11);
                            tx11.setText(convos.toString());
//
                            matches.add(new Match(result.name,l3,l4,similar,cropFaceRectangleOnBitmap(f),convos));
                            cardSliderViewPager.setAdapter(new MatchView(matches));


                        }


                    }
                };

        detectTask.execute(u);
    }
    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }
}