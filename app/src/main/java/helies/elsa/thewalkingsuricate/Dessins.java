package helies.elsa.thewalkingsuricate;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Elsa on 16/11/2016.
 */

public class Dessins extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder surface;

    public Dessins(Context context) {
        super(context);
        init();
    }

    public void init(){
        surface = getHolder();
        surface.addCallback(this);
    }

    @Override
    protected void onDraw(Canvas pCanvas) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
