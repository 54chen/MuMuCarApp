package com.chen.mumucarapp;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.math.MathUtils;
import chen.CarComm;
import chen.Commands;
import org.apache.commons.lang.math.NumberUtils;

public class MainActivity extends AppCompatActivity {
    public static boolean[] orientations = new boolean[4];
    public static int[] speed = new int[2];
    public static int workMode = 0;
    public static int max = 5;
    public static float fspeed = 0l;
    private static CarComm comm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOrientations(0l,0l,0);
        comm = new CarComm(orientations, speed);
        comm.connect();
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setCon("hello world");
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    private void setCon(String xxx) {
        TextView textView = (TextView)findViewById(R.id.textview);
        int b = CarComm.session.getInt(CarComm.KEY_POWER,0);
        String text;
        if (b < 2000){
            text = "电量较低 ";
        }else if(b<2400){
            text = "电量低 ";
        }else if(b<2700){
            text = "电量中 ";
        }else if(b>=2700){
            text = "电量充足 ";
        }else{
            text = "电量不详";
        }
        int cc = CarComm.session.getInt(CarComm.KEY_CONN,0);
        if (cc == 1){
            text += " 连接成功！";
        }else{
            text = "未连接！";
        }
        //使用setText()方法修改文本
        String sstr = String.format("%s", new Object[] { Integer.valueOf(speed[0]) });
        textView.setText(text + "模式:" + workMode +" 速度: " + sstr +" 限速:" + max +" 方向:" + Commands.getCommand(orientations) + "\ntext:" + xxx);
    }
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1);
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BUTTON_R1:
                        setMode(1);
                        setOrientations(-100l,-100l, 5);
                        break;
                    case KeyEvent.KEYCODE_BUTTON_L1:
                        setMode(1);
                        setOrientations(-100l,-100l, 3);
                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        setCon("B");
                        max = 1;
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        setCon("Y");
                        setMode(0);
                        break;
                    case KeyEvent.KEYCODE_BUTTON_X:
                        setCon("X");
                        max = 3;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_A:
                        setCon("A");
                        max = 4;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_SELECT:
                        setCon("select");
                        max = 5;
                        CarComm.session.saveInt(CarComm.KEY_DIR,0);//关闭直传
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_START:
                        setCon("start");
                        max = 10;
                        CarComm.session.saveInt(CarComm.KEY_DIR,1);//打开直传
                        handled = true;
                        break;

                    default:
                        if (isFireKey(keyCode)) {
                            // Update the ship object to fire lasers

                            handled = true;
                        }
                        break;
                }
            }
            if (handled) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private static void setMode(int mode){
        switch (mode){
            case 0: // 左右摇杆模式
                max = 3;
                break;
            case 1: // L1 R1 左右2速模式
                max = 5;
                break;
            case 2: // 刹车油门模式
                max = 6;
                break;
            default:
                CarComm.session.saveInt(CarComm.KEY_DIR, 0);
                max = 5;
        }
        workMode = mode;
    }
    private static boolean isFireKey(int keyCode) {
        // Here we treat Button_A and DPAD_CENTER as the primary action
        // keys for the game.
        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_BUTTON_A;
    }

    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);
        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);


        float z = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_Z, historyPos);
        float rz = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_RZ, historyPos);

        float triggerL = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_BRAKE, historyPos);
        float triggerR = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_GAS, historyPos);

        if ( triggerL != 0){
            fspeed = 0;
            setOrientations(-1000l, -1000l,  0);
        }
        if ( triggerR != 0){
            setMode(2);
            fspeed = fspeed + triggerR*0.05f;
            setOrientations(y, x, Math.round(fspeed));
        }

        setCon("X:" + x +" Y:"+y + " z:"+z+ " rz:"+rz + " trigger:"+triggerL+" R:"+triggerR);
        if(workMode != 2) setOrientations(y, x, (int)Math.abs(rz*9));
    }

    private static void setOrientations(float y, float x, int paramInt2) {
        if (paramInt2 > max) paramInt2 = max;

        if (y == -1000l) speed[0] = paramInt2; //刹车传来的必须改速度

        if (workMode == 0 || workMode == 2) speed[0] = paramInt2;
        if (workMode == 1 && x==-100l) speed[0] = paramInt2; // 2速模式只收2键速度

        if (y > 1 || y < -1) return;
        if (x > 1 || x < -1) return;

        if (workMode == 0 || workMode == 1 || workMode == 2){
            orientations[0] = false;
            orientations[1] = false;
            orientations[2] = false;
            orientations[3] = false;

            if (y < 0) orientations[0] = true;
            if (y > 0) orientations[1] = true;
            if (x < 0) orientations[2] = true;
            if (x > 0) orientations[3] = true;
        }
        int d = CarComm.session.getInt(CarComm.KEY_DIR,0);
        if (d == 1){
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        comm.sendWithPwd(Commands.getCommand(orientations), speed);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

    }
}