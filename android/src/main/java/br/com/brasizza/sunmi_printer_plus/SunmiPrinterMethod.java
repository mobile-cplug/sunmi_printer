package br.com.brasizza.sunmi_printer_plus;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.sunmi.peripheral.printer.InnerLcdCallback;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.util.ArrayList;

/**
 * Sunmi printer bridge using official {@code printerlibrary}.
 *
 * <p>Replaces local {@code woyou.aidlservice.jiuiv5} AIDL sources that duplicated
 * classes from {@code com.sunmi:paylib} (pulled by Stone POS on sunmi hardware)
 * and broke DEX merge. {@code printerlibrary} uses {@code com.sunmi.peripheral.printer}
 * packages — no overlap with paylib. On stone+sunmi builds the app should exclude
 * {@code printerlibrary} (plugin unused there) and keep paylib.
 */
public class SunmiPrinterMethod {

    private static final String TAG = SunmiPrinterMethod.class.getSimpleName();

    private final ArrayList<Boolean> _printingText = new ArrayList<>();
    private final Context _context;
    private SunmiPrinterService _printerService;

    private final InnerPrinterCallback _innerPrinterCallback =
            new InnerPrinterCallback() {
                @Override
                protected void onConnected(SunmiPrinterService service) {
                    _printerService = service;
                    try {
                        final String serviceVersion = service.getServiceVersion();
                        Toast.makeText(
                                        _context,
                                        "Sunmi Printer Service Connected. Version :"
                                                + serviceVersion,
                                        Toast.LENGTH_LONG)
                                .show();
                    } catch (RemoteException e) {
                        Log.e(TAG, "getServiceVersion failed", e);
                    } catch (NullPointerException e) {
                        Toast.makeText(
                                        _context,
                                        "Sunmi Printer Service Not Found",
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                }

                @Override
                protected void onDisconnected() {
                    _printerService = null;
                    Toast.makeText(
                                    _context,
                                    "Sunmi Printer Service Disconnected",
                                    Toast.LENGTH_LONG)
                            .show();
                }
            };

    public SunmiPrinterMethod(Context context) {
        this._context = context;
    }

    public void bindPrinterService() {
        try {
            InnerPrinterManager.getInstance()
                    .bindService(_context, _innerPrinterCallback);
        } catch (InnerPrinterException e) {
            Log.e(TAG, "bindService failed", e);
        }
    }

    public void unbindPrinterService() {
        try {
            InnerPrinterManager.getInstance()
                    .unBindService(_context, _innerPrinterCallback);
        } catch (InnerPrinterException e) {
            Log.e(TAG, "unBindService failed", e);
        }
    }

    public void initPrinter() {
        try {
            _printerService.printerInit(this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public int updatePrinter() {
        try {
            return _printerService.updatePrinterState();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public void printText(String text) {
        this._printingText.add(this._printText(text));
    }

    private Boolean _printText(String text) {
        try {
            _printerService.printText(text, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean setAlignment(Integer alignment) {
        try {
            _printerService.setAlignment(alignment, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean setFontSize(int fontSize) {
        try {
            _printerService.setFontSize(fontSize, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean setFontBold(Boolean bold) {
        if (bold == null) {
            bold = false;
        }

        byte[] command = new byte[] {0x1B, 0x45, 0x1};

        if (!bold) {
            command = new byte[] {0x1B, 0x45, 0x0};
        }

        try {
            _printerService.sendRAWData(command, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean printColumn(
            String[] stringColumns,
            int[] columnWidth,
            int[] columnAlignment,
            Boolean arabic) {
        try {
            if (arabic) {
                _printerService.printColumnsString(
                        stringColumns,
                        columnWidth,
                        columnAlignment,
                        this._callback());
            } else {
                _printerService.printColumnsText(
                        stringColumns,
                        columnWidth,
                        columnAlignment,
                        this._callback());
            }
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean printImage(Bitmap bitmap) {
        try {
            _printerService.printBitmap(bitmap, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean cutPaper() {
        try {
            _printerService.cutPaper(this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public String getPrinterSerialNo() {
        try {
            return _printerService.getPrinterSerialNo();
        } catch (RemoteException e) {
            return "";
        } catch (NullPointerException e) {
            return "NOT FOUND";
        }
    }

    public String getPrinterVersion() {
        try {
            return _printerService.getPrinterVersion();
        } catch (RemoteException e) {
            return "";
        } catch (NullPointerException e) {
            return "NOT FOUND";
        }
    }

    public int getPrinterPaper() {
        try {
            return _printerService.getPrinterPaper();
        } catch (RemoteException | NullPointerException e) {
            return 1;
        }
    }

    public int getPrinterMode() {
        try {
            return _printerService.getPrinterMode();
        } catch (RemoteException | NullPointerException e) {
            return 3;
        }
    }

    public Boolean openDrawer() {
        try {
            _printerService.openDrawer(this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public Boolean drawerStatus() {
        try {
            return _printerService.getDrawerStatus();
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public int timesOpened() {
        try {
            return _printerService.getOpenDrawerTimes();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public void lineWrap(int lines) {
        try {
            _printerService.lineWrap(lines, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void sendRaw(byte[] bytes) {
        try {
            this._printerService.sendRAWData(bytes, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void enterPrinterBuffer(Boolean clear) {
        try {
            this._printerService.enterPrinterBuffer(clear);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void commitPrinterBuffer() {
        try {
            this._printerService.commitPrinterBuffer();
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void exitPrinterBuffer(Boolean clear) {
        try {
            this._printerService.exitPrinterBuffer(clear);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void setAlignment(int alignment) {
        try {
            _printerService.setAlignment(alignment, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void printQRCode(String data, int modulesize, int errorlevel) {
        try {
            _printerService.printQRCode(
                    data, modulesize, errorlevel, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void printBarCode(
            String data,
            int barcodeType,
            int textPosition,
            int width,
            int height) {
        try {
            _printerService.printBarCode(
                    data,
                    barcodeType,
                    height,
                    width,
                    textPosition,
                    this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    private InnerResultCallback _callback() {
        return new InnerResultCallback() {
            @Override
            public void onRunResult(boolean isSuccess) throws RemoteException {}

            @Override
            public void onReturnString(String result) throws RemoteException {}

            @Override
            public void onRaiseException(int code, String msg)
                    throws RemoteException {}

            @Override
            public void onPrintResult(int code, String msg)
                    throws RemoteException {}
        };
    }

    public void sendLCDCommand(int flag) {
        try {
            _printerService.sendLCDCommand(flag);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void sendLCDString(String string) {
        try {
            _printerService.sendLCDString(string, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void sendLCDBitmap(android.graphics.Bitmap bitmap) {
        try {
            _printerService.sendLCDBitmap(bitmap, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void sendLCDDoubleString(String topText, String bottomText) {
        try {
            _printerService.sendLCDDoubleString(
                    topText, bottomText, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void sendLCDFillString(String string, int size, boolean fill) {
        try {
            _printerService.sendLCDFillString(
                    string, size, fill, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void sendLCDMultiString(String[] text, int[] align) {
        try {
            _printerService.sendLCDMultiString(text, align, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    private InnerLcdCallback _lcdCallback() {
        return new InnerLcdCallback() {
            @Override
            public void onRunResult(boolean show) throws RemoteException {}
        };
    }

    public void labelLocate() {
        try {
            _printerService.labelLocate();
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    public void labelOutput() {
        try {
            _printerService.labelOutput();
        } catch (RemoteException | NullPointerException ignored) {
        }
    }
}
