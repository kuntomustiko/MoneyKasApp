package com.minara.kirana.moneykas.helper;

import java.util.Calendar;

public class CurrentDate {

    public static Calendar calendar   = Calendar.getInstance();
    public static int year            = calendar.get(Calendar.YEAR); // current year
    public static int month           = calendar.get(Calendar.MONTH); // current month
    public static int day             = calendar.get(Calendar.DAY_OF_MONTH); // current day

}
