package com.blogspot.aruncyberspace.attendance_management_system_student;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;


public class StudentMainActivity extends ActionBarActivity {
    String LOGTAG = "AMS-SMA";
    private ProgressBar mProgressView;
    private Button btnMarkAttendance,btnViewTopics;
    User user;
    Course currentCourse;
    String userXML;
    Collection<Course> enrolledCourses = new ArrayList<Course>();
    Collection<Course> ongoingCourses = new ArrayList<Course>();
    Spinner currentCourseTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        //get the xml from previous intent
        mProgressView = (ProgressBar) findViewById(R.id.login_progress);
        userXML = getIntent().getStringExtra("USER_XML");

        user = deserializeUserXML(userXML);
        if (user.getFirstName() != "") {
            TextView welcomeTextView;
            welcomeTextView = (TextView) findViewById(R.id.welcomeText);
            String name = user.getFirstName() +" " + user.getLastName();
            welcomeTextView.setText(name);

            AsyncCallWS task = new AsyncCallWS();
            task.execute();
        } else {//close this activity since you don't user information
            this.finish();
        }

        btnViewTopics = (Button) findViewById(R.id.ViewTopicsBtn);
        btnViewTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ViewTopics.class);
                intent.putExtra("USER_XML",userXML);
               startActivity(intent);
            }
        });
        btnMarkAttendance = (Button) findViewById(R.id.markAttendanceBtn);
        btnMarkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the QR code Generate Page
                Intent intent = new Intent(getApplicationContext(), QRCodeGenerateActivity.class);
                intent.putExtra("USER_XML",userXML);
                intent.putExtra("course_id",currentCourse.getCourseId());
                startActivity(intent);
            }
        });
        //parse User XML to User Class

    }

    protected User deserializeUserXML(String userXML) {
        User user = new User();
        Serializer serializer = new Persister();
        try {
            user = serializer.read(User.class, userXML);
        } catch (Exception ex) {
            String exceptionstring = ex.toString();
            exceptionstring.toString();
        }
        return user;
    }


    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_student_main, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();

         //noinspection SimplifiableIfStatement
         if (id == R.id.action_settings) {
             return true;
         }

         return super.onOptionsItemSelected(item);
     }*/
    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(LOGTAG, "doInBackground");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date nowdate = new Date();
            String dateString = format.format(new Date());
            try {

                Log.w("AMS SMA AT", nowdate.toString());
                Log.w("AMS SMA ATT", dateString.toString());

            } catch (Exception e) {
                Log.w("AMS SM", e.toString());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
            }

            Date startDate = new Date(),
                    endDate = new Date();
            //get all courses
            enrolledCourses = user.getCourses();

            for (Course c : enrolledCourses) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                try {
                    startDate = df.parse( c.getCourseStartTime());
                    endDate =df.parse( c.getCourseEndTime());

                    Log.w("AMS", "startDate: " + startDate.toString());
                    Log.w("AMS", "EndDate: " + endDate.toString());

                } catch (Exception e) {
                    Log.w("AMS", e.toString());
                }

                if (nowdate.after(startDate) && nowdate.before(endDate)) {
                    ongoingCourses.add(c);
                    Log.w("AMS","Course is "+c.getCourseName());
//                    break;
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Log.i(LOGTAG, "onPostExecute");

            mProgressView.setVisibility(View.GONE);
            currentCourseTV = (Spinner) findViewById(R.id.currentCourse);
            final Button btnMarkAttendance = (Button) findViewById(R.id.markAttendanceBtn);
            // set the current course if found
            ArrayList<String> c = new ArrayList<>();
            for(Course cs : ongoingCourses){
                c.add(cs.getCourseName());
            }
            ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),android.R.layout.simple_list_item_1,c);
//            if (currentCourse != null) {
//
//                currentCourseTV.setText(currentCourse.getCourseName());
//                btnMarkAttendance.setEnabled(true);
//            } else {
//                currentCourseTV.setText("(No Ongoing Course)");
//                btnMarkAttendance.setEnabled(false);
//            }
            if(c.size()>0) {
                currentCourseTV.setAdapter(adapter);
                btnMarkAttendance.setVisibility(View.VISIBLE);
                currentCourseTV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                              for(Course cs : ongoingCourses){
                        if(cs.getCourseName().equals(currentCourseTV.getSelectedItem().toString())){
                            currentCourse = cs;
                        }
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
              //  btnMarkAttendance.setClickable(true);
            }
            else{
                Toast.makeText(getApplicationContext(),"No Ongoing Courses",Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onPreExecute() {
            Log.i(LOGTAG, "onPreExecute");
            mProgressView.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(LOGTAG, "onProgressUpdate");
            mProgressView.animate();
        }

    }
}
