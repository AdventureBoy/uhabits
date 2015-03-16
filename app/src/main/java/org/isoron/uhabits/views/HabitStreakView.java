package org.isoron.uhabits.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class HabitStreakView extends View
{
    private Habit habit;
    private int columnWidth, columnHeight, nColumns;

    private Paint pText, pBar;
    private long streaks[];

    private long streakStart[], streakEnd[], streakLength[];
    private long maxStreakLength;

    private int barHeaderHeight;

    private int[] colors;

    public HabitStreakView(Context context, Habit habit, int columnWidth)
    {
        super(context);
        this.habit = habit;
        this.columnWidth = columnWidth;

        pText = new Paint();
        pText.setColor(Color.LTGRAY);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(columnWidth * 0.5f);
        pText.setAntiAlias(true);

        pBar = new Paint();
        pBar.setTextAlign(Paint.Align.CENTER);
        pBar.setTextSize(columnWidth * 0.5f);
        pBar.setAntiAlias(true);

        columnHeight = 8 * columnWidth;
        barHeaderHeight = columnWidth;

        colors = new int[4];

        colors[0] = Color.rgb(230, 230, 230);
        colors[3] = habit.color;
        colors[1] = ColorHelper.mixColors(colors[0], colors[3], 0.66f);
        colors[2] = ColorHelper.mixColors(colors[0], colors[3], 0.33f);

        fetchStreaks();
    }

    private void fetchStreaks()
    {
        streaks = habit.getStreaks();
        streakStart = new long[streaks.length / 2];
        streakEnd = new long[streaks.length / 2];
        streakLength = new long[streaks.length / 2];

        for(int i=0; i<streaks.length / 2; i++)
        {
            streakStart[i] = streaks[i * 2];
            streakEnd[i] = streaks[i * 2 + 1];
            streakLength[i] = (streakEnd[i] - streakStart[i]) / DateHelper.millisecondsInOneDay + 1;
            maxStreakLength = Math.max(maxStreakLength, streakLength[i]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), columnHeight + 2*barHeaderHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        nColumns = w / columnWidth;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float lineHeight = pText.getFontSpacing();
        float barHeaderOffset = lineHeight * 0.4f;

        int start = Math.max(0, streakStart.length - nColumns);
        SimpleDateFormat dfMonth = new SimpleDateFormat("MMM");

        String previousMonth = "";

        for (int offset = 0; offset < nColumns && start+offset < streakStart.length; offset++)
        {
            String month = dfMonth.format(streakStart[start+offset]);

            long l = streakLength[offset+start];
            double lRelative = ((double) l) / maxStreakLength;

            pBar.setColor(colors[(int) Math.floor(lRelative*3)]);

            int height = (int) (columnHeight * lRelative);
            Rect r = new Rect(0,0,columnWidth-2, height);
            r.offset(offset * columnWidth, barHeaderHeight + columnHeight - height);

            canvas.drawRect(r, pBar);
            canvas.drawText(Long.toString(streakLength[offset+start]), r.centerX(), r.top - barHeaderOffset, pBar);

            if(!month.equals(previousMonth))
                canvas.drawText(month, r.centerX(), r.bottom + lineHeight * 1.2f, pText);

            previousMonth = month;
        }
    }
}