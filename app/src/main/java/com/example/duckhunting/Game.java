package com.example.duckhunting;

import android.graphics.Rect;
import android.graphics.Point;
import android.util.Log;

import java.util.Random;

public class Game {
    private Rect huntingRect;
    private int deltaTime;
    //Manage Duck
    private Rect duckRect;
    private int duckWidth;
    private int duckHeight;
    private float duckSpeed;
    private boolean duckShot;

    private  int duckShotCount = 0;

    //đảm bảo vịt tăng tốc chỉ 1 lần
    private boolean speedAdjustedForOneMinute = false;
    private boolean speedAdjustedForTwoMinutes = false;
    private boolean speedAdjustedFor30s = false;


    //Manage Canon
    private Point cannonCenter;
    private int cannonRadius;
    private int barrelLength;
    private int barrelRadius;
    private float cannonAngle;

    //Manage Bullet
    private Point bulletCenter;
    private int bulletRadius;
    private boolean bulletFired;
    private float bulletAngle;
    private float bulletSpeed;

    private Random random;

    //game time
    private long timeLeftInMillis;


    public Game(Rect duckRect, float duckSpeed, int bulletRadius, float bulletSpeed){
        setDuckRect(duckRect);
        setDuckSpeed(duckSpeed);
        setDuckShot(false);
        setBulletRadius(bulletRadius);
        setBulletSpeed(bulletSpeed);
        bulletFired = false;
        random = new Random();
        cannonAngle = (float)Math.PI/4;
    }
    public Rect getDuckRect(){
        return duckRect;
    }
    public void setDeltaTime(int deltaTime) {
        this.deltaTime = deltaTime;
    }

    public void setDuckHeight(int duckHeight) {
        this.duckHeight = duckHeight;
    }

    public void setDuckRect(Rect duckRect) {
        this.duckRect = duckRect;
        setDuckWidth(duckRect.right-duckRect.left);
        setDuckHeight(duckRect.bottom-duckRect.top);
    }

    public void setDuckShot(boolean duckShot) {
        this.duckShot = duckShot;
    }

    public void setDuckSpeed(float duckSpeed) {
        this.duckSpeed = duckSpeed;
    }

    public void setDuckWidth(int duckWidth) {
        this.duckWidth = duckWidth;
    }

    public boolean isDuckShot() {
        return duckShot;
    }

    public float getDuckSpeed() {
        return duckSpeed;
    }

    public int getDeltaTime() {
        return deltaTime;
    }

    public int getDuckHeight() {
        return duckHeight;
    }

    public int getDuckWidth() {
        return duckWidth;
    }
    public int getDucksShotCount() {
        return duckShotCount;
    }

    public void setHuntingRect(Rect huntingRect) {
        this.huntingRect = huntingRect;
    }

    public Rect getHuntingRect() {
        return huntingRect;
    }

    public Point getCannonCenter() {
        return cannonCenter;
    }
    public int getCannonRadius(){
        return cannonRadius;
    }

    public int getBarrelLength() {
        return barrelLength;
    }

    public int getBarrelRadius() {
        return barrelRadius;
    }

    public float getCannonAngle() {
        return cannonAngle;
    }

    public void setCannonAngle(float cannonAngle) {
        if (cannonAngle>=0 && cannonAngle <= Math.PI/2)
            this.cannonAngle = cannonAngle;
        else if (cannonAngle < 0)
            this.cannonAngle = 0;
        else
            this.cannonAngle = (float)Math.PI/2;
        if (!isBulletFired())
            loadBullet();
    }
    public void setCannon(Point cannonCenter, int cannonRadius, int barrelLength, int barrelRadius){
        if (cannonCenter!=null && cannonRadius>0 &&barrelLength>0){
            this.cannonCenter = cannonCenter;
            this.cannonRadius = cannonRadius;
            this.barrelLength = barrelLength;
            this.barrelRadius = barrelRadius;
            this.bulletCenter = new Point(
                    (int) (cannonCenter.x + cannonRadius*Math.cos(cannonAngle)),
                    (int)(cannonCenter.y - cannonRadius*Math.sin(cannonAngle))
            );
        }
    }

