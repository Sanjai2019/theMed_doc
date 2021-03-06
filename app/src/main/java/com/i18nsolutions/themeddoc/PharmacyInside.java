package com.i18nsolutions.themeddoc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class PharmacyInside extends AppCompatActivity
{
    String pharmacyname,city1,uidmed;
    FancyButton upload;
    Uri imageuri;
    TextView filename,webpage;
    DatabaseReference databaseReference,databaseReference1,databaseReference2;
    FirebaseDatabase firebaseDatabase,firebaseDatabase1;
    ImageButton attachfiles;
    List<MedicineDetails> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Upload Medicines");
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_pharmacy_inside);
        attachfiles=findViewById(R.id.attachfiles);
        upload = findViewById(R.id.submitmed);
        filename=findViewById(R.id.filename);
        webpage=findViewById(R.id.webview);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        uidmed=FirebaseAuth.getInstance().getCurrentUser().getUid();
        filename.setText("choose a csv file");
        databaseReference2=FirebaseDatabase.getInstance().getReference().child("PharmacyRegistrations").child(uidmed);
        databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                city1=dataSnapshot.child("location").getValue().toString();
                pharmacyname=dataSnapshot.child("pharmname").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        attachfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               selectimage();
            }
        });
        webpage.setMovementMethod(LinkMovementMethod.getInstance());
        webpage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.zamzar.com/convert/xls-to-csv/"));
                startActivity(browserIntent);
            }
        });
    }
    public void selectimage()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK  && data.getData()!=null)
        {
            imageuri=data.getData();
            File file = new File(imageuri.toString());
            filename.setText(file.getName().substring(10));
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zzzzz();
                }
            });
        }
        else
        {
            Toast.makeText(PharmacyInside.this,"no media selected",Toast.LENGTH_SHORT).show();
        }
    }
    void zzzzz()
    {
        InputStream inputStream = null;
        try
        {
            System.out.println(">>>>>>>>>>>>>>>>>>"+imageuri);
            inputStream = getContentResolver().openInputStream(imageuri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        String line="";
        firebaseDatabase1=FirebaseDatabase.getInstance();
        databaseReference1=firebaseDatabase1.getReference().child("MedicineDetails").child(city1).child(uidmed);
        try {
            bufferedReader.readLine();
            while ((line=bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                MedicineDetails medicineDetails = new MedicineDetails();
                medicineDetails.serialno=Integer.parseInt(tokens[0]);
                medicineDetails.medicinename=tokens[1];
                medicineDetails.category=tokens[2];
                medicineDetails.medicinecategory=tokens[3];
                medicineDetails.money=tokens[4];
                medicineDetails.location=city1;
                medicineDetails.pharmacyname=pharmacyname;
                medicineDetails.uidmed=uidmed;
                medicineDetails.outofstock=false;
                list.add(medicineDetails);
                databaseReference1.child(tokens[0]+"").setValue(medicineDetails);
            }
            Toast.makeText(PharmacyInside.this,"Added Successfully",Toast.LENGTH_LONG).show();
            filename.setText("choose a csv file");
        }catch (Exception e){
            System.out.println(e);
        }
    }
}