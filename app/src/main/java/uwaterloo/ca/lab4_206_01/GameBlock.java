package uwaterloo.ca.lab4_206_01;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by sarthak on 06/07/17.
 */

public class GameBlock extends GameBlockTemplate{
    float IMAGE_SCALE = 0.7f;
    int myCoordX, myCoordY;
    RelativeLayout gameboard;
    private TextView blockTV = new TextView(getContext());

    enum axis {X_AXIS, Y_AXIS}

    final float length = 358.75f;
    final int frames = 10;          // the number of frames it takes to complete the animation
    final float adjustment = -77.0f;

    final float offset10 = adjustment*1.535f/2.0f;
    final float offset100 = adjustment*1.535f/2.0f + adjustment*1.535f/3.0f;

    final float tvAdjust_x = -adjustment*2.58f;
    final float tvAdjust_y = -adjustment*1.535f;
    final float top_left_x = 0;

    //boundary constants
    final float top_left_y = 0;
    final float top_right_x = 1435;
    final float top_right_y = 0;
    final float bottom_left_x = 0;
    final float bottom_left_y = 1440;
    final float bottom_right_x = 1435;
    final float bottom_right_y = 1440;

    //position for block
    private float xPix = 0.0f;
    private float yPix = 0.0f;

    //destination coordinates
    private int dest_x = 0;
    private int dest_y = 0;

    //wall (test - debug)
    private int wall = 0;

    //block value (number displayed on block face)
    private int blockNumber;

    private boolean merged = false; // flag to be used by merging algorithm (prevents multiple merges
                                    // with one block
    private boolean kill = false;

    private boolean moving;
    private boolean deleting = false;
    private boolean adding = false;
    private boolean deleteThis = false;

    private GameBlock myMergeBlock;

    private float velocity = 0.0f;
    private int accelCnt = 0; // counter to be multiplied by acceleration
    // since velocity depends on the position of the block
    private float accel = 20.5f;

    private GameLoopTask.directions game_direction = GameLoopTask.directions.NO_MOVEMENT;

    //sets direction of block
    public void setBlockDirection(GameLoopTask.directions dir)
    {
        game_direction = dir;

    }

    //GameBlock constructor
    public GameBlock(Context gbCTX, int CoordX, int CoordY, RelativeLayout thisRL){

        super(gbCTX);
        this.setImageResource(R.drawable.gameblock);
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);
        this.setX(CoordX*length+adjustment);
        this.setY(CoordY*length+adjustment);



        xPix = CoordX*length+adjustment;
        yPix = CoordY*length+adjustment;
        Log.d("COORDINATES", String.format("%f,%f", xPix,yPix));
        blockNumber = Math.random() < 0.75 ? 2:4 ;
        String s = String.format("%d", blockNumber);
        myCoordX = CoordX;
        myCoordY = CoordY;
        gameboard = thisRL;