    public Point getBulletCenter() {
        return bulletCenter;
    }
    public int getBulletRadius() {
        return bulletRadius;
    }

    public void setBulletRadius(int bulletRadius) {
        if (bulletRadius>0)
            this.bulletRadius = bulletRadius;
    }

    public void setBulletSpeed(float bulletSpeed) {
        if (bulletSpeed>0)
            this.bulletSpeed = bulletSpeed;
    }

    public boolean isBulletFired() {
        return bulletFired;
    }

    public void fireBullet(){
        bulletFired = true;
        bulletAngle = cannonAngle;
    }
    public void moveBullet(){
        bulletCenter.x += bulletSpeed*Math.cos(bulletAngle) * deltaTime;
        bulletCenter.y -= bulletSpeed*Math.sin(bulletAngle) * deltaTime;
    }
    public boolean bulletOffScreen(){
        return bulletCenter.x - bulletRadius > huntingRect.right || bulletCenter.y + bulletRadius <0;
    }
    public void loadBullet(){
        bulletFired = false;
        bulletCenter.x = (int) (cannonCenter.x + cannonRadius*Math.cos(cannonAngle));
        bulletCenter.y = (int) (cannonCenter.y - cannonRadius*Math.sin(cannonAngle));
    }
    public void startDuckFromRightTopHalf(){
        duckRect.left=huntingRect.right;
        duckRect.right=duckRect.left+duckWidth;
        duckRect.top= random.nextInt(huntingRect.bottom/2);
        duckRect.bottom=duckRect.top+duckHeight;
    }

    public void moveDuck(){
        if (!duckShot) {
            duckRect.left -= duckSpeed * deltaTime;
            duckRect.right -= duckSpeed * deltaTime;
        }else{
            duckRect.top += 5 * duckSpeed * deltaTime;
            duckRect.bottom += 5 * duckSpeed * deltaTime;
        }
        //Log.w("moveDuck","left="+getDuckRect().left);
    }
    public boolean duckOffScreen(){
        return duckRect.right<0 || duckRect.left > huntingRect.right
                || duckRect.top >huntingRect.bottom || duckRect.bottom <0;
    }
    public boolean duckHit(){
        return duckRect.intersects(bulletCenter.x - bulletRadius, bulletCenter.y-bulletRadius,
                bulletCenter.x+bulletRadius,bulletCenter.y+bulletRadius);
    }


    // đánh dấu vịt bị bắn thì tăng số lượng vịt bắn dc lên 1
    public void markDuckShot() {
        if (!duckShot) {
            duckShot = true;
            duckShotCount++;
        }
    }

    //hàm khởi động lại game
    public void restartGame() {

        timeLeftInMillis = 180000;

        //duck di chuyển lại từ đầu
        startDuckFromRightTopHalf();

        //đặt lại tốc độ vịt
        duckSpeed = 0.05f;

        //vịt bắn dc
        duckShotCount = 0;

        //đặt lại biến ktra tốc độ vịt
        speedAdjustedForTwoMinutes = false;
        speedAdjustedForOneMinute = false;
        speedAdjustedFor30s = false;

        // canon and duckShot
        duckShot = false;
        bulletFired = false;
        loadBullet();

    }
    public void setTimeLeftInMillis(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
        adjustDuckSpeedBasedOnTime();
    }

    public void adjustDuckSpeedBasedOnTime() {
        //speed up just one time

        if (timeLeftInMillis <= 120000 && !speedAdjustedForTwoMinutes) { // 2m
            duckSpeed *= 1.5;
            speedAdjustedForTwoMinutes = true;
        }
        else if (timeLeftInMillis <= 60000 && !speedAdjustedForOneMinute) { // 1m
            duckSpeed *= 2.8;
            speedAdjustedForOneMinute = true;
        }
        else if (timeLeftInMillis <= 30000 && !speedAdjustedFor30s) { // 30s
            duckSpeed *= 2.0;
            speedAdjustedFor30s = true;
        }

    }
}
