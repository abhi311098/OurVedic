package com.abhi.ourvedic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Billing_Details extends AppCompatActivity {

    private String TAG = "errorres";
    Button confirmorder;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    ArrayList<item> item_cart_copy2;
    int total = 0;
    String email;
    String name;
    String mob;
    TextView billing_name;
    TextView billing_number;
    TextView billing_price;
    TextView billing_delivery_charges;
    TextView billing_amount_final;
    TextView billing_address;
    ImageView edit_billing;
    Vibrator vibrator;

    String h_no,area,pincode,street,land;

    DatabaseReference myCartRef = database.getReference("users").child(user.getUid()).child("user_cart");
    DatabaseReference myProfileRef = database.getReference("users").child(user.getUid()).child("user_profile");
    DatabaseReference myHistoryRef = database.getReference("users").child(user.getUid()).child("user_orderHistory");
    DatabaseReference adminRef = database.getReference("Admin").child("current_orders");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing__details);
        vibrator = (Vibrator)getApplicationContext().getSystemService(Billing_Details.VIBRATOR_SERVICE);

        confirmorder = findViewById(R.id.confirmqqorder);
        billing_price = findViewById(R.id.billing_price);
        billing_address = findViewById(R.id.billing_address);
        billing_name = findViewById(R.id.billing_name);
        billing_number = findViewById(R.id.billing_number);
        billing_delivery_charges = findViewById(R.id.billing_delivery_charges);
        billing_amount_final = findViewById(R.id.billing_amount_final);

        edit_billing = findViewById(R.id.edit_billing);
        edit_billing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                  //////////for abhishek
            }
        });

        item_cart_copy2 = new ArrayList<>();

        myProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    email = snapshot.child("email").getValue(String.class);
                    h_no = snapshot.child("house").getValue(String.class);
                    street = snapshot.child("street").getValue(String.class);
                    area = snapshot.child("area").getValue(String.class);
                    land = snapshot.child("land").getValue(String.class);
                    pincode = snapshot.child("pincode").getValue(String.class);
                    name = snapshot.child("name").getValue(String.class);
                    billing_name.setText(name);
                    mob = snapshot.child("number").getValue(String.class);
                    billing_number.setText(mob);
                    billing_address.setText(h_no+", "+land+", "+street+", "+area+", "+pincode);
                } else {
                    billing_address.setText("Go To Profile Section And Complete Your Profile First");
                    Toast.makeText(Billing_Details.this, "No Address Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Billing_Details.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        myCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dss : snapshot.getChildren()){
                        item i = dss.getValue(item.class);
                        item_cart_copy2.add(i);
                        total += i.getItem_Price();
                        billing_price.setText("₹" + String.valueOf(total));
                        billing_amount_final.setText("₹" + String.valueOf(total));
                        BillingAdapter billingAdapter = new BillingAdapter(Billing_Details.this, item_cart_copy2);
                        ListView billing_lv = findViewById(R.id.billing_lv);
                        billing_lv.setAdapter(billingAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Billing_Details.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        confirmorder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    StringBuilder s = new StringBuilder();
                    for(int i=0; i<item_cart_copy2.size(); i++){
                        s.append(item_cart_copy2.get(i).getItem_id()+ "-");
                    }

                    DateFormat dateFormat = new SimpleDateFormat("KK:mm:ss a, dd/MM/yyyy", Locale.getDefault());
                    String currentDateAndTime = dateFormat.format(new Date());

                    String address = h_no+", "+land+", "+street+", "+area+", "+pincode;
                    order_details o = new order_details(name, email, String.valueOf(s), address, mob, total, "cash On Delivery", currentDateAndTime, "NA");

                    Log.v("Tags", o.getName() + "-" + o.getEmail() + "-" + o.getItemIds() + "-" + o.getDelivery_address() + "-" + o.getMobile() + "-" + total + "-" + o.getMode_of_payment() + "-" + o.getDelivered_date_time() + "-" + o.getDelivered_date_time());

                    myHistoryRef.push().setValue(o).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG, "History Success: done" );
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "History Failure: fail: "+e.getMessage());
                        }
                    });

                    adminRef.push().setValue(o).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG, "Current Order Success: done" );
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Current Order Failure: fail: "+e.getMessage());
                        }
                    });
                    vibrator.vibrate(500);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }

                Task<Void> myHistoryRef = database.getReference("users").child(user.getUid()).child("user_cart").removeValue();
                startActivity(new Intent(Billing_Details.this,Splash_Screen2.class));
                finish();
            }
        });
    }
}
