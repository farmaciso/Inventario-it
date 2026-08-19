package it.farmaciso.shaketorch;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.hardware.camera2.CameraManager;
import android.os.*;
import androidx.annotation.Nullable;

public class ShakeService extends Service implements SensorEventListener {
    private SensorManager sm;
    private Sensor accel;
    private CameraManager camera;
    private String cameraId;
    private boolean torchOn=false;
    private long lastShake=0;
    private PowerManager.WakeLock wakeLock;

    @Override public void onCreate(){
        super.onCreate();
        createChannel();
        Notification n=new Notification.Builder(this,"shake_torch")
            .setContentTitle("Shake Torch attivo")
            .setContentText("Scuoti il telefono per accendere/spegnere la torcia")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true).build();
        startForeground(1001,n);
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accel=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accel!=null) sm.registerListener(this,accel,SensorManager.SENSOR_DELAY_GAME);
        camera=(CameraManager)getSystemService(CAMERA_SERVICE);
        try{ for(String id:camera.getCameraIdList()){ android.hardware.camera2.CameraCharacteristics c=camera.getCameraCharacteristics(id); Boolean flash=c.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE); Integer facing=c.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING); if(Boolean.TRUE.equals(flash)&&facing!=null&&facing==android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK){cameraId=id;break;}} }catch(Exception e){}
        PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
        wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"ShakeTorch:SensorLock");
        wakeLock.acquire();
    }

    private void createChannel(){ if(Build.VERSION.SDK_INT>=26){ NotificationChannel ch=new NotificationChannel("shake_torch","Shake Torch",NotificationManager.IMPORTANCE_LOW); getSystemService(NotificationManager.class).createNotificationChannel(ch);} }

    @Override public int onStartCommand(Intent intent,int flags,int startId){ return START_STICKY; }
    @Override public void onDestroy(){ if(sm!=null)sm.unregisterListener(this); if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release(); try{if(torchOn&&cameraId!=null)camera.setTorchMode(cameraId,false);}catch(Exception e){} super.onDestroy(); }
    @Nullable @Override public IBinder onBind(Intent i){return null;}
    @Override public void onAccuracyChanged(Sensor s,int a){}

    @Override public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType()!=Sensor.TYPE_ACCELEROMETER)return;
        float x=e.values[0],y=e.values[1],z=e.values[2];
        double force=Math.sqrt(x*x+y*y+z*z)-SensorManager.GRAVITY_EARTH;
        int level=getSharedPreferences("prefs",MODE_PRIVATE).getInt("sensitivity",1);
        double threshold= level==0 ? 15.5 : level==2 ? 10.5 : 13.0;
        long now=System.currentTimeMillis();
        if(force>threshold && now-lastShake>1100){ lastShake=now; toggleTorch(); }
    }

    private void toggleTorch(){
        if(cameraId==null)return;
        try{ torchOn=!torchOn; camera.setTorchMode(cameraId,torchOn); if(Build.VERSION.SDK_INT>=26){ ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(55,VibrationEffect.DEFAULT_AMPLITUDE)); } }catch(Exception e){ torchOn=false; }
    }
}
