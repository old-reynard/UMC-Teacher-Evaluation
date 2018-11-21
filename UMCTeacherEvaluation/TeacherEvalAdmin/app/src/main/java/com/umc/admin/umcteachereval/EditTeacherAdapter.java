package com.umc.admin.umcteachereval;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umc.admin.umcteachereval.models.Teacher;

import java.util.List;

public class EditTeacherAdapter extends RecyclerView.Adapter<EditTeacherAdapter.EditTeacherViewHolder> {

    private Context context;
    private List<Teacher> teachers;

    EditTeacherAdapter(Context context, List<Teacher> teachers) {
        this.context = context;
        this.teachers = teachers;
    }


    @NonNull
    @Override
    public EditTeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.edit_teacher_list_item, parent, false);
        return new EditTeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditTeacherViewHolder holder, int position) {
        final Teacher teacher = teachers.get(position);
        holder.nameTextView.setText(teacher.getName());
        holder.emailTextView.setText(teacher.getEmail());
        holder.numberTextView.setText(String.valueOf(position + 1));

        holder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTeachers(teacher);
            }
        });


        holder.emailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTeachers(teacher);
            }
        });

        holder.numberTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTeachers(teacher);
            }
        });
    }

    private void navigateToTeachers(Teacher teacher) {
        Intent teacherIntent = new Intent(context, TeacherActivity.class);
        teacherIntent.putExtra("name", teacher.getName());
        teacherIntent.putExtra("email", teacher.getEmail());
        teacherIntent.putExtra("id", teacher.getId());
        context.startActivity(teacherIntent);
    }


    @Override
    public int getItemCount() {
        return teachers.size();
    }


    class EditTeacherViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView emailTextView;
        TextView numberTextView;

        EditTeacherViewHolder(View itemView) {
            super(itemView);

            nameTextView    = itemView.findViewById(R.id.name_textView);
            emailTextView   = itemView.findViewById(R.id.email_textView);
            numberTextView  = itemView.findViewById(R.id.edit_teachers_number);
        }
    }
}
