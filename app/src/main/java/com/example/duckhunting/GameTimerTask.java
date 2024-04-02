package com.example.duckhunting;

import java.util.TimerTask;

public class GameTimerTask extends TimerTask {
    private Game game;
    private GameView gameView;

    public GameTimerTask(GameView view){
        this.gameView=view;
        game = view.getGame();
        game.startDuckFromRightTopHalf();
    }
    public void run(){
        game.moveDuck();
        if (game.bulletOffScreen())
            game.loadBullet();
        else if (game.isBulletFired())
            game.moveBullet();
        if (game.duckOffScreen()) {
            game.setDuckShot(false);
            game.startDuckFromRightTopHalf();
        } else if (game.duckHit()) {
            game.markDuckShot();
            game.setDuckShot(true);
            ((MainActivity) gameView.getContext()).playHitSound();
            game.loadBullet();
        }
        gameView.postInvalidate();
    }
}
