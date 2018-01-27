package com.cheep.utils;

import java.text.DecimalFormat;
import java.util.Formatter;

public class FormatFloatInJava {
    public static void main(String args[]) {
        // First task - format a floating point number up-to 2 decimal places
        float pi = 300000f; // From Java 5, String has a format() method
        float pi2 = 3000.256f; // From Java 5, String has a format() method
        String str = String.format("%.02f", pi);
        System.out.println("formatted float up to 2 decimals " + str);
        // If you just want to display, you can combine above two by using printf()
        // syntax of formatting will remain same
        // this will display floating point number up-to 3 decimals
        System.out.printf("floating point number up-to 3 decimals : %.03f %n", pi);
        DecimalFormat decimalFormat = new DecimalFormat("#,##,##.##");
        String price = decimalFormat.format(pi);
        String price2 = decimalFormat.format(pi2);
        System.out.print("decimal format" +price+"\n");
        System.out.print("decimal format" + price2+"\n");

        // Alternatively you can also use Formatter class to format floating point numbers
        // Allocate a Formatter on the
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        // Send all outputs to StringBuilder
        // format() has the same syntax as printf()
        formatter.format("%.4f", pi);
        // 4 decimal places
        // System.out.println("Value of PI up to four decimals : " + formatter.toString());
        formatter.close();
        // Similarly you can format double to String in Java
        double price1 = 20.00;
        System.out.printf("Value of double up-to 2 decimals : %.2f", price1);
        // best way to format floating point numbers in Java
        // beware it also round the numbers
        DecimalFormat df = new DecimalFormat("#.##");
        String formatted = df.format(2.00);
        System.out.println(formatted);
        // prints 2.46
    }
}
