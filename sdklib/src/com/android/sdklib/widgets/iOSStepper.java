package com.android.sdklib.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.android.sdklib.widgets.base_elements.iOSBaseElement;
import com.android.sdklib.widgets.base_elements.iOSLinearLayout;
import com.android.sdklib.widgets.base_elements.iOSView;


public class iOSStepper extends View implements iOSBaseElement {

    Paint paint = new Paint();

    public iOSStepper(Context context) {
        super(context);
    }

    public iOSStepper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        //Element border
        canvas.drawRoundRect(0, 0, w, h, 10, 10, paint);

        //Separate line
        canvas.drawLine(w / 2, 0, w / 2, h, paint);


        Paint plusMinusPaint = new Paint();
        plusMinusPaint.setColor(Color.BLUE);
        plusMinusPaint.setStyle(Paint.Style.STROKE);
        plusMinusPaint.setStrokeWidth(3);

        //Plus
        canvas.drawLine(w - w/4, h/4, w - w/4, h - h/4, plusMinusPaint);
        canvas.drawLine(w/2 + w/8, h/2, w - w/8, h/2, plusMinusPaint);

        //Minus
        canvas.drawLine(w/2 - w/8, h/2, w/8, h/2, plusMinusPaint);
    }
}
