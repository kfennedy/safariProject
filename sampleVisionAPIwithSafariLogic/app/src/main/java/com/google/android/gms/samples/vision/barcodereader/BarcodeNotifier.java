package com.google.android.gms.samples.vision.barcodereader;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by adria on 24/07/2017.
 */

public interface BarcodeNotifier {

    public void notifyBarcode(Barcode barcode);

}
