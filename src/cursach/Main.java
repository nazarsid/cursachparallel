package cursach;

import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.IntStream;

public class Main
{
    static int w_X = 30;
    static int w_T = 1000;

    static double A = 10;
    static double B = 5;
    static double a = 2;

    static double h = (double) 1/w_X;
    static double Tau = (double) Math.pow(h,2)*(0.1);
    static double[][] W_x_t = new double[w_X][w_T];
    static double func(double x, double t){
        return  Math.pow(Math.pow(x-A,2)/(6*a*(B-t)),2);
    }

    static double nextIter(int i,int k){
        return Tau*a*(0.5*Math.pow(W_x_t[i][k],-0.5)*(Math.pow((W_x_t[i+1][k]-W_x_t[i-1][k])/(2*h),2))+
                Math.pow(W_x_t[i][k],0.5)*((W_x_t[i-1][k]-2*W_x_t[i][k]+W_x_t[i+1][k])/(Math.pow(h,2))))+W_x_t[i][k];
    }

    public static void main(String[] args) throws IOException {

        long startSerialTime = System.nanoTime();

        for (int i = 0; i < W_x_t.length; i++) {
            W_x_t[i][0] = func(i * h, 0);
        }

        for (int i = 0; i < W_x_t[0].length; i++) {
            W_x_t[0][i] = func(0, i * Tau);
            W_x_t[W_x_t.length - 1][i] = func(1, i * Tau);
        }

        for (int k = 0; k < W_x_t[0].length - 1; k++) {
            for (int i = 1; i < W_x_t.length - 1; i++) {
                W_x_t[i][k + 1] = nextIter(i, k);
            }
        }

        long endSerialTime = System.nanoTime();
        long timeSerialSpent = endSerialTime - startSerialTime;

        FileWriter file = new FileWriter("test.txt");

        double AbsoluteAccuracySerial = 0;
        double RelativeAccuracySerial = 0;

        file.write("ListPlot3D[{" + "{" + (0.0) + "," + (0.0) + "," + W_x_t[0][0] + "}");

        for (int i = 0; i < W_x_t.length; i++) {
            for (int j = 1; j < W_x_t[0].length; j++) {
                file.write(",{" + (i * h) + "," + (j * Tau) + "," + W_x_t[i][j] + "}");
                if (AbsoluteAccuracySerial < Math.abs(W_x_t[i][j] - func(i * h, j * Tau))) {
                    AbsoluteAccuracySerial = Math.abs(W_x_t[i][j] - func(i * h, j * Tau));
                    RelativeAccuracySerial = AbsoluteAccuracySerial / W_x_t[i][j] * 100;
                }
            }

        }

        file.write("}, Mesh -> All]");
        file.close();

        long startTime = System.nanoTime();

        IntStream.range(0,W_x_t.length).parallel().forEach(i-> W_x_t[i][0] = func(i*h,0));

        IntStream.range(0,W_x_t[0].length).parallel().forEach(i->{
            W_x_t[0][i] = func(0,i*Tau);
            W_x_t[W_x_t.length-1][i] = func(1,i*Tau);
        });

        for (int k = 0; k < W_x_t[0].length-1; k++) {
            int finalK = k;
            IntStream.range(1, W_x_t.length - 1).parallel().forEach((i) -> W_x_t[i][finalK + 1] = nextIter(i, finalK));
        }

        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;

        FileWriter file1 = new FileWriter("test2.txt");

        double AbsoluteAccuracyParallel = 0;
        double RelativeAccuracyParallel = 0;

        file1.write("ListPlot3D[{"+"{"+(0.0)+","+(0.0)+","+W_x_t[0][0]+"}");

        for(int i=0;i<W_x_t.length;i++){
            for(int j=1;j<W_x_t[0].length;j++){
                file1.write( ",{" + ( i*h) + "," + ( j*Tau) + "," + W_x_t[i][j] + "}");
                if(AbsoluteAccuracyParallel < Math.abs(W_x_t[i][j]-func(i*h,j*Tau))){
                    AbsoluteAccuracyParallel = Math.abs(W_x_t[i][j]-func(i*h,j*Tau));
                    RelativeAccuracyParallel = AbsoluteAccuracyParallel/W_x_t[i][j]*100;
                }
            }

        }

        file1.write("}, Mesh -> All]");
        file1.close();

        System.out.println("Час роботи програми(послідовно): " + timeSerialSpent + " наносекунд");
        System.out.println("Час роботи програми(паралельно): " + totalTime + " наносекунд");
        System.out.println("Абсолютна похибка послідовних обчислень: "+ AbsoluteAccuracySerial);
        System.out.println("Відносна похибка послідовних обчислень: "+ RelativeAccuracySerial);
        System.out.println("Абсолютна похибка паралельних обчислень: "+ AbsoluteAccuracyParallel);
        System.out.println("Відносна похибка паралельних обчислень: "+ RelativeAccuracyParallel);
    }
}