package com.umc.admin.umcteachereval;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umc.admin.umcteachereval.models.Mark;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.umc.admin.umcteachereval.models.Teacher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.PageOrientation;
import jxl.format.PaperSize;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SendReportFragment extends Fragment implements DatePickerFragment.DatePickerListener {
    public SendReportFragment() {}

    /* Firestore database keys */
    private static final String timestampKey = "timestamp";
    private static final String senderKey = "sender";
    private static final String periodKey = "period";
    private static final String questionKey = "question";
    private static final String valueKey = "value";
    private static final String teacherKey = "teacher";
    private static final String textKey = "text";
    private static final String descriptionKey = "description";

    /* start and end dates of the chosen report period */
    long start;
    long end;

    /* instructions constants; will determine what fragment this is */
    private static final int INST_TEACHER       = 101;
    private static final int INST_FACILITIES    = 102;
    private static final int INST_FEEDBACK      = 103;
    private static final int INST_REMINDERS     = 104;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /* excel table constants */
    public static final int VOFFSET = 4;
    public static final int HOFFSET = 1;
    public static final int QUESTIONS = 16;
    public static final int ORG_QUESTIONS = 6;
    public static final int PRES_QUESTIONS = 5;
    public static final int TOTALS = 3;
    public static final int MAX = 50;
    public static final String AVERAGE = "Average";
    public static final String[] AVERAGE_LABELS = {
            "Organization and Lesson Planning",
            "Lesson Presentation and Instruction",
            "Classroom Management",
            "Total"
    };
    public static final int DIVIDE = QUESTIONS + TOTALS + 3;

    /* Storage Permissions */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /* UI elements */
    ProgressBar bar;
    Button startButton;
    Button endButton;
    Button sendButton;

    /* marks extracted from the database */
    List<Mark> marks;

    /* label instructing the user what kind of report this fragment is responsible for */
    String instruction;
    /*  code of the current fragment; expected to be one instruction constants;
        defines the behaviour of the fragment */
    private int CODE;

    Map<String, Integer> votesMap = new HashMap<>();

    private final List<Teacher> localTeachers = MainActivity.teachers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.send_report_fragment, container, false);

        final TextView instructionTextView = rootView.findViewById(R.id.instruction_textView);
        bar = rootView.findViewById(R.id.report_progressBar);
        startButton = rootView.findViewById(R.id.start_button);
        endButton = rootView.findViewById(R.id.end_button);
        sendButton = rootView.findViewById(R.id.send_reports_button);

        instructionTextView.setText(instruction == null ? "Something went wrong here" : instruction);

        marks = new ArrayList<>();

        CODE = instructionCode(instruction);
        toggleTeacherBar(false);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v);
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(v);
            }
        });

        if (CODE == INST_REMINDERS) {
            sendButton.setText(getContext().getString(R.string.main_buttons_send_reminders));
        }

        toggleSendButton(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CODE != INST_REMINDERS) marksReport();
                else sortReminders();
            }
        });
        return rootView;
    }

    /** hides and shows the sendButton */
    private void toggleSendButton(boolean visible) {
        if (visible) {
            sendButton.setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.GONE);
        }
    }

    /** hides and shows the loading bar */
    private void toggleTeacherBar(boolean visible) {
        if (visible) {
            bar.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
        }
    }

    private void showDatePicker(View v) {
        DialogFragment dateFragment = DatePickerFragment.newInstance(this, v.getId());
        dateFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Uses data passed by DatePickerListener to set the start and end date of the report (they are
     * later used to filter the report data) and update the date buttons
     * @param date date passed from the DatePickerListener
     * @param resId id of the button that called the DatePickerListener
     */
    @Override
    public void onDateSet(long date, int resId) {

        switch (resId) {
            case R.id.start_button:
                if ((end != 0 && date < end) || end == 0) {
                    start = date;
                    String dateString = Utils.getShortUtcDate(date);
                    startButton.setText(dateString);
                } else if (date > end) {
                    String message = "Choose start date before end date";
                    Utils.toast(getContext(), message);
                }
                checkDates();
                break;
            case R.id.end_button:
                if ((start != 0 && date > start) || start == 0) {
                    end = date;
                    String endString = Utils.getShortUtcDate(date);
                    endButton.setText(endString);
                } else if (date < start) {
                    String message = "Choose end date after start date";
                    Utils.toast(getContext(), message);
                }
                checkDates();
                break;
            default:
                break;
        }
    }

    /**
     * Checks if the dates on startButton and EndButton were selected correctly
     * if so, extracts the marks
     * if not, sends a notification toast
     */
    private void checkDates() {
        if (start != 0 && end != 0 && start < end) {
            toggleTeacherBar(true);
            extractMarks();

        } else if (start == 0 || end == 0 || start > end) {
            toggleSendButton(false);
        }
    }

    public void marksReport() {
        verifyStoragePermissions(getActivity());

        File storage = Environment.getExternalStorageDirectory();
        String reportFileName = getReportName();


        File directory = new File(storage.getAbsolutePath());
        if (!directory.isDirectory()) {
            //create directory if it does not exist
            directory.mkdirs();
        }


        /* file path */
        File report = new File(directory, reportFileName);

        try {

            /* excel book settings */
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "CA"));

            WritableWorkbook workbook = Workbook.createWorkbook(report, wbSettings);

            /* Excel sheet name. 0 represents first sheet */
            final WritableSheet sheet = workbook.createSheet("North York", 0);
            WritableFont.FontName usedFont = WritableFont.createFont("Calibri");
            final WritableFont font = new WritableFont(usedFont, 11, WritableFont.BOLD);

            sheet.setPageSetup(PageOrientation.LANDSCAPE, PaperSize.A4, 0, 0);

            switch (CODE) {
                case INST_TEACHER   : teacherReport(sheet, font);       break;
                case INST_FACILITIES: facilitiesReport(sheet, font);    break;
                case INST_FEEDBACK  : feedbackReport(sheet, font);      break;
                default: break;
            }

            /* wrapping up */
            workbook.write();
            workbook.close();

            String[] recipients = {
                   "elharzi2006@yahoo.com",
                   "machado2603@hotmail.com",
                   "jimtruo@gmail.com",
                   "studentservices@umcollege.ca"
            };
            send(recipients, report, "");

        } catch (WriteException we) {
            we.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.toast(getContext(), e.getMessage());
        }
    }

    private void sortReminders() {
        Map<Teacher, String> teacherMap = new HashMap<>();
        for (Teacher t : localTeachers) {
            t.setVotes(0);
            if (!teacherMap.containsKey(t)) {
                teacherMap.put(t, t.getName());
                votesMap.put(t.getName(), t.getVotes());
            }
        }

        for (Mark m : marks) {
            final String thisTeacher = m.getTeacher();
            if (teacherMap.containsValue(thisTeacher)) {
                Integer vote = votesMap.get(thisTeacher);

                vote = Integer.valueOf(vote.intValue() + 1);
                votesMap.put(thisTeacher, vote);
            }
        }
        sendReminders();
    }

    private void sendReminders() {
        String message = "Hi teachers, \n\n" +
                "Please remember that the Teacher and Facilities Evaluation takes place online, through our UMC Teacher Evaluation App.\n\n" +
                "This is to remind you to remind your students to submit their evaluations in case they have not done so yet.\n\n" +
                "So far we have received information from following numbers of people, divided by teacher: \n\n\n";
        StringBuilder body = new StringBuilder(message);

        List<String> recipients = new ArrayList<>();
        int maxLength = 0;
        for (Teacher t : localTeachers) {
            if (t.getName().length() > maxLength) {
                maxLength = t.getName().length();
            }
            if (votesMap.containsKey(t.getName())) {
                recipients.add(t.getEmail());
            }
        }

        for (String teacher : votesMap.keySet()) {
            String line = "| " + teacher + "  :  " + (votesMap.get(teacher) / 16) + "\n";
            body.append(line);
        }

        body.append("\n\nIf not all of your students have submitted their evaluations, please remind them to do so.\n\n\n");

        String[] rs = new String[recipients.size()];
        rs = recipients.toArray(rs);
        send(rs, null, body.toString());
    }

    private void send(String[] recipients, File report, String body) {
        /* sending the report through an intent */
        Intent mail = new Intent(Intent.ACTION_SEND);
        mail.setType("text/plain");
        if (CODE != INST_REMINDERS) {
            Uri path = Uri.fromFile(report);
            mail.putExtra(Intent.EXTRA_STREAM, path);
        } else {
            mail.putExtra(Intent.EXTRA_TEXT, body);
        }

        mail.putExtra(Intent.EXTRA_EMAIL, recipients);

        String subject;
        String unknown = "Unknown";

        switch (CODE) {
            case INST_TEACHER   : subject = "Teacher Evaluation Report";    break;
            case INST_FACILITIES: subject = "Facilities report";            break;
            case INST_FEEDBACK  : subject = "Feedback";                     break;
            case INST_REMINDERS : subject = "Teacher evaluation reminder";  break;
            default: subject = unknown;
        }

        if (!subject.equals(unknown)) {
            subject += " as of " + Utils.getUtcDate(end);
        }

        mail.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(mail);
        onSendCleanUp();
    }

    /**
     * gets report file name depending on the button that sends the command
     * @return file name of the report
     */
    private String getReportName() {
        String reportName;
        String unknown = "Unknown";
        switch (CODE) {
            case INST_TEACHER:
                reportName = "Teacher Evaluation report";   break;
            case  INST_FACILITIES:
                reportName = "Facilities report";           break;
            case INST_FEEDBACK:
                reportName = "Feedback";                    break;
            default:
                reportName = unknown;
        }

        if (!reportName.equals(unknown)) {
            reportName += " as of " + Utils.getUtcDate(end) + ".xls";
        }
        return reportName;
    }

    private void teacherReport(WritableSheet sheet, WritableFont font) {
        try {
            /* cell formats by colors */
            final WritableCellFormat red = Utils.makeFormat(Colour.RED, true, true, font);
            final WritableCellFormat greyV = Utils.makeFormat(Colour.GRAY_25, true, false, font);
            final WritableCellFormat greyH = Utils.makeFormat(Colour.GRAY_25, false, true, font);
            final WritableCellFormat blue = Utils.makeFormat(Colour.OCEAN_BLUE, false, false, font);
            final WritableCellFormat lightBlue = Utils.makeFormat(Colour.PALE_BLUE, true, true, font);
            final WritableCellFormat yellow = Utils.makeFormat(Colour.YELLOW, true, true, font);
            final WritableCellFormat white = Utils.makeFormat(Colour.WHITE, false, false, font);

            /* prepare final marks map */
            final Map<String, List<List<Long>>> ready = flattenMarks(sortMarks());
            /* list of teachers */
            String[] tKeys = ready.keySet().toArray(new String[ready.keySet().size()]);
            /* iterating over list of teachers and printing their names and marks */
            for (int t = 0; t < tKeys.length; t++) {

                String currentTeacher = tKeys[t];
                int firstRow = DIVIDE * t + VOFFSET;

                /* number columns */
                for (int k = 1; k <= MAX; k++) {
                    sheet.addCell(new Number(k, firstRow, k, greyV));
                    /* format the columns apart from the left one */
                    sheet.setColumnView(k, 3);
                }

                /* set up the labels for average values */
                for (int l = 0; l < AVERAGE_LABELS.length; l++) {
                    int thisRow = firstRow + QUESTIONS + 1 + l;
                    sheet.addCell(new Label(0, thisRow, String.valueOf(l + 1) + ")", blue));
                    sheet.mergeCells(HOFFSET, thisRow, MAX, thisRow);
                    sheet.addCell(new Label(1, thisRow, AVERAGE_LABELS[l], lightBlue));
                }

                /* coordinates for top left and bottom right cells */
                int firstX = 0, firstY = 0, lastY = 0;

                /* selecting this teacher's marks */
                final List<List<Long>> grid = ready.get(currentTeacher);

                /* log the marks */
                for (int s = 0; s < grid.size(); s++) {
                    int x = s + HOFFSET;
                    final List<Long> theseMarks = grid.get(s);
                    for (int m = 0; m < theseMarks.size(); m++) {
                        int y = firstRow + m + 1;
                        Long mark = grid.get(s).get(m);
                        sheet.addCell(new Number(x, y, mark.intValue(), white));

                        /* number the lines in the first column */
                        if (s == 0) {
                            sheet.addCell(new Label(0, y, String.valueOf(m + 1), greyH));
                            if (m == 0) {
                                /* remember the top left cell */
                                WritableCell firstCell = sheet.getWritableCell(x, y);
                                firstX = firstCell.getColumn();
                                firstY = firstCell.getRow() + 1;
                            }
                        }

                        if (s == grid.size() - 1) {
                            /* set the average formulae for every line */
                            String avgString = "AVERAGE(" + letter(HOFFSET) + String.valueOf(y + 1)
                                    + ":A" + letter(MAX) + String.valueOf(y + 1) + ")";
                            sheet.addCell(new Formula(MAX + 1, y, avgString, white));
                            if (m == theseMarks.size() - 1) {
                                /* remember the bottom left cell */
                                WritableCell lastCell = sheet.getWritableCell(x, y);
                                lastY = lastCell.getRow() + 1;
                            }
                        }
                    }
                }

                int avgX = HOFFSET + MAX;

                /* average formulae */
                String[] averages = {
                        "AVERAGE(" + letter(firstX) + String.valueOf(firstY) +
                                ":A" + letter(MAX) + String.valueOf(firstRow + ORG_QUESTIONS + 1),
                        "AVERAGE(" + letter(firstX)  + String.valueOf(firstY + ORG_QUESTIONS) +
                                ":A" + letter(MAX) + String.valueOf(firstRow + ORG_QUESTIONS + PRES_QUESTIONS + 1),
                        "AVERAGE(" + letter(firstX)  + String.valueOf(firstY + ORG_QUESTIONS + PRES_QUESTIONS) +
                                ":A" + letter(MAX) + String.valueOf(lastY),
                        "AVERAGE(" + letter(firstX)  + String.valueOf(firstY) +
                                ":A" + letter(MAX) + String.valueOf(lastY),
                };

                /* set AVERAGE label */
                sheet.addCell(new Label(avgX, firstRow, AVERAGE, red));
                sheet.getColumnView(avgX).setAutosize(true);
                /* set teacher's name */
                sheet.addCell(new Label(0, firstRow, "" + (t+1) + ". " + currentTeacher, yellow));
                sheet.getColumnView(0).setAutosize(true);
                /* set average cells */
                for (int a = 1; a <= averages.length; a++) {
                    sheet.addCell(new Formula(avgX, firstRow + QUESTIONS + a, averages[a - 1], red));
                }
            }

            setLabels(sheet, font);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void facilitiesReport(WritableSheet sheet, WritableFont font) {
        final WritableCellFormat white = Utils.makeFormat(Colour.WHITE, false, false, font);
        final WritableCellFormat greyV = Utils.makeFormat(Colour.GRAY_25, true, false, font);
        final WritableCellFormat greyH = Utils.makeFormat(Colour.GRAY_25, false, true, font);
        final WritableCellFormat red = Utils.makeFormat(Colour.RED, true, true, font);

        final String[] questions = getActivity().getResources().getStringArray(R.array.facilities_questions);


        try {
            final List<List<Long>> grid = flattenFacilities(sortMarks());

            for (int s = 0; s < grid.size(); s++) {
                for (int m = 0; m < grid.get(s).size(); m++) {
                    int row = VOFFSET + m + 1;
                    int col = HOFFSET + s;

                    if (s == 0 && m == 0) {
                        sheet.addCell(new Label(col - 1, row - 1, "Facilities", red));
                        sheet.setColumnView(col - 1, 12);
                    }

                    /* set labels to rows */
                    if (s == 0) {
                        String question = questions[m < questions.length ? m : 0];
                        sheet.addCell(new Label(0, row, question, greyH));
                    }

                    /* number the columns */
                    if (m == 1) {
                        sheet.addCell(new Number(col, VOFFSET, s + 1, greyV));
                    }

                    /* log the marks */
                    int mark = grid.get(s).get(m).intValue();
                    sheet.addCell(new Number(col, row, mark, white));
                    sheet.setColumnView(col, 3);

                    /* set average formula for the line */
                    if (s == grid.size() - 1) {
                        String avg = "AVERAGE(B" + String.valueOf(row + 1) + ":" + letter(col) + String.valueOf(row + 1);
                        sheet.addCell(new Formula(col + 1, row, avg, red));
                    }

                    /* set total and average label, final average formula */
                    if (s == grid.size() - 1 && m == grid.get(s).size() - 1) {
                        sheet.addCell(new Label(0, row + 1, "Total", greyH));
                        String avg = "AVERAGE(B6:" + letter(col) + String.valueOf(row + 1);
                        sheet.addCell(new Formula(col + 1, row + 1, avg, red));
                        sheet.addCell(new Label(col + 1, VOFFSET, AVERAGE, red));
                    }
                }
            }
            setLabels(sheet, font);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void feedbackReport(WritableSheet sheet, WritableFont font) {
        final WritableCellFormat white = Utils.makeFormat(Colour.WHITE, false, false, font);
        final WritableCellFormat orange = Utils.makeFormat(Colour.ORANGE, true, true, font);
        final WritableCellFormat light = Utils.makeFormat(Colour.LIGHT_ORANGE, true, true, font);
        final WritableCellFormat green = Utils.makeFormat(Colour.SEA_GREEN, true, true, font);

        final int numberCol         = 2;
        final int descriptionCol    = numberCol + 1;
        final int textCol           = descriptionCol + 1;
        try {
            white.setWrap(true);

            for (int f = 0; f < marks.size(); f++) {
                int row = VOFFSET + f + 1;
                Mark mark = marks.get(f);
                String description = mark.getDescription();


                sheet.addCell(new Number(numberCol, row, (f + 1),  white));
                sheet.addCell(new Label(descriptionCol, row, description, white));
                sheet.addCell(new Label(textCol, row, mark.getText(), white));

            }
            final String utcDate = Utils.getUtcDate(end);
            sheet.addCell(new Label(0, 1, "TERM:", orange));
            sheet.addCell(new Label(0, 2, "DATES:", orange));
            sheet.addCell(new Label(1, 1, " ", light));
            sheet.addCell(new Label(1, 2, utcDate, light));
            sheet.addCell(new Label(textCol, 1, "FEEDBACK", green));

            sheet.setColumnView(numberCol, 3);
            sheet.setColumnView(descriptionCol, 25);
            sheet.setColumnView(textCol, 100);
            sheet.setColumnView(1, utcDate.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * cancels all the data after the report has been sent and defaults the UI
     */
    private void onSendCleanUp() {
        marks.clear();
        start = 0;
        end = 0;
        startButton.setText(R.string.send_reports_start_button);
        endButton.setText(R.string.send_reports_end_date);
        toggleSendButton(false);
        toggleTeacherBar(false);
    }

    /**
     * sets date and header labels to the report sheet
     * @param sheet writable xls sheet
     * @param font used font
     * @throws WriteException throws exception if failed to write to sheet
     */
    private void setLabels(WritableSheet sheet, WritableFont font) throws WriteException {
        WritableCellFormat orange = Utils.makeFormat(Colour.ORANGE, true, true, font);
        WritableCellFormat light = Utils.makeFormat(Colour.LIGHT_ORANGE, true, true, font);

        /* set date and term cells */
        sheet.addCell(new Label(0, 1, "TERM:", orange));
        sheet.mergeCells(1, 1, 7, 1);
        sheet.addCell(new Label(0, 2, "DATES:", orange));
        sheet.mergeCells(1, 2, 7, 2);
        sheet.addCell(new Label(1, 1, " ", light));
        sheet.addCell(new Label(1, 2, Utils.getUtcDate(end), light));

        WritableCellFormat green = Utils.makeFormat(Colour.SEA_GREEN, true, true, font);
        /* set header */
        sheet.mergeCells(12, 1, 42, 2);
        String title;
        switch (CODE) {
            case INST_TEACHER       : title = "Teacher Evaluation"; break;
            case INST_FACILITIES    : title = "Facilities report";  break;
            case INST_FEEDBACK      : title = "Feedback";           break;
            default:                  title = "ERROR";
        }

        sheet.addCell(new Label(12 , 1, title, green));
    }

    /**
     * checks if the app is allowed to store files in inner storage and asks for permission if not
     * @param activity current activity
     */
    private static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * sort marks in maps in the following order: by teacher - by sender - by period
     * @return not flattened map of marks to be processed further (by flattenMarks)
     */
    private Map sortMarks() {
        Map extract;

        switch (CODE) {
            case INST_TEACHER:
                Map<String, Map<String, Map<Long, Map<Long, List<Long>>>>> raw = new HashMap<>();
                for (Mark mark : marks) {
                    String teacher  = mark.getTeacher();
                    String sender   = mark.getSender();
                    long date       = mark.getTimestamp();
                    long period     = mark.getPeriod();
                    long value      = mark.getValue();

                    /* sort marks by teacher */
                    if (!raw.containsKey(teacher)) {
                        raw.put(teacher, new HashMap<String, Map<Long, Map<Long, List<Long>>>> ());
                    }

                    /* by student */
                    Map<String, Map<Long, Map<Long, List<Long>>>> senderMap = raw.get(teacher);
                    if (!senderMap.containsKey(sender)) {
                        senderMap.put(sender, new HashMap<Long, Map<Long, List<Long>>>());
                    }

                    /* by date */
                    Map<Long, Map<Long, List<Long>>> dateMap = senderMap.get(sender);
                    if (!dateMap.containsKey(date)) {
                        dateMap.put(date, new HashMap<Long, List<Long>>());
                    }

                    /* by period */
                    Map<Long, List<Long>> periodMap = dateMap.get(date);
                    if (!periodMap.containsKey(period)) {
                        periodMap.put(period, new ArrayList<Long>());
                    }
                    periodMap.get(period).add(value);
                }
                extract = raw;
                break;
            case INST_FACILITIES:
                Map<String, Map<Long, ArrayList<Long>>> senderMap = new HashMap<>();
                for (Mark mark : marks) {

                    String sender   = mark.getSender();
                    long value      = mark.getValue();
                    long date       = mark.getTimestamp();

                    /* sort facility marks by student */
                    if (!senderMap.containsKey(sender)) {
                        senderMap.put(sender, new HashMap<Long, ArrayList<Long>>());
                    }

                    /* by date  */
                    Map<Long, ArrayList<Long>> dateMap = senderMap.get(sender);
                    if (!dateMap.containsKey(date)) {
                        dateMap.put(date, new ArrayList<Long>());
                    }
                    dateMap.get(date).add(value);
                }
                extract = senderMap;
                break;
            default:
                extract = null;
        }
        return extract;
    }

    /**
     * creates a grid of marks where horizontal axis sorts marks by sender and vertical axis sorts
     * marks by question in the questionnaire
     * @param raw sorted and multi-level map of marks
     * @return grid of marks, readied for printing
     */
    private Map<String, List<List<Long>>> flattenMarks(@NonNull Map<String, Map<String, Map<Long, Map<Long, List<Long>>>>> raw) {
        Map<String, List<List<Long>>> sorted = new HashMap<>();
        final Set<String> tKeys = raw.keySet();

        for (String tKey : tKeys) {
            Map<String, Map<Long, Map<Long, List<Long>>>> senderMap = raw.get(tKey);
            if (!sorted.containsKey(tKey)) sorted.put(tKey, new ArrayList<List<Long>>());
            List<List<Long>> lists = sorted.get(tKey);
            for (String sKey : senderMap.keySet()) {
                final Map<Long, Map<Long, List<Long>>> dateMap = senderMap.get(sKey);
                for (Long dKey : dateMap.keySet()) {
                    final Map<Long, List<Long>> periodMap = dateMap.get(dKey);
                    for (Long pKey : periodMap.keySet()) {
                        final List<Long> marks = periodMap.get(pKey);
                        lists.add(marks);
                    }
                }
            }
        }

        return sorted;
    }

    private List<List<Long>> flattenFacilities(@NonNull Map<String, Map<Long, ArrayList<Long>>> fac) {

        final List<List<Long>> sorted = new ArrayList<>();
        for (String sKey : fac.keySet()) {
            final Map<Long, ArrayList<Long>> senderMap = fac.get(sKey);
            for (Long dKey : senderMap.keySet()) {
                final List<Long> marks = senderMap.get(dKey);
                sorted.add(marks);
            }
        }

        return sorted;
    }

    /**
     * extracts marks from Firestore database, relies on user selected start and end date;
     * saves date in global marks list
     */
    private void extractMarks() {
        marks.clear();

        String collectionKey;
        switch (CODE) {
            case INST_TEACHER       :
            case INST_REMINDERS     : collectionKey = "marks";              break;
            case INST_FACILITIES    : collectionKey = "facilities";         break;
            case INST_FEEDBACK      : collectionKey = "feedback";           break;
            default:                  collectionKey = "";
        }

        CollectionReference marksRef = db.collection(collectionKey);
        marksRef.whereGreaterThanOrEqualTo(timestampKey, start)
                .whereLessThanOrEqualTo(timestampKey, end)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                Mark.Builder builder = new Mark.Builder(
                                        (long)      doc.get(timestampKey),
                                        (String)    doc.get(senderKey)
                                );
                                switch (CODE) {
                                    case INST_TEACHER:
                                    case INST_REMINDERS:
                                        builder .period(((Long)doc.get(periodKey)).intValue())
                                                .question(((Long)doc.get(questionKey)).intValue())
                                                .teacher((String)doc.get(teacherKey))
                                                .value(((Long)doc.get(valueKey)).intValue());
                                        break;
                                    case INST_FACILITIES:
                                        builder .question(((Long)doc.get(questionKey)).intValue())
                                                .value(((Long)doc.get(valueKey)).intValue());
                                        break;
                                    case INST_FEEDBACK:
                                        builder. text(   (String)doc.get(textKey));
                                        builder. description((String)doc.get(descriptionKey));
                                        break;
                                    default: Utils.toast(getContext(), "Wrong instruction code");
                                }

                                Mark mark = builder.build();
                                marks.add(mark);
                            }
                            onPostExtractMarks();
                        } else {
                            Log.w("SendReportsActivity", "Error getting documents.", task.getException());
                        }

                    }
                });
    }

    /**
     * used after marks for corresponding report have been extracted from the database
     * if any number of marks have been extracted, it turns on the Send Button, hides the loading
     * bar and sends a Toast message telling the user how many marks have been downloaded
     * otherwise sends a Toast message that there were no marks for the selected period
     */
    private void onPostExtractMarks() {
        if (marks.size() > 0) {
            sendButton.setEnabled(true);
            toggleSendButton(true);
            Utils.toast(getContext(), "" + marks.size() + " marks extracted");
        } else {
            Utils.toast(getContext(), "No marks for this period, nothing to send!");
            onSendCleanUp();
        }
        toggleTeacherBar(false);
    }

    /**
     * utility method that gets the "letter index of the column for Excel tables
     * @param in index
     * @return letter index
     */
    public static String letter(int in) {
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int index = in % alpha.length();
        return alpha.substring(index, index + 1);
    }

    /**
     * used to set the instruction label in the corresponding TextView
     * @param instruction passed down from the activity
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * sets the code of this fragment that defines its behaviour
     * @param instruction the type of fragment that is passed down from the activity
     * @return the type of the current fragment
     */
    private int instructionCode(String instruction) {
        int[] codes = {
                INST_TEACHER, INST_FACILITIES, INST_FEEDBACK, INST_REMINDERS
        };
        String[] instructions = getContext().getResources().getStringArray(R.array.send_reports_instructions);
        for (int i = 0; i < instructions.length; i++) {
            if (instruction.equals(instructions[i])) {
                return codes[i];
            }
        }
        return 0;
    }
}
