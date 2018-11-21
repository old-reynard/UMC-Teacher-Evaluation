package com.umc.admin.umcteachereval;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.umc.admin.umcteachereval.models.Teacher;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

public class Utils {


    /**
     * Used to format date strings for Attendance and Marks adapters
     * @param timeInMillis input date for every date
     * @return String representation of the chosen date
     */
    public static String getShortUtcDate(long timeInMillis) {
        Locale locale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d", locale);
        return formatter.format(timeInMillis);
    }

    /**
     * Used to format date strings from milliseconds
     * @param timeInMillis input date
     * @return String representation of the chosen date
     */
    public static String getUtcDate(long timeInMillis) {
        Locale locale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", locale);
        return formatter.format(timeInMillis);
    }

    static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static void deleteTeacher(@NonNull final Teacher teacher, final Context context) {
        FirebaseFirestore.getInstance().collection("teachers")
                .document(teacher.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Utils.toast(context, teacher.getName() + " has been deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.toast(context, "Deletion failed!");
                    }
                });
    }

    /**
     * creates cell formats for the table
     * @param colour current JXL colour
     * @param vertical says if vertical borders are to be drawn bold
     * @param horizontal says if horizontal borders are to be drawn bold
     * @param font current font
     * @return resulting format
     */
    static WritableCellFormat makeFormat(Colour colour, boolean vertical, boolean horizontal, WritableFont font) {
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setBackground(colour);
            format.setBorder(Border.ALL, BorderLineStyle.THIN);
            if (horizontal) {
                format.setBorder(Border.RIGHT, BorderLineStyle.THICK);
                format.setBorder(Border.LEFT, BorderLineStyle.THICK);
            }
            if (vertical) {
                format.setBorder(Border.TOP, BorderLineStyle.THICK);
                format.setBorder(Border.BOTTOM, BorderLineStyle.THICK);
            }

            if (colour == Colour.SEA_GREEN) {
                format.setAlignment(Alignment.CENTRE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return format;
    }
}
