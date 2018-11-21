package com.umc.admin.umcteachereval;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SendRemindersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_reminders);

        SendReportFragment remindersFragment = new SendReportFragment();

        remindersFragment.setInstruction(getString(R.string.send_reminders_instruction));

        FragmentManager manager = getFragmentManager();

        manager.beginTransaction().add(R.id.reminders_fragment, remindersFragment).commit();
    }
}
