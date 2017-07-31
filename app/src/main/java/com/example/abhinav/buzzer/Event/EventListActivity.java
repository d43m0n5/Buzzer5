package com.example.abhinav.buzzer.Event;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhinav.buzzer.College.CollegeName;
import com.example.abhinav.buzzer.R;
import com.example.abhinav.buzzer.Timeline.PostActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    //view objects
    EditText editTextName;
    Button buttonAddEvent;
    ListView listViewEvent;
    Toolbar mToolbar;
    AdView mAdView;
    private Button postTime;
    int year_x,month_x,day_x;
    static final int DIALOG_ID = 0;
    private TextView eventDate;


    //a list to store all the artist from firebase database
    List<EventName> cName;

    DatabaseReference databaseEvent;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        final String clgID = getIntent().getExtras().getString("colgId");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);

        databaseEvent = FirebaseDatabase.getInstance().getReference().child(clgID).child("Event");

        //getting views
        editTextName = (EditText) findViewById(R.id.editTextName);
        listViewEvent = (ListView) findViewById(R.id.listViewArtists);
        buttonAddEvent = (Button) findViewById(R.id.buttonAddCollege);
        eventDate = (TextView) findViewById(R.id.eventDate);
        postTime = (Button) findViewById(R.id.post_time);

        final Calendar cal = Calendar.getInstance();
        year_x =  cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);



        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("Event Names");

        //list to store artists
        cName = new ArrayList<>();

        postTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //the method is defined below
                //this method is actually performing the write operation
                addCollege();
            }
        });

        listViewEvent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //getting the selected artist
                EventName artist = cName.get(i);

                Intent setup=new Intent(EventListActivity.this,PostActivity.class);
                   setup.putExtra("EventName",artist.getEventName());
                   setup.putExtra("EventId",artist.getEventID());
                   setup.putExtra("EventDate", artist.getEventDate());
                   setup.putExtra("colgId", clgID);
                   setup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(setup);

                //starting the activity with intent
            }
        });

        listViewEvent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                EventName clg_name = cName.get(i);
                showUpdateDeleteDialog(clg_name.getEventID(), clg_name.getEventName());
                return true;
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID)
            return new DatePickerDialog(this, dpickerListner , year_x , month_x , day_x);
        else
            return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_x = year;
            month_x = month + 1;
            day_x = dayOfMonth;
            eventDate.setText(day_x+"/"+month_x+"/"+year_x);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        databaseEvent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                 cName.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    EventName artist = postSnapshot.getValue(EventName.class);
                    cName.add(artist);
                }

                //creating adapter
                EventActivity collegeAdapter = new EventActivity(EventListActivity.this, cName);
                //attaching adapter to the listview
                listViewEvent.setAdapter(collegeAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addCollege() {
        String name = editTextName.getText().toString().trim();
        String date = eventDate.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(date)) {

            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Artist
            String id = databaseEvent.push().getKey();

            EventName clg_name = new EventName(id ,name ,date);

            databaseEvent.child(id).setValue(clg_name);

            //setting edit text to blank again
            editTextName.setText("");

            //displaying a success toast
            Toast.makeText(this, "Event added", Toast.LENGTH_LONG).show();
        } else {
            //if the value is not given displaying a toast
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
        }
    }

    private boolean updateCollege(String id, String name) {
        //getting the specified artist reference
        final String clgID = getIntent().getExtras().getString("colgId");
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child(clgID).child("Event").child(id);

        //updating artist
        CollegeName clg_name = new CollegeName(id, name,"","");
        dR.setValue(clg_name);
        Toast.makeText(getApplicationContext(), "College Updated", Toast.LENGTH_LONG).show();
        return true;
    }

    private void showUpdateDeleteDialog(final String collegeId, String collegeName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.name_update, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateArtist);

        dialogBuilder.setTitle(collegeName);
        final AlertDialog b = dialogBuilder.create();
        b.show();


        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) {
                    updateCollege(collegeId, name);
                    b.dismiss();
                }
            }
        });

    }
}