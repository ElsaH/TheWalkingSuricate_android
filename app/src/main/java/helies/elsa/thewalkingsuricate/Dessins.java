package helies.elsa.thewalkingsuricate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Created by Elsa on 16/11/2016.
 */

public class Dessins extends SurfaceView implements SurfaceHolder.Callback {
    public Canvas canvas; /** Outil pour dessiner l'écran */
    private SurfaceHolder holder;
    private GameLoop game; /** Pointeur vers la boucle de jeu */

    private boolean etat1;
    private boolean etat2;
    private boolean etat3;
    private boolean etat4;

    /** Constructeur */
    public Dessins(Context context, GameLoop game){
        super(context);
        this.holder = getHolder();
        this.holder.addCallback(this);
        this.game = game;
    }

    /** Rafraichir l'écran */
    @Override
    public void invalidate(){
        if(holder != null){
            canvas = holder.lockCanvas();
            if(canvas != null){
                this.onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /** Callback quand l'écran est touché */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.game.lastEvent = event;
        return true;
    }

    /** Callback lorsque la surface est chargée = démarre boucle de jeu */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        canvas = new Canvas();
        this.game.start();
    }

    private int singleMeasure(int spec, int screenDim){
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);

        if(mode == MeasureSpec.UNSPECIFIED) return screenDim/2;
        else return size;
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        // On récupère les dimensions de l'écran
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        // Sa largeur et sa hauteur
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int retourWidth = singleMeasure(widthMeasureSpec, screenWidth);
        int retourHeight = singleMeasure(heightMeasureSpec, screenHeight);
        setMeasuredDimension(retourWidth, retourHeight);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        canvas.drawRect(0,0,width,height,new Paint(Color.WHITE));
        Log.i("INFO", width + " " + height );
        canvas.drawCircle(0,0,50,new Paint(Color.BLACK));

        // 4 ronds et en fonction de l'état la couleur change
        canvas.drawCircle(width/4, height/6, 10, new Paint(Color.CYAN));
        canvas.drawCircle(3*width/4, height/6,10, new Paint(Color.CYAN));
        canvas.drawCircle(width/4, 2*height/6, 10, new Paint(Color.CYAN));
        canvas.drawCircle(3*width/4, 2*height/6, 10, new Paint(Color.CYAN));

    }

    public void surfaceCreated(SurfaceHolder holder){}
    public void surfaceDestroyed(SurfaceHolder holder){}
}