package uwaterloo.ca.lab4_206_01;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by sarthak on 06/07/17.
 */

public class GameLoopTask extends TimerTask {
    private Activity thisActivity;
    private RelativeLayout thisRL;
    private Context gameloopCTX;
    enum directions {UP, DOWN, LEFT, RIGHT, NO_MOVEMENT};
    directions dir = directions.NO_MOVEMENT;

    enum endGameCond {CHECKING, WAITING};
    endGameCond endGame = endGameCond.WAITING;
    GameBlock[][] blockPositions = new GameBlock[4][4];
    GameBlock[][] destBlockPositions = new GameBlock[4][4];

    public boolean gameOverFlag = false;


    // timer to prevent being stuck in a move state (LEFT, RIGHT, UP, DOWN)
    public Timer watchdog = new Timer();
    int wdDelay = 1000;


    enum moveState {MOVING, LOGGING, WAITING}
    moveState curMoveState = moveState.WAITING;


    int counter  = 0;

    //constructor for GameLoopTask
    public GameLoopTask(Activity myActivity, RelativeLayout myRL, Context myContext ) {
        thisActivity = myActivity;
        thisRL = myRL;
        gameloopCTX = myContext;
        //creates a GameBlock at (0,0)
        Log.i("BLOCKS", "Before logs");
//        logBlockPositions("REAL BLOCK", blockPositions);
        resetBlkArr();
//        logBlockPositions("REAL BLOCK", blockPositions);
        spawnBlock(3);
//        logBlockPositions("REAL BLOCK", blockPositions);
        Log.i("BLOCKS", "After logs");

    }

    //factory method to create a GameBlock
    private void createBlock(){
        Random rand  = new Random();
        int x_pos = rand.nextInt(4);
        int y_pos = rand.nextInt(4);
        blockPositions[x_pos][y_pos] = new GameBlock(gameloopCTX, x_pos , y_pos, thisRL);
    }

