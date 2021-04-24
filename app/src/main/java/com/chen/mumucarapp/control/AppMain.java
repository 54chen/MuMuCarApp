package com.chen.mumucarapp.control;

import java.util.Scanner;

public class AppMain {
    public static boolean[] orientations = new boolean[4];
    public static int[] speed = new int[2];
    public static Scanner sc = null;
    public static void main(String[] args) {
        (new Thread() {
            public void run() {
                    try {
                        sc = new Scanner(System.in);
                        while (true) {
                            System.out.println("请输入您的方向：");
                            int dir = sc.nextInt();
                            System.out.println("请输入您的速度：");
                            int sp = sc.nextInt();
                            setOrientations(dir, sp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (sc != null) {
                            sc.close();
                            sc = null;
                        }
                    }

            }
        }).start();
        setOrientations(6,0);

        CarComm comm = new CarComm(orientations, speed);
        comm.connect();

    }

    private static void setOrientations(int paramInt1, int paramInt2) {
        speed[0] = paramInt2;
        switch (paramInt1) {
            default:
                return;
            case 0://$9 right
                orientations[0] = false;
                orientations[1] = false;
                orientations[2] = false;
                orientations[3] = true;
                return;
            case 1://$6 BACKWARD_RIGHT
                orientations[0] = false;
                orientations[1] = true;
                orientations[2] = false;
                orientations[3] = true;
                return;
            case 2://$5 BACKWARD
                orientations[0] = false;
                orientations[1] = true;
                orientations[2] = false;
                orientations[3] = false;
                return;
            case 3://$7 BACKWARD_LEFT
                orientations[0] = false;
                orientations[1] = true;
                orientations[2] = true;
                orientations[3] = false;
                return;
            case 4:// $8 left
                orientations[0] = false;
                orientations[1] = false;
                orientations[2] = true;
                orientations[3] = false;
                return;
            case 5://$4 FORWARD_RIGHT
                orientations[0] = true;
                orientations[1] = false;
                orientations[2] = false;
                orientations[3] = true;
                return;
            case 6://$0 FORWARD
                orientations[0] = true;
                orientations[1] = false;
                orientations[2] = false;
                orientations[3] = false;
                return;
            case 7://$3 FORWARD_LEFT
                orientations[0] = true;
                orientations[1] = false;
                orientations[2] = true;
                orientations[3] = false;
                return;
            case -1:
                break;
        }
        orientations[0] = false;
        orientations[1] = false;
        orientations[2] = false;
        orientations[3] = false;
    }
}
