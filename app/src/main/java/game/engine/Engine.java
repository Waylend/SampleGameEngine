package game.engine;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.renderscript.Float2;
import android.renderscript.Float3;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import java.math.BigDecimal;

public abstract class Engine extends Activity implements Runnable, View.OnTouchListener {

    private SurfaceView p_view;
    private Canvas p_canvas;
    private Thread p_thread;
    private boolean p_running, p_paused;
    private int p_pauseCount;
    private Paint p_paintDraw, p_paintFont;
    private Typeface p_typeface;
    private Point[] p_touchPoints;
    private int p_numPoints;
    private long p_preferredFrameRate, p_sleepTime;

    public Engine() {
        Log.d("Engine","Engine Constructor");
        p_view = null;
        p_canvas = null;
        p_thread = null;
        p_running = false;
        p_paused = false;
        p_paintDraw = null;
        p_paintFont = null;
        p_numPoints = 0;
        p_typeface = null;
        p_preferredFrameRate = 40;
        p_sleepTime = 1000 / p_preferredFrameRate;
        p_pauseCount = 0;
    }

    //abstract methods
    public abstract void init();
    public abstract void load();
    public abstract void draw();
    public abstract void update();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Log.d("Engine", "Engine.onCreate start");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

        init();

        p_view = new SurfaceView(this);
        setContentView(p_view);
        p_view.setOnTouchListener(this);
        p_touchPoints = new Point[5];
        for (int n=0; n<5; n++) {
            p_touchPoints[n] = new Point(0,0);
        }
        p_paintDraw = new Paint();
        p_paintDraw.setColor(Color.WHITE);
        p_paintFont = new Paint();
        p_paintFont.setColor(Color.WHITE);
        p_paintFont.setTextSize(24);

        load();

        p_running = true;
        p_thread = new Thread(this);
        p_thread.start();

        Log.d("Engine", "Engine.onCreate end");
    }

    @Override
    public void run() {
        Log.d("Engine", "Engine.run start");

        Timer frameTimer = new Timer();
        int frameCount=0;
        int frameRate=0;
        long starTime=0;
        long timeDiff=0;
        while (p_running) {
            if (p_paused) continue;
            frameCount++;
            starTime = frameTimer.getElapsed();
            if (frameTimer.stopwatch(1000)){
                frameRate = frameCount;
                frameCount = 0;
                p_numPoints = 0;
            }
            update();
            if (beginDrawing()) {
                p_canvas.drawColor(Color.BLUE);
                draw();
                int x = p_canvas.getWidth() - 150;
                p_canvas.drawText("ENGINE", x, 20, p_paintFont);
                p_canvas.drawText(toString(frameRate) + "FPS", x, 40, p_paintFont);
                p_canvas.drawText("Pauses: " + toString(p_pauseCount), x, 60, p_paintFont);
                endDrawing();
            }

            timeDiff = frameTimer.getElapsed() - starTime;
            long updatePeriod = p_sleepTime - timeDiff;
            if (updatePeriod > 0) {
                try {
                    Thread.sleep(updatePeriod);
                }
                catch (InterruptedException e) {}
            }
        }
        Log.d("Engine", "Engine.run end");
        System.exit(RESULT_OK);
    }
    private boolean beginDrawing() {
        if (!p_view.getHolder().getSurface().isValid()) {
            return false;
        }
        p_canvas = p_view.getHolder().lockCanvas();
        return true;
    }
    private void endDrawing() {
        p_view.getHolder().unlockCanvasAndPost(p_canvas);
    }
    @Override
    public void onResume() {
        Log.d("Engine", "Engine.onResume");
        super.onResume();
        p_paused = false;
        p_running = true;
        p_thread = new Thread(this);
        p_thread.start();
    }
    @Override
    public void onPause() {
        Log.d("Engine", "Engine.onPause");
        super.onPause();
        p_paused = true;
        p_running = false;
        while (true) {
            try {
                p_thread.join();
            }
            catch (InterruptedException e) {}
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event){
        p_numPoints = event.getPointerCount();
        if (p_numPoints > 5) p_numPoints = 5;

        for (int n=0; n<p_numPoints; n++) {
            p_touchPoints[n].x = (int)event.getX(n);
            p_touchPoints[n].y = (int)event.getY(n);
        }
        return true;
    }

    //helpers

    public void fatalError(String msg){
        Log.e("FATAL ERROR", msg);
        System.exit(0);
    }
    public void drawText(String text, int x, int y) {
        p_canvas.drawText(text, x, y, p_paintFont);
    }
    public SurfaceView getView() {
        return p_view;
    }
    public Canvas getCanvas() {
        return p_canvas;
    }
    public void setFrameRate(int rate) {
        p_preferredFrameRate = rate;
        p_sleepTime = 1000 / p_preferredFrameRate;
    }
    public int getTouchInputs() {
        return  p_numPoints;
    }
    public Point getTouchPoint(int index) {
        if (index > p_numPoints) index = p_numPoints;
        return  p_touchPoints[index];
    }
    public void setDrawColor(int color) {
        p_paintDraw.setColor(color);
    }
    public void setTextColor(int color) {
        p_paintFont.setColor(color);
    }
    public void setTextSize(int size) {
        p_paintFont.setTextSize((float) size);
    }
    public void setTextSize(float size) {
        p_paintFont.setTextSize(size);
    }

    public enum FontStyles {
        NORMAL (Typeface.NORMAL),
        BOLD (Typeface.BOLD),
        ITALIC (Typeface.ITALIC),
        BOLD_ITALIC (Typeface.BOLD_ITALIC);
        int value;
        FontStyles(int type) {
            this.value = type;
        }
    }
    public void setTextStyle(FontStyles style) {
        p_typeface = Typeface.create(Typeface.DEFAULT, style.value);
        p_paintFont.setTypeface(p_typeface);
    }

    public enum ScreenModes {
        LANDSCAPE (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
        PORTRAIT (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int value;
        ScreenModes(int mode){
            this.value = mode;
        }
    }
    public void setScreenOrientation(ScreenModes mode) {
        setRequestedOrientation(mode.value);
    }

    public double round(double value) {
        return round(value,2);
    }
    public double round(Double value, int precision) {
        try {
            BigDecimal bd = new BigDecimal(value);
            BigDecimal rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
            return rounded.doubleValue();
        }
        catch (Exception e) {
            Log.e("Engine", "round: error rounding number");
        }
        return 0;
    }
    public String toString(int value) {
        return Integer.toString(value);
    }
    public String toString(float value) {
        return Float.toString(value);
    }
    public String toString(double value) {
        return Double.toString(value);
    }
    public String toString(Float2 value) {
        String s = "X:" + round(value.x) + "," + "Y:" + round(value.y);
        return s;
    }
    public String toString(Float3 value) {
        String s = "X:" + round(value.x) + "," + "Y:" + round(value.y) + "," + "Z:" + round(value.z);
        return s;
    }
}
