package com.umc.admin.umcteachereval.models;

import com.umc.admin.umcteachereval.Utils;

public class Mark {

    private final int period;
    private final int question;
    private final int value;
    private final long timestamp;
    private final String sender;
    private final String teacher;
    private final String text;
    private final String description;

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

    public int getPeriod() {
        return period;
    }

    public int getQuestion() {
        return question;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "" + value + " for question " + question + " to " + teacher + " in period " + period
                + " from " + sender + " on " + Utils.getShortUtcDate(timestamp);
    }

    public static class Builder {
        // required parameters
        private final long timestamp;
        private final String sender;

        // optional parameters
        private String text     = "";
        private int question    = 0;
        private int value       = 0;
        private String teacher  = "";
        private int period      = 0;
        private String description = "";

        public Builder(long timestamp, String sender) {
            this.timestamp  = timestamp;
            this.sender     = sender;
        }

        public Builder text(String val)         { text          = val;   return this; }
        public Builder question(int val)        { question      = val;   return this; }
        public Builder value(int val)           { value         = val;   return this; }
        public Builder teacher(String val)      { teacher       = val;   return this; }
        public Builder period(int val)          { period        = val;   return this; }
        public Builder description(String val)  { description   = val;   return this; }

        public Mark build() {
            return new Mark(this);
        }
    }

    private Mark(Builder builder) {
        period = builder.period;
        question = builder.question;
        value = builder.value;
        timestamp = builder.timestamp;
        sender = builder.sender;
        teacher = builder.teacher;
        text = builder.text;
        description = builder.description;
    }
}
