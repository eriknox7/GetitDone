package com.example.getitdone2;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class GradientTextView extends AppCompatTextView {

    public GradientTextView(Context context) {
        super(context);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Get current text color
        int[] colors = {0xFFBA2B60,0xFFFBA939, 0xFFBA2B60, 0xFFBA2B60}; // Start color and end color

        // Create a LinearGradient text shader
        Shader textShader = new LinearGradient(0,0, getWidth(), getTextSize(), colors, null, Shader.TileMode.CLAMP);

        // Apply the shader to the paint
        getPaint().setShader(textShader);

        // Call super.onDraw() to draw the text with the shader
        super.onDraw(canvas);
    }
}