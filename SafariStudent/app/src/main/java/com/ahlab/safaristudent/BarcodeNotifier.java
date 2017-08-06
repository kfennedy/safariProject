package com.ahlab.safaristudent;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Katherine on 06/08/2017.
 */

public interface BarcodeNotifier {

    public void notifyBarcode(Barcode barcode);

}
