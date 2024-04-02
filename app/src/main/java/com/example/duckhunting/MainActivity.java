package com.example.duckhunting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.res.Resources;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;

public class MainActivity extends Activity {

    private long timeLeftInMillis ; // thời gian của game
    private TextView countdownText; // thời gian đếm ngược
    private GameView gameView;
    private GestureDetector gestureDetector;
    private Game game;
    private SoundPool soundPool;
    private int fireSoundId;
    private int hitSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();
        int statusBarHeight = 0;
        int statusBarId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarId > 0) {
            statusBarHeight = res.getDimensionPixelSize(statusBarId);
        }
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        gameView = new GameView(this, size.x, size.y - statusBarHeight);

        FrameLayout gameLayout = new FrameLayout(this);
        gameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Text hiển thị tg đếm ngược
        countdownText = new TextView(this);
        FrameLayout.LayoutParams countdownParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        countdownParams.setMargins(30, 30, 0, 0); // Left, Top, Right, Bottom
        countdownText.setLayoutParams(countdownParams);
        countdownText.setTextSize(20);

        // text hiển thị số vịt bắn được
        TextView ducksShotText = new TextView(this);
        FrameLayout.LayoutParams ducksParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ducksParams.setMargins(0, 30, 30, 0); // Left, Top, Right, Bottom
        ducksParams.gravity = Gravity.RIGHT;
        ducksShotText.setLayoutParams(ducksParams);
        ducksShotText.setTextSize(20);

        // Add views to layout
        gameLayout.addView(gameView);
        gameLayout.addView(countdownText);
        gameLayout.addView(ducksShotText);

        setContentView(gameLayout);

        Timer gameTimer = new Timer();
        gameTimer.schedule(new GameTimerTask(gameView), 0, GameView.DELTA_TIME);

        game = gameView.getGame();
        TouchHandler touchHandler = new TouchHandler();
        gestureDetector = new GestureDetector(this, touchHandler);
        gestureDetector.setOnDoubleTapListener(touchHandler);

        SoundPool.Builder poolBuilder = new SoundPool.Builder();
        poolBuilder.setMaxStreams(2);
        soundPool = poolBuilder.build();
        fireSoundId = soundPool.load(this, R.raw.cannon_fire, 1);
        hitSoundId = soundPool.load(this, R.raw.duck_hit, 1);

        timeLeftInMillis = 180000; // Set 3m
        startCountdown(); // start countdown

        // cập nhật số vịt được bắn
        final Handler handler = new Handler();
        final Runnable updateTextRunnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                ducksShotText.setText("Ducks Shot: " + game.getDucksShotCount());
                handler.postDelayed(this, 1000); // cập nhật mỗi giây
            }
        };
        handler.post(updateTextRunnable);
    }


    private void startCountdown() {
        CountDownTimer countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                //get left time to speed up
                game.setTimeLeftInMillis(timeLeftInMillis);
                game.adjustDuckSpeedBasedOnTime();
                updateCountdownText();

            }

            public void onFinish() {
                runOnUiThread(() -> {
                    // lấy điểm cao nhất
                    SharedPreferences prefs = getSharedPreferences("DuckHuntingPrefs", MODE_PRIVATE);
                    int highScore = prefs.getInt("HighScore", 0);
                    int currentScore = game.getDucksShotCount();
                    if (currentScore > highScore) {
                        // update high score
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("HighScore", currentScore);
                        editor.apply();
                        highScore = currentScore;
                    }

                    // khởi tạo dialog dừng game
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.time_over_title))
                            .setMessage(getString(R.string.duck_shot_message, currentScore) + "\n" + getString(R.string.high_score_message, highScore))
                            .setPositiveButton(getString(R.string.replay_button), (dialog, which) -> restartGame())
                            .setNegativeButton(getString(R.string.exit_button), (dialog, which) -> finish());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }
        }.start();
    }


    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countdownText.setText(timeFormatted);
    }


    public void restartGame() {
        // reset game
        game.restartGame();
        timeLeftInMillis = 180000;
        updateCountdownText();
        startCountdown();
    }

    public void playHitSound(){
        soundPool.play(hitSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class TouchHandler extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            if (!game.isBulletFired()) {
                game.fireBullet();
                soundPool.play(fireSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            updateCannon(e);
            return true;
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            updateCannon(e2);
            return true;
        }

        public void updateCannon(MotionEvent event){
            float x = event.getX() - game.getCannonCenter().x;
            float y = game.getCannonCenter().y - event.getY();
            float angle = (float)Math.atan2(y,x);
            game.setCannonAngle(angle);
        }
    }
}