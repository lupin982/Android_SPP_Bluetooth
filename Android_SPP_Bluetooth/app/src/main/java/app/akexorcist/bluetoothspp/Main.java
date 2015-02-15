package app.akexorcist.bluetoothspp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;


public class Main extends Activity implements OnClickListener {

    BluetoothSPP bt;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bt = new BluetoothSPP(this);

        Button bStart = (Button) findViewById(R.id.bStart);
        bStart.setOnClickListener(this);

    }

    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        switch (id) {
            case R.id.bStart:
                SearchDevice sd = new SearchDevice();
                intent = new Intent(getApplicationContext(), sd.getClass());
                startActivity(intent);
                break;
        }
    }
}
