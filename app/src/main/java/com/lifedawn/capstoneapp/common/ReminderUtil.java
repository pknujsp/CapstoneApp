package com.lifedawn.capstoneapp.common;

import android.content.Context;

import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;

public class ReminderUtil {
    private ReminderUtil(){}

    public static ReminderTimeObj make(int minutes){
        final int WEEK_1 = 10080;
        final int DAY_1 = 1440;
        final int HOUR_1 = 60;

        // 10일 - 14400, 4주 - 40320, (1주 - 10080, 1일 - 1440, 1시간 - 60)
        final int week = minutes / WEEK_1;
        int remainder = minutes - (WEEK_1 * week);

        final int day = remainder / DAY_1;
        remainder = remainder - (DAY_1 * day);

        final int hour = remainder / HOUR_1;
        remainder = remainder - (HOUR_1 * hour);

        final int minute = remainder;

        return new ReminderTimeObj(week,day,hour,minute);
    }

    public static String makeReminderText(ReminderTimeObj reminderTimeObj, Context context) {

        StringBuilder stringBuilder = new StringBuilder();

        if (reminderTimeObj.getWeek() > 0) {
            stringBuilder.append(reminderTimeObj.getWeek()).append(context.getString(R.string.week)).append(" ");
        }
        if (reminderTimeObj.getDay() > 0) {
            stringBuilder.append(reminderTimeObj.getDay()).append(context.getString(R.string.day)).append(" ");
        }
        if (reminderTimeObj.getHour() > 0) {
            stringBuilder.append(reminderTimeObj.getHour()).append(context.getString(R.string.hour)).append(" ");
        }
        if (reminderTimeObj.getMinute() > 0) {
            stringBuilder.append(reminderTimeObj.getMinute()).append(context.getString(R.string.minute)).append(" ");
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append(context.getString(R.string.notification_on_time));
        } else {
            stringBuilder.append(context.getString(R.string.remind_before));
        }

        return stringBuilder.toString();
    }

    public static int toMinutes(ReminderTimeObj reminderTimeObj) {
        final int WEEK_1 = 10080;
        final int DAY_1 = 1440;
        final int HOUR_1 = 60;

        return reminderTimeObj.getWeek() * WEEK_1 + reminderTimeObj.getDay() * DAY_1 +
                reminderTimeObj.getHour() * HOUR_1 + reminderTimeObj.getMinute();
    }

    public static class ReminderTimeObj{
        private int week;
        private int day;
        private int hour;
        private int minute;

        public ReminderTimeObj(int week, int day, int hour, int minute) {
            this.week = week;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
        }

        public int getWeek() {
            return week;
        }

        public void setWeek(int week) {
            this.week = week;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }
    }
}
