package demo.qualcomm.spi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import demo.qualcomm.blsptest.R;
import demo.qualcomm.spi.spi.spiApplications;
import demo.qualcomm.spi.spi.spiConfigs;

public class MainActivity extends Activity {

    private static final String FRAGMENT_DIALOG = "dialog";
    private int[] mGPIOList = null;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private void GetAllResources() {

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.textView_uart_dev);
        //tv.append(" :" + Configs.devicePath);

        TextView tv = (TextView) findViewById(R.id.textView_spi_speed);
        tv.append(" :" + Integer.toString(spiConfigs.speed));


        spiRx = (EditText) findViewById(R.id.textView_spi_rx);
        spiTx = (EditText) findViewById(R.id.textView_spi_tx);
        cbnCS = (CheckBox) findViewById(R.id.checkBox_nCS);
        cbCPOL = (CheckBox) findViewById(R.id.checkBox_CPOL);
        cbCPHA = (CheckBox) findViewById(R.id.checkBox_CPHA);


        spiTransferButton = (Button) findViewById(R.id.button_spi_transfer);
    }


    private void ConfigureCallbacks() {
        spiTransferButton.setOnClickListener(mSPITransferCallback);
    }

    public static class ErrorMessage extends DialogFragment {

        private static final String msg = "msg";

        public static ErrorMessage newInstance(String message) {
            ErrorMessage dialog = new ErrorMessage();
            Bundle args = new Bundle();
            args.putString(msg, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(msg))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }


    private spiApplications[] mSPIDev = {null, null};

    private void SPIDeviceOpen() {
        try {
            int i = 0;
            for (String spiDevicePath : spiConfigs.devicePath) {
                mSPIDev[i] = new spiApplications(new File(spiDevicePath), i);
                i++;
            }
        } catch (SecurityException e) {
            ErrorMessage.newInstance(getString(R.string.error_spi))
                    .show(getFragmentManager(), FRAGMENT_DIALOG);
        } catch (IOException e) {
            ErrorMessage.newInstance(getString(R.string.error_unknown))
                    .show(getFragmentManager(), FRAGMENT_DIALOG);
        } catch (InvalidParameterException e) {
            ErrorMessage.newInstance(getString(R.string.error_spi_config))
                    .show(getFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    private void SPIDeviceClose() {
        for (spiApplications spiDev : mSPIDev) {
            spiDev.close();
        }
    }

    private String SPIDeviceTransfer(int devId, String stringTx, boolean nCS, boolean CPOL, boolean CPHA) {
        if (stringTx.length() % 2 != 0)
            stringTx = stringTx.substring(0, stringTx.length() - 1);
        byte[] dataTx = null;
        byte[] dataRx = new byte[stringTx.length() / 2];
        dataTx = hexStringToByteArray(stringTx);
        mSPIDev[devId].configureMode(nCS, CPOL, CPHA);
        mSPIDev[devId].transferData(dataTx, dataRx);
        String stringRx = toHexString(dataRx);
        return stringRx;
    }

    private final CompoundButton.OnClickListener mSPITransferCallback = new View.OnClickListener() {
        public void onClick(View v) {
            String spiTxString = spiTx.getText().toString();
            String spiRxString = null;
            Spinner sp = (Spinner) findViewById(R.id.textView_spi_dev);
            int devId = sp.getSelectedItemPosition();
            spiRxString = SPIDeviceTransfer(
                    devId,
                    spiTxString,
                    cbnCS.isChecked(),
                    cbCPOL.isChecked(),
                    cbCPHA.isChecked());
            spiRx.setText(spiRxString);
        }
    };


    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        s.toUpperCase();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < ba.length; i++)
            str.append(String.format("%02x", ba[i]));
        return str.toString().toUpperCase();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private EditText spiTx;
    private EditText spiRx;
    private CheckBox cbAddrBytes;
    private CheckBox cbDataBytes;
    private CheckBox cbnCS;
    private CheckBox cbCPOL;
    private CheckBox cbCPHA;
    private CheckBox[] cbINT;
    private Button spiTransferButton;

}
    
