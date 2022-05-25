package com.kmit.findafriend.core;

import static com.kmit.findafriend.utils.Utils.apiEndpoint;
import static com.kmit.findafriend.utils.Utils.subscriptionKey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.kmit.findafriend.R;
import com.kmit.findafriend.chip.HobbyChip;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Profile extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    List<HobbyChip> hobbyList = new ArrayList<>();
    ProgressDialog detectionProgressDialog,identifyProgressDialog,editingSaved;
    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);
    TextView selectHobbies;
    TextView viewname, viewhobbies,edithobbies;
    EditText editname;
    String personId;
    Button editprofile,saveprofile;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    NavigationView nv;
    LinearLayout resultlayout,initiallayout;
    LinearLayout editmode,viewmode;
    String loggedin;
    boolean[] selectedhobbies;
    boolean editedphoto,isEditmode=false;
    ArrayList<Integer> langList = new ArrayList<>();
    ImageView photoedit;
    Bitmap bitmap;
    ChipsInput chipsInput,chipsInputReadonly;
    ChipGroup gchip;
    String[] hobbies;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        selectHobbies = findViewById(R.id.textViewa);
        this.prefs = getApplicationContext().getSharedPreferences("face", 0);
        this.editor = this.prefs.edit();
        Intent intent = getIntent();
        personId=(String)intent.getStringExtra("personid");
        if(personId==null) {
            try {
                personId = this.prefs.getString("loggedin", "");
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }
        photoedit=findViewById(R.id.imageView2);
        viewname =findViewById(R.id.person_name);
        viewhobbies =findViewById(R.id.person_hobbies);
        editname =findViewById(R.id.editTextTextPersonNamea);
        edithobbies =findViewById(R.id.textViewa);
        editmode=findViewById(R.id.editmode);
        viewmode=findViewById(R.id.viewmode);
        editprofile=findViewById(R.id.editprofile);
        saveprofile=findViewById(R.id.saveprofile);
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile();
            }
        });
        saveprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEditedData();
            }
        });
        fetchUser(personId,true);
        loadImageFromStorage();

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
                        Intent i = new Intent(Profile.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case R.id.profile:
                        Intent j = new Intent(Profile.this, Profile.class);
                        startActivity(j);
                        finish();
                        break;
                    case R.id.connectfriend:
                        Intent k = new Intent(Profile.this, Dashboard.class);
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

            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#351c75"));
            // getSupportActionBar().setBackgroundDrawable(colorDrawable);

        }catch (Exception ex){
            alert("Exception",ex.getMessage());
        }
        photoedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEditmode){
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 7);
                }
            }
        });

    }
    public void loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = Profile.this.getAssets().open("hobbies.json");
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
    private void addHobbiesToList() {
        // get ChipsInput view
        chipsInput = (ChipsInput) findViewById(R.id.chips_input);
        chipsInputReadonly =(ChipsInput) findViewById(R.id.chips_input_readonly);
        gchip=(ChipGroup) findViewById(R.id.chip_group_filter);

        String hobbieschoosen=viewhobbies.getText().toString();
        String[] chips=hobbieschoosen.split(",");
        for (int i=0;i<chips.length;i++) {
            chipsInputReadonly.addChip(new HobbyChip(chips[i].trim()));
        }


        hobbyList=new ArrayList<>();
        loadJSONFromAsset();
        for (int i=0;i<hobbies.length;i++) {
            hobbyList.add(new HobbyChip(hobbies[i].trim()));
        }
        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                //chipsInput.setFilterableList(hobbyList);
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
               // chipsInput.setFilterableList(hobbyList);
            }

            @Override
            public void onTextChanged(CharSequence text) {
                //chipsInput.setFilterableList(hobbyList);
            }
        });
        chipsInput.setFilterableList(hobbyList);
        chipsInput.setMaxRows(20);
        chipsInputReadonly.setMaxRows(20);
        chipsInputReadonly.clearFocus();


    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
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
    private void loadImageFromStorage()
    {

        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f=new File(directory, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            photoedit.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7 && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            //saveToInternalStorage(bitmap);
            photoedit.setImageBitmap(bitmap);

            detectFaces(bitmap);
        }
    }
    // Detect faces by uploading a face image.
    // Frame faces after detection.
    private void detectFaces(final Bitmap imageBitmap) {
        detectionProgressDialog = new ProgressDialog(this);
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

                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        matchPersonPhoto(result[0]);


                    }
                };

        detectTask.execute(inputStream);
    }
    //
    private void matchPersonPhoto(final Face faceId) {
        identifyProgressDialog = new ProgressDialog(this);


        AsyncTask<String, String, VerifyResult> detectTask =
                new AsyncTask<String, String, VerifyResult>() {
                    String exceptionMessage = "";

                    @Override
                    protected VerifyResult doInBackground(String... params) {
                        try {
                            publishProgress("Identifying...");
                            VerifyResult result=faceServiceClient.verify(faceId.faceId,"teleparadigm",UUID.fromString(personId));
                            if (result == null){
                                publishProgress(
                                        "Identifying Finished. Nothing detected");
                                return null;
                            }

                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Identifying failed: %s", e.getMessage());
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
                    protected void onPostExecute(VerifyResult result) {

                        //TODO: update face frames
                        identifyProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        if(result.isIdentical){
                            saveToInternalStorage(bitmap);
                        }else{
                            showError("Please click the photo of current user only");
                        }


                    }
                };

        detectTask.execute();
    }
    private void saveEditedData() {
        editingSaved = new ProgressDialog(this);


        AsyncTask<String, String, Void> detectTask =
                new AsyncTask<String, String, Void>() {
                    String exceptionMessage = "";

                    @Override
                    protected Void doInBackground(String... params) {
                        try {
                            publishProgress("Updating data...");
                            List<HobbyChip> hobbiesselected = (List<HobbyChip>) chipsInput.getSelectedChipList();
                            String s="";
                            for(int i=0;i<hobbiesselected.size();i++){
                                if(i==hobbiesselected.size()-1){
                                    s+=hobbiesselected.get(i).getLabel();
                                    break;
                                }
                                s+=hobbiesselected.get(i).getLabel()+", ";
                            }
                            faceServiceClient.updatePerson("teleparadigm",UUID.fromString(personId),editname.getText().toString(),s);//,edithobbies.getText().toString());

                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Update failed: %s", e.getMessage());

                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        editingSaved.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        editingSaved.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Void result) {
                        Intent i = new Intent(Profile.this, Profile.class);
                        startActivity(i);
                        finish();
                        //TODO: update face frames
                        editingSaved.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;



                    }
                };

        detectTask.execute();
    }
    private void editProfile(){

        isEditmode=true;
        editmode.setVisibility(View.VISIBLE);
        viewmode.setVisibility(View.GONE);
        editname.setText(viewname.getText().toString());
        edithobbies.setText(viewhobbies.getText());
        String hobbieschoosen=viewhobbies.getText().toString();
        String[] chips=hobbieschoosen.split(",");
        for (int i=0;i<chips.length;i++) {
            chipsInput.addChip(new HobbyChip(chips[i]));
        }


    }


    private void alert(String title,String message){
        AlertDialog.Builder alert=new AlertDialog.Builder(Profile.this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }
    private void fetchUser(String personId,boolean currentUser) {
        detectionProgressDialog = new ProgressDialog(this);
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
                            viewname.setText(result.name);
                            viewhobbies.setText(result.userData);
                            addHobbiesToList();
                        }else{

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