    private void spawnBlock(int numOfBlocks) {
        Random rand = new Random();
        Map map_x = new HashMap();
        Map map_y = new HashMap();
        int hashCnt = 0;    // hash counter
        int x_pos = 0;
        int y_pos = 0;

        int newBlockPos = 0;    // temporary var that stores a random hash value


        for (int k=0; k<numOfBlocks; k++) {

            for (int i=0; i<4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (blockPositions[i][j] == null) {
                        map_x.put(hashCnt, i);
                        map_y.put(hashCnt++, j);
                    }
                }
            }

            newBlockPos = rand.nextInt(hashCnt);

            x_pos = (int)map_x.get(newBlockPos);
            y_pos = (int)map_y.get(newBlockPos);

            Log.i("SPAWN", String.format("#####-----##### x, y: %d, %d #####-----#####", x_pos, y_pos));

            blockPositions[x_pos][y_pos] = new GameBlock(gameloopCTX, x_pos , y_pos, thisRL);

            hashCnt = 0;
        }

    }

    //sets direction
    public void setDirection(directions newDir){
        Log.d("Setting", "Direction");
        dir = newDir;
        for (int i=0; i<4; i++) {
            for (int j = 0; j < 4; j++) {
                if (blockPositions[i][j] != null) {
                    blockPositions[i][j].setBlockDirection(dir);
                }
            }
        }

//        Log.d("CREATION", "Creating a new block");
        //createBlock();
        if(dir == directions.LEFT ){
            Log.d("DIRECTION is ", "LEFT");
        }
        else if(dir == directions.RIGHT){
            Log.d("DIRECTIONS is ", "RIGHT");
        }
        else if(dir == directions.UP){
            Log.d("DIRECTION IS ", "UP");
        }
        else if(dir == directions.DOWN){
            Log.d("DIRECTION IS ", "DOWN");
        }
    }

    private void collisDetect(int wallTo, int wallFrom, GameBlock.axis curAxis){
        int wallChange = (wallFrom - wallTo)/Math.abs(wallFrom-wallTo);
        int wall = wallTo;
        Log.i("WALL_DEBUG", String.format("wall: %d", wall));
        Log.i("WALL_DEBUG", String.format("wallTo: %d", wallTo));
        Log.i("WALL_DEBUG", String.format("wallFrom: %d", wallFrom));
        Log.i("WALL_DEBUG", String.format("wallChange: %d", wallChange));

        switch(curAxis) {
            case X_AXIS:

                for (int i = 0; i <= 3; i++) {
                    wall = wallTo;
                    for (int j = wallTo; j != wallFrom+wallChange; j += wallChange) {
                        if (blockPositions[j][i] != null) {
                            blockPositions[j][i].setDestX(wall);
                            blockPositions[j][i].setDestY(i);
                            blockPositions[j][i].setWall(wall);
                            Log.i("IIJJ", String.format("i  i : %d %d\nj  j-: %d %d", i, i, j, j - wallChange));

                            wall += wallChange;

                        }
                    }
                }

                //merge algorithm X
                for (int i = 0; i <= 3; i++) {
                    wall = wallTo + wallChange;
                    for (int j = wallTo; j != wallFrom + wallChange; j += wallChange) {
                        if (blockPositions[j][i] != null) {


                            if ((0 <= (j - wallChange)) && ((j - wallChange) <= 3)) {
                                Log.i("IIJJ - FOR", String.format("i  i : %d %d\nj  j-: %d %d", i, i, j, j - wallChange));
                                inner:
                                for (int k = 1; k < 4; k++) {

                                    if ((0 <= (j - k*wallChange)) && ((j - k*wallChange) <= 3)) {
                                        if (blockPositions[j - k*wallChange][i] != null) {

                                            mergeXhelp(i, j, k*wallChange);
                                            break inner;
                                        }
                                    }
                                }

                            }


                        }
                    }
                }

                logBlockPositions("COLLIS", blockPositions);
                break;


            case Y_AXIS:
                for (int i = 0; i <= 3; i++) {
                    wall = wallTo;
                    for (int j = wallTo; j != wallFrom+wallChange; j += wallChange) {
                        if (blockPositions[i][j] != null) {
                            blockPositions[i][j].setDestX(i);
                            blockPositions[i][j].setDestY(wall);
                            blockPositions[i][j].setWall(wall);
                            Log.i("IIJJ", String.format("i  i : %d %d\nj  j-: %d %d", i, i, j, j - wallChange));

                            wall += wallChange;

                        }
                    }
                }

                // merge algorithm Y
                for (int i = 0; i <= 3; i++) {
                    wall = wallTo + wallChange;
                    for (int j = wallTo; j != wallFrom + wallChange; j += wallChange) {
                        if (blockPositions[i][j] != null) {


                            if ((0 <= (j - wallChange)) && ((j - wallChange) <= 3)) {
                                Log.i("IIJJ - FOR", String.format("i  i : %d %d\nj  j-: %d %d", i, i, j, j - wallChange));


                                inner:
                                for (int k = 1; k < 4; k++) {

                                    if ((0 <= (j - k * wallChange)) && ((j - k * wallChange) <= 3)) {
                                        if (blockPositions[i][j-k*wallChange] != null) {

                                            mergeYhelp(i, j, k*wallChange);
                                            break inner;
                                        }
                                    }
                                }

                            }


                        }
                    }
                }

                logBlockPositions("COLLIS", blockPositions);
                break;
        }
    }

    private void mergeXhelp(int i, int j, int wallChange) {
        Log.i("GET-MERGED", String.format("j, i: %b\nj-(%d), i: %b",
                blockPositions[j][i].getMerged(),
                wallChange,
                blockPositions[j - wallChange][i].getMerged()));

        if (findMerge(j, j - wallChange, i, i)) {
            if (!blockPositions[j][i].getMerged() &&
                    !blockPositions[j - wallChange][i].getMerged()) {
//                wall -= wallChange;
                blockPositions[j][i].doubleValue();
                blockPositions[j][i].updateTV();    // move to move function

                blockPositions[j][i].setMerged(true);    // move to move function
                blockPositions[j - wallChange][i].setMerged(true);

                blockPositions[j - wallChange][i].kill();   // sets kill flag
                blockPositions[j - wallChange][i] = null;
            }
        }
    }

    private void mergeYhelp(int i, int j, int wallChange) {
        Log.i("GET-MERGED", String.format("i, j: %b\ni, j-(%d): %b",
                blockPositions[i][j].getMerged(),
                wallChange,
                blockPositions[i][j-wallChange].getMerged()));

        if (findMerge(i, i, j, j - wallChange)) {
            if (!blockPositions[i][j].getMerged() &&
                    !blockPositions[i][j - wallChange].getMerged()) {
//                wall -= wallChange;
                blockPositions[i][j].doubleValue();
                blockPositions[i][j].updateTV();    // move to move function

                blockPositions[i][j].setMerged(true);
                blockPositions[i][j - wallChange].setMerged(true);

                blockPositions[i][j - wallChange].kill();   // sets kill flag
                blockPositions[i][j - wallChange] = null;
            }

        }
    }

    private boolean findMerge(int i1, int i2, int j1, int j2) {     // A and B are arbitrary axes
        Log.i("FIND_MERGE", String.format("I1 I2: %d %d\nJ1 J2: %d %d", i1, i2, j1, j2));
        if (blockPositions[i1][j1].getValue() == blockPositions[i2][j2].getValue()) {
            return true;
        }
        else {
            return false;
        }
    }

    // main thread
    public void run(){
        thisActivity.runOnUiThread(
                new Runnable(){
                    public void run() {
                        if(counter%20 == 0)
                            Log.i("Timer", String.format("%d", counter/20));

                        switch (dir) {
                            case LEFT:
                                Log.i("DIRECTION", "--------------------LEFT----------------------");
                                collisDetect(0, 3, GameBlock.axis.X_AXIS);
                                logBlockPositions("COLLISION", blockPositions);
                                for (int i = 0; i < 4; i++) {
                                    for (int j = 0; j < 4; j++) {
                                        if (blockPositions[i][j] != null) {
                                            blockPositions[i][j].moved();
                                        }
                                    }
                                }

                                watchdog.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        thisActivity.runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateBlockPosArray();
                                                        if (!isGameBoardFull()) {
                                                            spawnBlock(1);
                                                        }
                                                        logBlockPositions("MERGE-UPDATE", blockPositions);
                                                        dir = directions.NO_MOVEMENT;
                                                        endGame = endGameCond.CHECKING;
//                                                        outputBlocks("VISUAL-DISPLAY", blockPositions);
                                                        watchdog.cancel();
                                                        watchdog = null;
                                                        watchdog = new Timer();
                                                    }
                                                }
                                        );

                                    }
                                }, wdDelay);
                                break;

                            case RIGHT:
                                Log.i("DIRECTION", "--------------------RIGHT---------------------");
                                collisDetect(3, 0, GameBlock.axis.X_AXIS);
                                logBlockPositions("COLLISION", blockPositions);
                                for (int i = 0; i < 4; i++) {
                                    for (int j = 0; j < 4; j++) {
                                        if (blockPositions[i][j] != null) {
                                            blockPositions[i][j].moved();
                                        }
                                    }
                                }

                                watchdog.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        thisActivity.runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateBlockPosArray();
                                                        if (!isGameBoardFull()) {
                                                            spawnBlock(1);
                                                        }
                                                        logBlockPositions("MERGE-UPDATE", blockPositions);
                                                        dir = directions.NO_MOVEMENT;
                                                        endGame = endGameCond.CHECKING;