        gameboard.addView(this);
        blockTV.setText(s);
        blockTV.setTextSize(50.f);
        blockTV.setX(xPix+tvAdjust_x);
        blockTV.setY(yPix+tvAdjust_y);
        blockTV.setTextColor(getResources().getColor(android.R.color.black));
        thisRL.addView(blockTV);
        blockTV.bringToFront();

    }

    // helper function for animate()
    private void moveBlock(float xPix, float yPix) {
        this.setX(xPix);
        this.setY(yPix);

        if (getValue() > 100) {
            blockTV.setX(xPix+tvAdjust_x+offset100);
        } else if (getValue() > 10) {
            blockTV.setX(xPix+tvAdjust_x+offset10);
        } else {
            blockTV.setX(xPix+tvAdjust_x);
        }
        blockTV.setY(yPix+tvAdjust_y);
    }

    //function to animate the movement of the GameBlock
    public boolean animation(axis myAxis,int start,int end){
        //calculates velocity using final and initial positions

//        velocity = (end*length - start*length)/frames;
        velocity = ((end - start)*length + accel*accelCnt)/frames;


        if(myAxis == axis.X_AXIS){
            xPix += velocity;

            Log.w("ANIMATE", String.format("xPix: %f, length: %f", xPix, length));
            //velocity > 0 when moving to the right
            if (velocity > 0) {
                if (xPix / length >= end) {
                    xPix = end * length + adjustment;
                    myCoordX = end;
//                    game_direction = GameLoopTask.directions.NO_MOVEMENT;
                    return true;
                }
            }
            else {
                //adjustment applies only to the left of the block
                //that's why we only include the adjustment here
                if ((xPix-adjustment) / length < end) {
                    xPix = end * length + adjustment;
                    myCoordX = end;
//                    game_direction = GameLoopTask.directions.NO_MOVEMENT;
                    return true;
                }
            }
        }
        if(myAxis == axis.Y_AXIS){
            yPix += velocity;
            if (velocity > 0) {
                if (yPix / length >= end) {
                    yPix = end * length + adjustment;
                    myCoordY = end;
//                    game_direction = GameLoopTask.directions.NO_MOVEMENT;
                    return true;
                }
            }
            else {
                if ((yPix - adjustment) / length < end) {
                    yPix = end * length + adjustment;
                    myCoordY = end;
//                    game_direction = GameLoopTask.directions.NO_MOVEMENT;
                    return true;
                }
            }
        }
        moveBlock(xPix, yPix);


        return false;
    }

    //moved is called at every frame
    //returns false to keep moving
    //returns true when the block reaches its destination
    public boolean moved(){
        //return true if object has already "moved"
        if(game_direction == GameLoopTask.directions.NO_MOVEMENT){
            return true;
        }
        if(game_direction == GameLoopTask.directions.LEFT) {
            Log.i("MOVED - LEFT", String.format("X: %d, Y: %d", myCoordX, myCoordY));
            if (!animation(axis.X_AXIS, myCoordX, dest_x)) {
                return false;
            }
            else {
                myCoordX = dest_x;
                return true;
            }
        }

        else if(game_direction == GameLoopTask.directions.RIGHT){
            Log.i("MOVED - RIGHT", String.format("X: %d, Y: %d", myCoordX, myCoordY));
            if (!animation(axis.X_AXIS, myCoordX, dest_x)) {
                return false;
            }
            else {
                myCoordX = dest_x;
                return true;
            }
        }
        else if(game_direction == GameLoopTask.directions.UP){
            Log.i("MOVED - UP", String.format("X: %d, Y: %d", myCoordX, myCoordY));
            if (!animation(axis.Y_AXIS, myCoordY, dest_y)) {
                return false;
            }
            else {
                myCoordY = dest_y;
                return true;
            }
        }
        else if(game_direction == GameLoopTask.directions.DOWN){
            Log.i("MOVED - DOWN", String.format("X: %d, Y: %d", myCoordX, myCoordY));
            if (!animation(axis.Y_AXIS, myCoordY, dest_y)) {
                return false;
            }
            else {
                myCoordY = dest_y;
                return true;
            }
        }
        return false;
    }

    public void setDestination(){

    }

    public void logBlockPos(){
//        int numOfBlocks = 0;
//        for (int i=0; i<4; i++){
//            for (int j=0; j<4; j++){
//                if (destBlockPosArr[i][j] != null){
//                    Log.i("BLOCK CLASS", String.format("Block Value: %d", destBlockPosArr[i][j].blockNumber));
//                    Log.i("BLOCK CLASS", String.format("Block Coord X: %d", destBlockPosArr[i][j].myCoordX));
//                    Log.i("BLOCK CLASS", String.format("Block Coord Y: %d", destBlockPosArr[i][j].myCoordY));
//                    numOfBlocks++;
//                }
//            }
//        }
//        Log.i("BLOCK", String.format("Number of blocks: %d", numOfBlocks));
    }

    public void logBlockBefore(){
//        int numOfBlocks = 0;
//        for (int i=0; i<4; i++){
//            for (int j=0; j<4; j++){
//                if (destBlockPosArr[i][j] != null){
//                    Log.i("BLOCK BEFORE", String.format("Block Value: %d", destBlockPosArr[i][j].blockNumber));
//                    Log.i("BLOCK BEFORE", String.format("Block Coord X: %d, index: %d", destBlockPosArr[i][j].myCoordX, i));
//                    Log.i("BLOCK BEFORE", String.format("Block Coord Y: %d, index: %d", destBlockPosArr[i][j].myCoordY, j));
//                    numOfBlocks++;
//                }
//            }
//        }
//        Log.i("BLOCK BEFORE", String.format("Number of blocks: %d", numOfBlocks));
    }

    public void logBlockAfter(){
//        int numOfBlocks = 0;
//        for (int i=0; i<4; i++){
//            for (int j=0; j<4; j++){
//                if (destBlockPosArr[i][j] != null){
//                    Log.i("BLOCK AFTER", String.format("Block Value: %d", destBlockPosArr[i][j].blockNumber));
//                    Log.i("BLOCK AFTER", String.format("Block Coord X: %d, index: %d", destBlockPosArr[i][j].myCoordX, i));
//                    Log.i("BLOCK AFTER", String.format("Block Coord Y: %d, index: %d", destBlockPosArr[i][j].myCoordY, j));
//                    numOfBlocks++;
//                }
//            }
//        }
//        Log.i("BLOCK AFTER", String.format("Number of blocks: %d", numOfBlocks));
    }

    public void doubleValue() { blockNumber *= 2;};

    public void updateTV() {
        String s = String.format("%d", blockNumber);
        blockTV.setText(s);
    }


    public void setValue(int val) { blockNumber = val; }

    public void setMerged(boolean m) { merged = m; }

    public void kill(){
        gameboard.removeView(blockTV);
        gameboard.removeView(this);
    }

    public void setDestX(int x){
        dest_x = x;
    }

    public void setDestY(int y){
        dest_y = y;
    }

    public void setWall(int w) { wall = w; }


//    public float getVelocity() { return velocity; }

    public int getValue() { return blockNumber; }

    public boolean getMerged() { return merged; }

    public int getDestX(){
        return dest_x;
    }

    public int getDestY(){
        return dest_y;
    }

    public int getWall() { return wall; }



}
