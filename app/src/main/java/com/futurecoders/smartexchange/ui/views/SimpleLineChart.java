package com.futurecoders.smartexchange.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleLineChart extends View {

    private List<Float> sellValues = new ArrayList<>();
    private List<Float> buyValues = new ArrayList<>();
    private List<String> xLabels = new ArrayList<>();

    private Paint sellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint buyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float scaleX = 1f;
    private float panX = 0f;
    private float lastTouchX;
    private boolean isPanning = false;
    private ScaleGestureDetector scaleDetector;

    public SimpleLineChart(Context context) {
        super(context);
        init(context);
    }

    public SimpleLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        sellPaint.setColor(Color.RED);
        sellPaint.setStrokeWidth(4f);
        sellPaint.setStyle(Paint.Style.STROKE);

        buyPaint.setColor(Color.BLUE);
        buyPaint.setStrokeWidth(4f);
        buyPaint.setStyle(Paint.Style.STROKE);

        pointPaint.setColor(Color.DKGRAY);
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(28f);

        axisPaint.setColor(Color.GRAY);
        axisPaint.setStrokeWidth(2f);

        legendPaint.setTextSize(28f);
        legendPaint.setColor(Color.BLACK);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleX *= detector.getScaleFactor();
                scaleX = Math.max(1f, Math.min(scaleX, 5f));
                invalidate();
                return true;
            }
        });
    }

    public void setData(List<Float> sell, List<Float> buy, List<String> labels) {
        this.sellValues = sell != null ? sell : new ArrayList<>();
        this.buyValues = buy != null ? buy : new ArrayList<>();
        this.xLabels = labels != null ? labels : new ArrayList<>();
        // reset pan/zoom
        scaleX = 1f;
        panX = 0f;
        invalidate();
    }

    public void clear() {
        sellValues.clear();
        buyValues.clear();
        xLabels.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float left = 60f;
        float right = 20f;
        float top = 20f;
        float bottom = 80f;

        float usableW = w - left - right;
        float usableH = h - top - bottom;

        int n = Math.max(Math.max(sellValues.size(), buyValues.size()), xLabels.size());
        if (n == 0) return;

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for (Float v : sellValues) if (v != null) { max = Math.max(max, v); min = Math.min(min, v); }
        for (Float v : buyValues) if (v != null) { max = Math.max(max, v); min = Math.min(min, v); }
        if (max == Float.MIN_VALUE || min == Float.MAX_VALUE) return;
        if (max == min) { max += 1f; min -= 1f; }

        // apply scale
        float scaledW = usableW * scaleX;

        // ensure panX bounds
        float minPan = Math.min(0, usableW - scaledW);
        float maxPan = 0;
        panX = Math.max(minPan, Math.min(maxPan, panX));

        // draw axes
        canvas.drawLine(left, top + usableH, left + usableW, top + usableH, axisPaint);

        // draw lines
        if (!sellValues.isEmpty()) {
            drawSeries(canvas, sellValues, left, top, usableW, usableH, max, min, scaledW, Color.RED, sellPaint);
        }
        if (!buyValues.isEmpty()) {
            drawSeries(canvas, buyValues, left, top, usableW, usableH, max, min, scaledW, Color.BLUE, buyPaint);
        }

        // draw x labels (sparse)
        int labelCount = Math.min(xLabels.size(), 6);
        for (int i = 0; i < labelCount; i++) {
            int idx = (int) Math.round((i * (xLabels.size() - 1)) / (double) Math.max(1, labelCount - 1));
            float x = left + panX + (scaledW) * (idx) / Math.max(1, n - 1);
            String s = xLabels.get(idx);
            // format yyyy-MM-dd -> MM-dd
            String out = s;
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s);
                out = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(d);
            } catch (Exception ignored) {}
            canvas.drawText(out, x - 20f, top + usableH + 40f, textPaint);
        }

        // draw legend
        float legendX = w - 180f;
        float legendY = top + 10f;
        canvas.drawRect(legendX, legendY, legendX + 12f, legendY + 12f, sellPaint);
        canvas.drawText("Sell", legendX + 20f, legendY + 12f, legendPaint);
        canvas.drawRect(legendX, legendY + 24f, legendX + 12f, legendY + 36f, buyPaint);
        canvas.drawText("Buy", legendX + 20f, legendY + 36f, legendPaint);
    }

    private void drawSeries(Canvas canvas, List<Float> values, float left, float top, float usableW, float usableH, float max, float min, float scaledW, int color, Paint paint) {
        Paint p = paint;
        p.setColor(color);
        if (values.size() == 0) return;
        int n = values.size();
        float prevX = 0, prevY = 0;
        boolean hasPrev = false;
        for (int i = 0; i < n; i++) {
            Float v = values.get(i);
            if (v == null) continue;
            float x = left + panX + (scaledW) * (i) / Math.max(1, n - 1);
            float y = top + ((max - v) * usableH) / (max - min);
            if (hasPrev) {
                canvas.drawLine(prevX, prevY, x, y, p);
            }
            canvas.drawCircle(x, y, 4f, pointPaint);
            prevX = x; prevY = y; hasPrev = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                isPanning = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isPanning && !scaleDetector.isInProgress()) {
                    float x = event.getX();
                    float dx = x - lastTouchX;
                    lastTouchX = x;
                    panX += dx;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPanning = false;
                return true;
        }
        return super.onTouchEvent(event);
    }
}

