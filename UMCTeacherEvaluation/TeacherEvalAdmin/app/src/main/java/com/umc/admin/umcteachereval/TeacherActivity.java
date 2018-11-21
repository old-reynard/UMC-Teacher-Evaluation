package com.umc.admin.umcteachereval;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.umc.admin.umcteachereval.models.Teacher;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TeacherActivity extends AppCompatActivity {

    Teacher teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        final EditText nameEdit = findViewById(R.id.name_editText);
        final EditText emailEdit = findViewById(R.id.email_editText);

        final Intent intent = getIntent();
        teacher = new Teacher(intent.getStringExtra("name"), intent.getStringExtra("email"));
        teacher.setId(intent.getStringExtra("id"));

        ImageButton deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherActivity.this);
                builder.setTitle("Delete?")
                        .setMessage("This will delete this teacher")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.deleteTeacher(teacher, TeacherActivity.this);
                                finish();
                            }
                        })
                        .setNegativeButton("No, stop", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        if (teacher.getEmail() != null) {
            emailEdit.setText(teacher.getEmail());
        }

        if (teacher.getName() != null) {
            nameEdit.setText(teacher.getName());
        }

        if (teacher.getId() == null || teacher.getId().isEmpty()) {
            setTitle("Create teacher");
            deleteButton.setVisibility(View.GONE);
        }

        final FloatingActionButton saveButton = findViewById(R.id.save_teacher_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString().trim();
                String email = emailEdit.getText().toString().trim();
                if (name.isEmpty()) {
                    Utils.toast(TeacherActivity.this, "We need a name");
                    return;
                }
                if (email.isEmpty()) {
                    Utils.toast(TeacherActivity.this, "We need an email");
                    return;
                }
                saveTeacher(name, email);
            }
        });
    }

    /**
     * saves teacher to Firestore
     * @param name name of the teacher
     * @param email email of the teacher, needed for notifications
     */
    private void saveTeacher(final String name, String email) {
        final CollectionReference teachersRef = FirebaseFirestore.getInstance().collection("teachers");
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        if (teacher.getId() == null || teacher.getId().isEmpty()) {
            teachersRef.add(map)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Utils.toast(TeacherActivity.this, name + " saved!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(TeacherActivity.this, "Not saved!");
                        }
                    });
        } else {
            teachersRef.document(teacher.getId()).set(map)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(TeacherActivity.this, "Not saved!");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Utils.toast(TeacherActivity.this, name + " saved!");
                        }
                    });
        }
        finish();
    }
}
