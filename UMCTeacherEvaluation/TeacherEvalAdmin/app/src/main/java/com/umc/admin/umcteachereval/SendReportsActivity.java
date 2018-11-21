package com.umc.admin.umcteachereval;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * Send Reports is meant to form XLS reports and email them to admin.
 * The admin's phone effectively functions as a server for all the processing
 */
public class SendReportsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_reports);

        SendReportFragment teacherFragment = new SendReportFragment();
        SendReportFragment facilitiesFragment = new SendReportFragment();
        SendReportFragment feedbackFragment = new SendReportFragment();

        teacherFragment.setInstruction(getString(R.string.send_reports_instruction));
        facilitiesFragment.setInstruction(getString(R.string.send_facilities_instruction));
        feedbackFragment.setInstruction(getString(R.string.send_feedback_instruction));

        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .add(R.id.teacher_eval_fragment, teacherFragment)
                .add(R.id.facilities_fragment, facilitiesFragment)
                .add(R.id.feedback_fragment, feedbackFragment)
                .commit();
    }
}