//                                                        outputBlocks("VISUAL-DISPLAY", blockPositions);
                                                        watchdog.cancel();
                                                        watchdog = null;
                                                        watchdog = new Timer();
                                                    }
                                                }
                                        );

                                    }
                                }, wdDelay);
                                break;

                            case UP:
                                Log.i("DIRECTION", "--------------------UP------------------------");
                                collisDetect(0, 3, GameBlock.axis.Y_AXIS);
                                logBlockPositions("COLLISION", blockPositions);
                                for (int i = 0; i < 4; i++) {
                                    for (int j = 0; j < 4; j++) {
                                        if (blockPositions[i][j] != null) {
                                            blockPositions[i][j].moved();
                                        }
                                    }
                                }

                                watchdog.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        thisActivity.runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateBlockPosArray();
                                                        if (!isGameBoardFull()) {
                                                            spawnBlock(1);
                                                        }
                                                        logBlockPositions("MERGE-UPDATE", blockPositions);
                                                        dir = directions.NO_MOVEMENT;
                                                        endGame = endGameCond.CHECKING;
//                                                        outputBlocks("VISUAL-DISPLAY", blockPositions);
                                                        watchdog.cancel();
                                                        watchdog = null;
                                                        watchdog = new Timer();
                                                    }
                                                }
                                        );

                                    }
                                }, wdDelay);
                                break;

                            case DOWN:
                                Log.i("DIRECTION", "--------------------DOWN----------------------");
                                collisDetect(3, 0, GameBlock.axis.Y_AXIS);
                                logBlockPositions("COLLISION", blockPositions);
                                for (int i = 0; i < 4; i++) {
                                    for (int j = 0; j < 4; j++) {
                                        if (blockPositions[i][j] != null) {
                                            blockPositions[i][j].moved();
                                        }
                                    }
                                }

                                watchdog.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        thisActivity.runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateBlockPosArray();
                                                        if (!isGameBoardFull()) {
                                                            spawnBlock(1);
                                                        }
                                                        logBlockPositions("MERGE-UPDATE", blockPositions);
                                                        dir = directions.NO_MOVEMENT;
                                                        endGame = endGameCond.CHECKING;
