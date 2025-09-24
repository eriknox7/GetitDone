package com.example.getitdone2;
import android.text.style.AbsoluteSizeSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.EditText;

public class CustomHintSize {
    public static void set(EditText component, String hint, int hintSizeSp) {
        SpannableString spannableString = new SpannableString(hint);
        spannableString.setSpan(new AbsoluteSizeSpan(hintSizeSp, true), 0, hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        component.setHint(spannableString);
    }
}