package com.i18nsolutions.themeddoc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import mehdi.sakout.fancybuttons.FancyButton;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class LabRegistration extends AppCompatActivity
{
    de.hdodenhof.circleimageview.CircleImageView Profile,dp;
    public Uri imageuri;
    EditText labname,email,propreitorname,isonumber,workinghours,phonenumber,addressfulllab;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private UploadTask uploadtask;
    StorageReference imageref;
    String uid;
    double lats,longs;
    ProgressDialog pd;
    String labname1,mail,location1,propreitorname1,isonumber1,address1,workinghours1,phonenumber1;
    ImageButton addlocationlab;
    LocationManager locationManager;
    FancyButton labnext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_lab_registration);
        Profile=findViewById(R.id.profilelab);
        pd=new ProgressDialog(LabRegistration.this);
        dp=findViewById(R.id.dplab);
        labname=findViewById(R.id.labnamereg);
        propreitorname=findViewById(R.id.proprietornamereg);
        isonumber=findViewById(R.id.isonumberreg);
        labnext=findViewById(R.id.labnextbt);
        workinghours=findViewById(R.id.workinghoursreg);
        phonenumber=findViewById(R.id.phonereg);
        email=findViewById(R.id.maillab);
        addressfulllab=findViewById(R.id.fulladdresslab);
        addlocationlab=findViewById(R.id.addlocationlab);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(LabRegistration.this, "LabRegistration");
        sequence.setConfig(config);
        sequence.addSequenceItem(Profile,"Register as laboratory,add your profile too", "GOT IT");
        sequence.addSequenceItem(addlocationlab, "Add your location and you should be in your lab location for first time of registration", "GOT IT");
        sequence.start();
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        uid=user.getUid();
        imageref= FirebaseStorage.getInstance().getReference("lab_profile");
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference().child("LaboratoryRegistrations");
        addlocationlab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LabRegistration.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else
                {
                    new AlertDialog.Builder(LabRegistration.this)
                            .setTitle("Important")
                            .setMessage("Add your Hospital location")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface arg0, int arg1)
                                {
                                    getCurrentLocation();
                                    addlocationlab.setBackgroundColor(Color.GREEN);
                                    Toast.makeText(getApplicationContext(),"Added successfully",Toast.LENGTH_SHORT).show();
                                    arg0.cancel();
                                }
                            }).create().show();
                }
            }
        });
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectimage();
            }
        });
        labnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Loading...");
                pd.show();
                final String androiid= Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                if(isValid())
                {
                    StorageReference ref = imageref.child(androiid).child(getextension(imageuri)+"#");
                    if(imageuri==null)
                    {
                        imageuri= Uri.parse("android.resource://com.i18nsolutions.themeddoc/drawable/noimg");
                    }
                    uploadtask = ref.putFile(imageuri);
                    uploadtask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LabRegistration.this,"failed",Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(LabRegistration.this,"success" ,Toast.LENGTH_SHORT).show();
                        imageref.child(androiid).child(getextension(imageuri)+"#").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String profile_pic=task.getResult().toString();
                                SharedPreferences sharedPreferences=getSharedPreferences("MyPrefs",MODE_PRIVATE);
                                SharedPreferences.Editor editor1=sharedPreferences.edit();
                                editor1.putString("uid",uid);
                                editor1.commit();
                                Geocoder geocoder = new Geocoder(LabRegistration.this, Locale.getDefault());

                                List<Address> addresses  = null;
                                try {
                                    addresses = geocoder.getFromLocation(lats,longs, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String address = addresses.get(0).getAddressLine(0);
                                String city = addresses.get(0).getSubAdminArea();
                                LaboratoryRegistrationDetails laboratoryRegistrationDetails=new LaboratoryRegistrationDetails(labname1,mail,city.toLowerCase(),propreitorname1,isonumber1,"lab",profile_pic,address1,phonenumber1,workinghours1,uid,1f,lats,longs,0,false);
                                databaseReference.child(uid).setValue(laboratoryRegistrationDetails);
                                SharedPreferences.Editor editor2=sharedPreferences.edit();
                                editor2.putString("labname",labname1);
                                editor2.putString("location",city.toLowerCase());
                                editor2.commit();
                                sharedPreferences=getSharedPreferences("labordoc", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                //editor.putString("prefs","");
                                editor.putString("prefs","lab");
                                editor.commit();
                                pd.dismiss();
                                startActivity(new Intent(LabRegistration.this,LabDashboard.class));
                            }
                        });

                    });
                }
                else
                    {
                    pd.dismiss();
                    Toast.makeText(LabRegistration.this, "No null values!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void selectimage()
    {
        Intent intent=new Intent();
        // ivc.setVisibility(View.VISIBLE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }
    boolean isValid()
    {
        boolean i=false;
        labname1=labname.getText().toString();
        mail=email.getText().toString();
        propreitorname1=propreitorname.getText().toString();
        isonumber1=isonumber.getText().toString();
        workinghours1=workinghours.getText().toString();
        phonenumber1=phonenumber.getText().toString();
        address1=addressfulllab.getText().toString();
        if(labname1.isEmpty() ||mail.isEmpty()|| propreitorname1.isEmpty() || isonumber1.isEmpty() || workinghours1.isEmpty() || phonenumber1.isEmpty() || lats==0.0 && longs==0.0 || address1.isEmpty())
        {
            if(labname1.isEmpty())
                labname.setError("Can't be empty");
            if(mail.isEmpty())
                email.setError("Can't be empty");
            if(propreitorname1.isEmpty())
                propreitorname.setError("Can't be empty");
            if(isonumber1.isEmpty())
                isonumber.setError("Can't be empty");
            if(workinghours1.isEmpty())
                workinghours.setError("Can't be empty");
            if(phonenumber1.isEmpty())
                phonenumber.setError("Can't be empty");
            if(address1.isEmpty())
                addressfulllab.setError("Can't be empty");
        }
        else
            i=true;
        return i;
    }
    private String getextension(Uri uri)
    {
        ContentResolver cr=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        if(uri==null)
        {
            uri= Uri.parse("android.resource://com.example.ehospital/drawable/noimg");
            return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
        }
        else
        {
            return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK  && data.getData()!=null)
        {imageuri=data.getData();

            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                imageuri);
                Profile.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(LabRegistration.this,"no media selected",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getCurrentLocation();
        }
    }

    void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(LabRegistration.this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(LabRegistration.this).removeLocationUpdates(this);
                if(locationResult!=null && locationResult.getLocations().size()>0)
                {
                    int latestlocationindex=locationResult.getLocations().size()-1;
                    lats=locationResult.getLocations().get(latestlocationindex).getLatitude();
                    longs=locationResult.getLocations().get(latestlocationindex).getLongitude();
                }
            }
        }, Looper.getMainLooper());
    }
}