//                                                        outputBlocks("VISUAL-DISPLAY", blockPositions);
                                                        watchdog.cancel();
                                                        watchdog = null;
                                                        watchdog = new Timer();
                                                    }
                                                }
                                        );

                                    }
                                }, wdDelay);
                                break;

                            case NO_MOVEMENT:
                                Log.i("DIRECTION", "--------------------NO MOVEMENT---------------");

                                switch (endGame) {
                                    case WAITING:
                                        break;
                                    case CHECKING:
                                        if (gameOver()){
                                            gameOverFlag = true;
                                            Log.wtf("GAME OVER", "Game over");

                                        }
                                        endGame = endGameCond.WAITING;
                                        break;
                                    default:
                                        endGame = endGameCond.WAITING;
                                        break;

                                }

//                                watchdog = null;
//                                watchdog = new Timer();
                                break;

                        }

                    }
                }
        );
        counter++;

    }

    public boolean gameOver(){

        int[][] nums = new int[6][6];
        int count = 0;      // counts the number of merges

        for (int i=0; i<6; i++) {
            for (int j=0; j<6; j++) {
                if ((i == 0) || (i == 5) || (j == 0) || (j == 5)){ nums[i][j] = 0; }
                else {
                    if (blockPositions[i-1][j-1] != null){
                        nums[i][j] = blockPositions[i-1][j-1].getValue();
                        if (nums[i][j] == 256){
                            Log.i("GAME OVER", "You got to 256!");
                            gameOverFlag = true;
                            return true;
                        }
                    }

                }
            }
        }

        // debugging gameboard output
        Log.i("GAME OVER", "--------------------------------");

        Log.i("GAME OVER", String.format("  %d  %d  %d  %d  %d  %d  ",
            nums[0][0], nums[1][0], nums[2][0], nums[3][0], nums[4][0], nums[5][0]));
        Log.i("GAME OVER", String.format("  %d  %d  %d  %d  %d  %d  ",
            nums[0][1], nums[1][1], nums[2][1], nums[3][1], nums[4][1], nums[5][1]));
        Log.i("GAME OVER", String.format("  %d  %d  %d  %d  %d  %d  ",
            nums[0][2], nums[1][2], nums[2][2], nums[3][2], nums[4][2], nums[5][2]));
        Log.i("GAME OVER", String.format("  %d  %d  %d  %d  %d  %d  ",
            nums[0][3], nums[1][3], nums[2][3], nums[3][3], nums[4][3], nums[5][3]));
        Log.i("GAME OVER", String.format("  %d  %d  %d  %d  %d  %d  ",
            nums[0][4], nums[1][4], nums[2][4], nums[3][4], nums[4][4], nums[5][4]));
        Log.i("GAME OVER", String.format("  %d  %d  %d  %d  %d  %d  ",
            nums[0][5], nums[1][5], nums[2][5], nums[3][5], nums[4][5], nums[5][5]));

        Log.i("GAME OVER", "--------------------------------");

        if (isGameBoardFull()){

            Log.wtf("GAME OVER", "Game board full -----------------------");

            for (int i=1; i<5; i++) {
                for (int j = 1; j < 5; j++) {
                    if ((nums[i][j] != nums[i-1][j]) &&
                        (nums[i][j] != nums[i+1][j]) &&
                        (nums[i][j] != nums[i][j-1]) &&
                        (nums[i][j] != nums[i][j+1])
                        ) {
                        count++;
                    } else {
                        return false;
                    }
                }
            }
            if (count == 16) {
                return true;
            }
        }
        return false;
    }

    private void updateBlockPosArray(){
        GameBlock[][] tempPosArray = new GameBlock[4][4];   // temporary destination array
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++){
                if (blockPositions[i][j] != null) {
                    blockPositions[i][j].setMerged(false);
                    tempPosArray[blockPositions[i][j].myCoordX][blockPositions[i][j].myCoordY] = blockPositions[i][j];
                }
            }
        }
        blockPositions = tempPosArray;
        tempPosArray = null;
    }

    private boolean isGameBoardFull(){
        int count = 0;
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++){
                if (blockPositions[i][j] != null){
                    count++;
                }
            }
        }
        if (count == 16){
            return true;
        }
        return false;
    }

    private void resetBlkArr(){
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++){
                blockPositions[i][j] = null;
            }
        }
    }

    public void logBlockPositions(String label, GameBlock[][] arr){
        int numOfBlocks = 0;
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++){
                if (arr[i][j] != null){
                    Log.i(label, String.format("Block Value: %d", arr[i][j].getValue()));
                    Log.i(label, String.format("Block Coord X: %d, index: %d", arr[i][j].myCoordX, i));
                    Log.i(label, String.format("Block Coord Y: %d, index: %d", arr[i][j].myCoordY, j));
                    Log.i(label, String.format("WALL: %d", arr[i][j].getWall()));
                    Log.i(label, String.format("MERGE: %b", arr[i][j].getMerged()));
                    Log.i(label, String.format("Dest X: %d", arr[i][j].getDestX()));
                    Log.i(label, String.format("Dest Y: %d", arr[i][j].getDestY()));
                    numOfBlocks++;
                }
            }
        }
        Log.i(label, String.format("Number of blocks: %d", numOfBlocks));
    }

    public void outputBlocks(String label, GameBlock[][] arr){
        int numOfBlocks = 0;
        String s = "";
        for (int i=0; i<4; i++) {
            for (int j = 0; j < 4; j++) {
                if (arr[j][i] != null){

                    s += String.format("  %d ", blockPositions[j][i]);
                    if (j == 3) {
                        s += "\n";
                    }


                } else {
                    s += "  0 ";
                }
            }
        }

        Log.i(label, "------------------");
        Log.i(label, s);
    }

}
