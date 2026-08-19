package it.farmaciso.shaketorch;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private Switch enabled;
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs=getSharedPreferences("prefs",MODE_PRIVATE);
        if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.CAMERA},10);
        else if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},10);

        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(60,90,60,40); root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title=new TextView(this); title.setText("SHAKE TORCH"); title.setTextSize(30); title.setGravity(Gravity.CENTER); root.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView sub=new TextView(this); sub.setText("Scuoti il telefono per accendere o spegnere la torcia"); sub.setTextSize(16); sub.setGravity(Gravity.CENTER); sub.setPadding(0,25,0,45); root.addView(sub);
        enabled=new Switch(this); enabled.setText("Rilevamento shake attivo"); enabled.setTextSize(18); enabled.setChecked(prefs.getBoolean("enabled",false)); root.addView(enabled,new LinearLayout.LayoutParams(-1,-2));
        TextView sensLabel=new TextView(this); sensLabel.setText("Sensibilità"); sensLabel.setTextSize(18); sensLabel.setPadding(0,40,0,8); root.addView(sensLabel);
        Spinner spinner=new Spinner(this); String[] vals={"Bassa","Media","Alta"}; spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,vals)); spinner.setSelection(prefs.getInt("sensitivity",1)); root.addView(spinner,new LinearLayout.LayoutParams(-1,-2));
        status=new TextView(this); status.setText(enabled.isChecked()?"Servizio attivo anche a schermo spento":"Servizio disattivato"); status.setTextSize(15); status.setPadding(0,45,0,15); root.addView(status);
        TextView note=new TextView(this); note.setText("Per la massima affidabilità su Samsung, consenti all’app di funzionare senza restrizioni batteria."); note.setTextSize(14); root.addView(note);
        Button battery=new Button(this); battery.setText("Apri impostazioni batteria"); battery.setOnClickListener(v->{ try{ startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); }catch(Exception e){} }); root.addView(battery);
        setContentView(root);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int pos,long id){prefs.edit().putInt("sensitivity",pos).apply();}});
        enabled.setOnCheckedChangeListener((btt,on)->{prefs.edit().putBoolean("enabled",on).apply(); if(on){startForegroundService(new Intent(this,ShakeService.class));status.setText("Servizio attivo anche a schermo spento");}else{stopService(new Intent(this,ShakeService.class));status.setText("Servizio disattivato");}});
        if(enabled.isChecked()) startForegroundService(new Intent(this,ShakeService.class));
    }
}