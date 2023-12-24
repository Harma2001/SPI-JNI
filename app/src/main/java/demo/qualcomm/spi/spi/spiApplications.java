package demo.qualcomm.spi.spi;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class spiApplications {
    private static final String TAG = "spi";
    private FileDescriptor spiFd;
    private int mIdx;
    public spiApplications(File device, int idx) throws SecurityException, IOException {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            Log.e(TAG, "SPI Permission not granted");
            throw new SecurityException();
        }

        spiFd = open(device.getAbsolutePath(),spiConfigs.speed,0,spiConfigs.SPI_MODE_CS_L,idx);
        if (spiFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mIdx=idx;
    }

    public void closeDevice() {

       close();
    }

    public int transferData(byte[] arrTx, byte[] arrRx) {
        return transfer(mIdx,arrTx, arrRx, Math.min(arrTx.length,arrRx.length));
    }

    public int configureMode(boolean nCS, boolean CPOL, boolean CPHA) {
        return configuremode(mIdx,
                (nCS?0:spiConfigs.SPI_CS_HIGH ) | (CPOL?spiConfigs.SPI_CPOL:0)| (CPHA?spiConfigs.SPI_CPHA:0));
    }

    // JNI
    public native static void close();
    private native static FileDescriptor open(
            String path, int speed, int flags, int mode,int idx);
    private native static int transfer(
            int idx, byte[] arrTx, byte[] arrRx, int len);

    private native static int configuremode(int idx, int mode);

    static {
        System.loadLibrary("native-lib");
    }

}
