package helies.elsa.thewalkingsuricate;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;


// Gestion du choix de l'ordinateur pour le Bluetooth
public class MainActivity extends Activity{
    // Bluetooth
    private BluetoothAdapter btAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBTState();
        setContentView(R.layout.choixbluetooth);
        setChoices();
        setDesign();
    }

    private void setDesign() {
        TextView myTextView = (TextView) findViewById(R.id.welcome);
        Spanned spanned = Html.fromHtml("<p style=\"align:center;font-size:20px;\"><strong>Bienvenue sur le jeu The Walking Suricate. Vous n'êtes plus qu'à une étape de jouer.</strong> </p>" +
                "<p style=\"align:center\">Pour pouvoir jouer, vous devez vous connecter avec votre ordinateur en Bluetooth.</p>" +
                "<p style=\"align:center\">Si votre ordinateur n'apparait pas dans la liste ci-dessous, il vous faut donc apparailler votre téléphone avec votre ordinateur " +
                "(pour cela aller dans les paramètres Bluetooth de vos appareils).</p>" +
                "<p style=\"align:center\">Relancer ensuite l'application.</p>");
        myTextView.setText(spanned);
    }

    private void setChoices() {
        Button validate = (Button) findViewById(R.id.validate);
        final RadioGroup group = (RadioGroup) findViewById(R.id.choices);

        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
        final ArrayList<String> adresses = new ArrayList<String>();

        int i = 0;
        for(BluetoothDevice elem : devices) {
            RadioButton rd = new RadioButton(this);
            adresses.add(i,elem.getAddress());
            rd.setId(i++);
            rd.setText(elem.getName());
            group.addView(rd);
        }

        validate.setText("Valider le choix");
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("INFO", "Validate");
                if (group.getCheckedRadioButtonId() == -1) {
                    // no radio buttons are checked
                    // TODO
                } else {
                    // one of the radio buttons is checked
                    GameActivity.address = adresses.get(group.getCheckedRadioButtonId());
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // Comportement du bouton "A Propos"
                return true;
            case R.id.help:
                // Comportement du bouton "Aide"
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Vérifie que le bluetooth est présent sur l'appareil et qu'il estr activé
     */
    private void CheckBTState() {
        if(btAdapter!=null) {
            if (!btAdapter.isEnabled()) {
                btAdapter.enable();
            }
        }
        //else {
            // Bluetooth non supporté
            // TODO
        //}
    }

}
