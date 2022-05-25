package com.kmit.findafriend.core;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kmit.findafriend.R;
import com.kmit.findafriend.chip.HobbyChip;
import com.kmit.findafriend.utils.Utils;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.pchmn.materialchips.ChipsInput;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Registration extends AppCompatActivity {
    // initialize variables
    TextView selectHobbies,editTextTextPersonName;
    ImageView im;
    Button register;
    ProgressDialog addingFace;
    List<HobbyChip> hobbyList = new ArrayList<>();
    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(Utils.apiEndpoint, Utils.subscriptionKey);
    private ProgressDialog detectionProgressDialog;
    ChipsInput chips_input_hobbies;
    String[] hobbies_json;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);
        chips_input_hobbies=findViewById(R.id.chips_input_hobbies);
        //initView();
        // assign variable
        selectHobbies = findViewById(R.id.textView);

        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
        im = findViewById(R.id.registerimage);
        register=findViewById(R.id.registerbutton);
        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
        im.setImageBitmap(bitmap);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextTextPersonName.getText().length()<3) {
                    Toast.makeText(Registration.this,"Please enter your name.",Toast.LENGTH_LONG).show();
                    return;

                }
                List<HobbyChip> hobbiesselected = (List<HobbyChip>) chips_input_hobbies.getSelectedChipList();
                String s="";
                for(int i=0;i<hobbiesselected.size();i++){
                    if(i==hobbiesselected.size()-1){
                        s+=hobbiesselected.get(i).getLabel();
                        break;
                    }
                    s+=hobbiesselected.get(i).getLabel()+", ";
                }

                if(s.equals("")){
                    Toast.makeText(Registration.this,"Please select hobbies.",Toast.LENGTH_LONG).show();
                }else {
                    registerUser();
                }
            }
        });

        addSuggestionChips();
    }

    private void addSuggestionChips() {
        hobbyList=new ArrayList<>();
        loadJSONFromAsset();
        for (int i=0;i<hobbies_json.length;i++) {
            hobbyList.add(new HobbyChip(hobbies_json[i].trim()));
        }
        chips_input_hobbies.setFilterableList(hobbyList);

    }

    public void loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = Registration.this.getAssets().open("hobbies.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            try {
                JSONArray j = new JSONArray(json);
                hobbies_json=new String[j.length()];

                for (int i=0;i<j.length();i++){
                    JSONObject b=(JSONObject) j.get(i);
                    hobbies_json[i]=b.getString("title");

                }

            }catch (Exception ex){
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();

        }

    }




    private void trainPersonGroup(){
        AsyncTask<String,String,String> trainTask=new AsyncTask<String, String, String>() {
                    String exceptionMessage = "";

                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            System.out.println("Training Person group Teleparadigm");
                            publishProgress("Training...");
                            faceServiceClient.trainPersonGroup(params[0]);

                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
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
                    protected void onPostExecute(String result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;


                    }
                };

        trainTask.execute("teleparadigm");
    }
    private void addFaceToRegisterUser(final UUID personId) {
        addingFace=new ProgressDialog(this);
        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, AddPersistedFaceResult> addFaceTask =
                new AsyncTask<InputStream, String, AddPersistedFaceResult>() {
                    String exceptionMessage = "";

                    @Override
                    protected AddPersistedFaceResult doInBackground(InputStream... params) {
                        try {
                            publishProgress("Adding Face...");
                            AddPersistedFaceResult result = faceServiceClient.addPersonFace("teleparadigm",personId,params[0],null,null);
                            if (result == null){
                                publishProgress(
                                        "Could not add face to person.");
                                return null;
                            }

                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Failed to add face: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        addingFace.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        addingFace.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(AddPersistedFaceResult result) {

                        //TODO: update face frames
                        addingFace.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;

                        AlertDialog.Builder alert=new AlertDialog.Builder(Registration.this);
                        alert.setMessage("Registration successful");
                        alert.setCancelable(false);
                        alert.setTitle("Registration");
                        alert.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent(Registration.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                        );
                        trainPersonGroup();
                        alert.show();
                    }
                };

        addFaceTask.execute(inputStream);
    }
    private void registerUser() {
        detectionProgressDialog = new ProgressDialog(this);


        AsyncTask<String, String, CreatePersonResult> detectTask =
                new AsyncTask<String, String, CreatePersonResult>() {
                    String exceptionMessage = "";

                    @Override
                    protected CreatePersonResult doInBackground(String... params) {
                        try {
                            publishProgress("Creating...");
                            List<HobbyChip> hobbiesselected = (List<HobbyChip>) chips_input_hobbies.getSelectedChipList();
                            String s="";
                            for(int i=0;i<hobbiesselected.size();i++){
                                if(i==hobbiesselected.size()-1){
                                    s+=hobbiesselected.get(i).getLabel();
                                    break;
                                }
                                s+=hobbiesselected.get(i).getLabel()+", ";
                            }

                            CreatePersonResult result = faceServiceClient.createPerson("teleparadigm",editTextTextPersonName.getText().toString(),s);//selectedHobbies);// selectHobbies.getText().toString());
                            if (result == null){
                                publishProgress(
                                        "Could not create person");
                                return null;
                            }
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Person creation failed: %s", e.getMessage());
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
                    protected void onPostExecute(CreatePersonResult result) {

                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        addFaceToRegisterUser(result.personId);


                    }
                };

        detectTask.execute();
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