package helies.elsa.thewalkingsuricate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Set;

public class GameLoop extends Thread {

    public boolean running; /** Variable pour arrêter le jeu */
    private long sleepTime = 100; /** FPS */
    public Dessins screen; /** Ecran de jeu */
    private Context context; /** Context */
    public MotionEvent lastEvent;
    public boolean animate; /** Pour stopper l'animation */


    public void initGame(Context context){
        this.context = context;
        animate = true;
        running = true;
        this.screen = new Dessins(context, this);
    }

    /** Boucle du jeu */
    @Override
    public void run() {
        while(this.running){
            this.processEvents();
            this.render();
            try{
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored){

            }
        }
    }

    /** Dessiner les composants du jeu */
    public void render(){
        this.screen.invalidate();
    }

    /** Quand on appuie sur l'écran l'animation s'arrète/redémarre */
    public void processEvents(){
        if(lastEvent != null && lastEvent.getAction() == MotionEvent.ACTION_DOWN){
            this.animate = !this.animate;
        }
        lastEvent = null;
    }
}
