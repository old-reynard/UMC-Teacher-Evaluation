package com.umc.admin.umcteachereval;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.umc.admin.umcteachereval.models.Teacher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This app has been developed for Upper Madison College by Kit Gerasimov and is meant to be used by
 * admin staff in order to manage the student-side app (UMC Teacher Evaluation written in Dart for
 * iOS and Android). This app, Teacher Eval Admin can perform the following:
 * - extract marks given by UMC students to their teachers for performance from a Firestore server
 * - sort those marks by teacher, date, sender and period and set pack them in a XLS report
 * - send the report by email through an intent
 * - create, delete and edit list of active UMC teachers and their emails
 * - create an XLS report with UMC facilities evaluation
 * - create a report with students' arbitrary feedback
 * todo
 * - send emails to teachers reminding them how many of their students have submitted evaluations
 *
 * The app uses Firebase Auth for authentication
 */
public class MainActivity extends AppCompatActivity {

    /* Firebase instance variables */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    /* Auth request code for activity for result */
    private static final int RC_SIGN_IN = 123;

    public static final List<Teacher> teachers = new ArrayList<>();
    /* Main simply allows to choose on of the main functions */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initialise Firebase components */
        mAuth = FirebaseAuth.getInstance();

        Button sendReportsButton = findViewById(R.id.send_reports_button);
        sendReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendReportIntent = new Intent(MainActivity.this, SendReportsActivity.class);
                startActivity(sendReportIntent);
            }
        });

        Button editTeachersButton = findViewById(R.id.edit_teachers_button);
        editTeachersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(MainActivity.this, EditTeachersActivity.class);
                startActivity(editIntent);
            }
        });

        final Button sendRemindersButton = findViewById(R.id.send_reminders_button);
        sendRemindersButton.setEnabled(false);
        sendRemindersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reminderIntent = new Intent(MainActivity.this, SendRemindersActivity.class);
                startActivity(reminderIntent);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("teachers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Teacher teacher = new Teacher((String)doc.get("name"), (String)doc.get("email"));
                        teachers.add(teacher);
                    }
                    if (teachers.size() > 0) {
                        sendRemindersButton.setEnabled(true);
                    }
                } else {
                    Utils.toast(MainActivity.this, "Failed to download list of teachers");
                }
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) { // logged in
                    Toast.makeText(MainActivity.this, "logged in", Toast.LENGTH_SHORT).show();
                } else {            // logged out
                    startActivityForResult(
                            AuthUI.getInstance().createSignInIntentBuilder()

                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build())).build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_teachers_menu_item:
                Intent editIntent = new Intent(MainActivity.this, EditTeachersActivity.class);
                startActivity(editIntent);
                break;
            case R.id.logout_menu_item:
                AuthUI.getInstance().signOut(this);
                break;
            default:
                return true;
        }
        return true;
    }


    /* verifies if the user logged in or not */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "logged in", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "logged out", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
