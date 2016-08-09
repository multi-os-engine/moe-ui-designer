/*
Copyright 2014-2016 Intel Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.android.sdklib.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.android.sdklib.widgets.base_elements.iOSBaseElement;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class iOSTextPicker extends View implements  iOSBaseElement {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String pickerList [] = {"Mountain View", "Sunnyvale",
            "Cupertino", "Santa Clara", "San Jose"};

    Rect textRect = new Rect();
    float padding = 8;
    float linePadding = 15;


    public iOSTextPicker(Context context) {
        super(context);
    }

    public iOSTextPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);


        // Mountain View
        paint.setTextSize(getTextSizeForWidth(paint, width, pickerList[0]) - 10);
        paint.getTextBounds(pickerList[0], 0, pickerList[0].length(), textRect);
        canvas.drawText(pickerList[0], width / 2 - textRect.width() / 2, height / 5, paint);

        paint.setTextSize(getTextSizeForWidth(paint, width, pickerList[1]) - 10);
        paint.getTextBounds(pickerList[1], 0, pickerList[1].length(), textRect);
        canvas.drawText(pickerList[1], width/2 - textRect.width()/ 2, (height / 5) * 2, paint);

        //Separate line
        canvas.drawLine(0, (height/5)*2 + linePadding, width, (height/5)*2 + linePadding, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(getTextSizeForWidth(paint, width, pickerList[3]) - 2);
        paint.getTextBounds(pickerList[2], 0, pickerList[2].length(), textRect);
        canvas.drawText(pickerList[2], width / 2 - textRect.width() / 2, (height / 5) * 3, paint);
        paint.setColor(Color.GRAY);

        //Separate line
        canvas.drawLine(0, (height/5)*3 + linePadding, width, (height / 5) * 3 + linePadding, paint);

        paint.setTextSize(getTextSizeForWidth(paint, width, pickerList[3]) - 6);
        paint.getTextBounds(pickerList[3], 0, pickerList[3].length(), textRect);
        canvas.drawText(pickerList[3], width/2 - textRect.width() / 2, (height/5)*4, paint);

        paint.setTextSize(getTextSizeForWidth(paint, width, pickerList[4]) - 16);
        paint.getTextBounds(pickerList[4], 0, pickerList[4].length(), textRect);
        canvas.drawText(pickerList[4], width/2 - textRect.width()/2, (height/5)*5, paint);

    }


    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    private float getTextSizeForWidth(Paint paint, float desiredWidth,
                                     String text) {

        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        return desiredTextSize;
    }

}
