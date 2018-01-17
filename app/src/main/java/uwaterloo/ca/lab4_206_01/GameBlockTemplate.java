package uwaterloo.ca.lab4_206_01;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.widget.ImageView;

/**
 * Created by sarthak on 06/07/17.
 */

public abstract class GameBlockTemplate extends AppCompatImageView {
    public GameBlockTemplate(Context ctx){
        super(ctx);
    }

    public abstract void setDestination();
    public abstract boolean moved();

}
