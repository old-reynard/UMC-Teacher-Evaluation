package com.umc.admin.umcteachereval;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.umc.admin.umcteachereval.models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditTeachersActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ProgressBar bar;
    RecyclerView editRecyclerView;
    EditTeacherAdapter editTeacherAdapter;
    List<Teacher> teachers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teachers);

        teachers = new ArrayList<>();

        bar = findViewById(R.id.edit_teachers_progressBar);
        toggleBar(true);
        extractTeachers();
    }

    private void toggleBar(boolean visible) {
        if (visible) {
            bar.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
        }
    }

    public void extractTeachers() {
        db.collection("teachers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String name = (String) doc.get("name");
                                String email = (String) doc.get("email");
                                Teacher teacher = new Teacher(name, email);
                                teacher.setId(doc.getId());
                                teachers.add(teacher);
                            }
                            toggleBar(false);
                            buildRecyclerView();

                        } else {
                            Log.w("SendReportsActivity", "Error getting documents.", task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.toast(EditTeachersActivity.this, e.getMessage());
            }
        });
    }

    private void buildRecyclerView() {
        editRecyclerView = findViewById(R.id.edit_teachers_recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager
                (EditTeachersActivity.this, LinearLayoutManager.VERTICAL, false);
        editRecyclerView.setLayoutManager(manager);
        editTeacherAdapter = new EditTeacherAdapter
                (EditTeachersActivity.this, teachers);
        editRecyclerView.setAdapter(editTeacherAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_teacher:
                navigateToTeachers();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(EditTeachersActivity.this);
                break;
        }
        return true;
    }

    private void navigateToTeachers() {
        Intent teacherIntent = new Intent(EditTeachersActivity.this, TeacherActivity.class);
        teacherIntent.putExtra("name", "");
        teacherIntent.putExtra("email", "");
        startActivity(teacherIntent);
    }
}
