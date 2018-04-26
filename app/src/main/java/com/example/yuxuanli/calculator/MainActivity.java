package com.example.yuxuanli.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView numScreen;
    private TextView resultScreen;
    private String display = ""; //for displaying in the screen
    private String strRecord = ""; //for calculating
    private String result = ""; // for displaying in the result screen
    private boolean dotClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        numScreen = (TextView)findViewById(R.id.numScreen);
        resultScreen = (TextView)findViewById(R.id.operator);
    }

    /**
     * Update the display screen
     */
    private void updateScreen() {
        numScreen.setText(display);
    }

    /**
     * update the result screen
     */
    private void updateResult(){
        try {
            resultScreen.setTextSize(50);
            if (strRecord.equals("")) {
                result = "";
            } else {
                result = calculateResult(strRecord);
            }
        } catch (Exception e) {
            result = "Error";
        }

        resultScreen.setText(result);
    }

    /**
     * Update when click numbers
     * @param v
     */
    public void onClickNumbers(View v) {
        if (display.length() != 0 && display.charAt(display.length() - 1) == '=') {
            display = "";
            strRecord = "";
            result = "";
        }
        resultScreen.setText(result);

        Button btNum = (Button) v;
        String numDisplay = btNum.getText().toString();
        String numRec = numDisplay;
        if(numDisplay.equals("+/-")) {
            if (!validNegSign(strRecord)) {
                return;
            }
            numDisplay = "-";
            numRec = "n";

            //the previous input is also neg sign
            if (strRecord.length() != 0 && strRecord.charAt(strRecord.length() - 1) == 'n') {
                display = display.substring(0, display.length() - 1);
                strRecord = strRecord.substring(0, strRecord.length() - 1);
                updateScreen();
                return;
            }
        }
        display += numDisplay;
        strRecord += numRec;
        updateScreen();
    }

    /**
     * Update when click operators
     * @param v
     */
    public void onClickOperator(View v) {
        Button btOpe = (Button) v;
        String ope = btOpe.getText().toString();
        if (display.length() != 0 && display.charAt(display.length() - 1) == '=' && result.matches("[+-]?([0-9]*[.])?[0-9]+")) {
            display = result;
            if (result.charAt(0) == '-') {
                strRecord = "n" + result.substring(1, result.length());
            } else {
                strRecord = result;
            }

            display += ope;
            strRecord += ope;

            result = "";
            updateScreen();
            dotClicked = false;
        } else if (checkPreInputValid(display)) {
            display += ope;
            strRecord += ope;

            updateScreen();
            dotClicked = false;
        }
    }

    /**
     * when click the dot
     * @param v
     */
    public void onClickDot(View v) {
        if (checkPreInputValid(display) && !dotClicked) {
            Button btOpe = (Button) v;
            display += ".";
            strRecord += ".";
            updateScreen();
            dotClicked = true;
        }
    }

    /**
     * Calculate result when click equal button
     * @param v
     */
    public void onClickEqual(View v){
        if (checkPreInputValid(display)) {
            Button btEql = (Button) v;
            display += "=";
            strRecord += "=";
            dotClicked = false;
            updateScreen();
            updateResult();
        }
    }

    /**
     * delete one previous input
     * @param v
     */
    public void onClickDel(View v) {
        if (display.length() != 0) {
            if (display.charAt(display.length() - 1) == '.') {
                dotClicked = false;
            }
            display = display.substring(0, display.length() - 1);
            strRecord = strRecord.substring(0, strRecord.length() - 1);
        }
        updateScreen();
    }

    /**
     * When click the smile face button
     * @param v
     */
    public void onClickCopyright(View v) {
        resultScreen.setText("@Copyright: Yuxuan(Stella) Li");
        resultScreen.setTextSize(25);
    }

    /**
     * Clear the screen and result when click clear button
     * @param v
     */
    public void onClickClear(View v) {
        display = "";
        strRecord = "";
        result = "";
        dotClicked = false;
        updateScreen();
        updateResult();
    }

    /**
     * Calculate the result based ont he display string
     * @param s
     * @return the result string
     */
    private String calculateResult (String s) throws Exception {
        //handle the string formate
        List<String> strList = new ArrayList<>();
        int i = 0;
        StringBuilder tempNum = new StringBuilder();
        while (i < s.length() - 1) {
            char c = s.charAt(i);
            if (c == 'n') { // it is not operator
                tempNum.append("-");
            } else if ("+-x/".indexOf(c) == -1) {
                tempNum.append(c);
            } else {
                strList.add(tempNum.toString());
                strList.add(c + "");
                tempNum.setLength(0);
            }
            i++;
        }
        strList.add(tempNum.toString());

        //doing the calculation
        Deque<String> deque = new LinkedList<>();
        for (int j = 0; j < strList.size(); j++) {
            String cur = strList.get(j);
            if (cur.equals("x") || cur.equals("/")) {
                Double a = Double.parseDouble(deque.removeLast());
                Double b = Double.parseDouble(strList.get(j + 1));
                j++;
                Double temp = cur.equals("x") ? a * b : a / b;
                deque.add(formatDouble(temp));
            } else {
                deque.add(cur);
            }
        }

        String result = deque.remove();
        String cur = "";
        while (!deque.isEmpty()) {
            String ope = deque.remove();
            cur = deque.remove();
            result = doingCalculate(ope, result, cur);
        }

        return checkIsInteger(Double.parseDouble(result));
    }

    /**
     * Calculating double numbers with precision
     * @param ope
     * @param first
     * @param second
     * @return
     */
    private String doingCalculate(String ope, String first, String second) {
        int lenFirst = first.indexOf(".") == -1 ? 0 : first.length() -  first.indexOf(".") - 1;
        int lenSecond = second.indexOf(".") == -1 ? 0 : second.length() - second.indexOf(".") - 1;

        long times = (long) Math.pow(10, Math.max(lenFirst, lenSecond));
        long firstLong = (long)(Math.round(Double.parseDouble(first) * times));
        long secondLong = (long)(Math.round(Double.parseDouble(second) * times));

        double res = ope.equals("+") ? (firstLong + secondLong) / (double)times : (firstLong - secondLong) / (double)times;

        return res + "";
    }

    /**
     *
     * @param result
     * @return
     */
    private String checkIsInteger(double result) {
        if (result == Math.floor(result)) {
            long temp = (long)result;
            return temp + "";
        }
        return result +  "";
    }

    /**
     * Format double to keep 6 decimals
     * @param d
     * @return a string
     */
    private String formatDouble(Double d) {
        if (Double.isInfinite(d) || decimalsExceed(d)) {
            DecimalFormat res = new DecimalFormat(".########");
            return res.format(d).toString();
        }

        return d.toString();
    }

    /**
     * check if the decimal exceeds 6 decimals
     * @param d
     * @return true if the double exceeds 6 decimals
     */
    private boolean decimalsExceed(Double d) {
        String text = Double.toString(Math.abs(d));
        int integerPlace = text.indexOf('.');
        if ((integerPlace != -1) && (text.length() - integerPlace - 1 > 8)) {
            return true;
        }

        return false;
    }

    private boolean validNegSign (String s) {
        if (strRecord.length() == 0 ) {
            return true;
        }

        char c = s.charAt(s.length() - 1);
        if (Character.isDigit(c) || c == '.') {
            return false;
        }

        return true;
    }

    /**
     * Makes sure the last input is not operators
     * @param s
     * @return true if last input is not operator
     */
    private boolean checkPreInputValid (String s) {
        if (s.length() == 0 || "+-x/=.".indexOf(s.charAt(s.length() - 1)) != -1) {
            return false;
        }
        return true;
    }
}
