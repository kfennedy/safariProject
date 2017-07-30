package com.ahlab.safariteacher;

/**
 * Created by Katherine on 30/7/17.
 */

public class Mapping {

    String qrName;
    String qrContent;

    public Mapping(String qrName, String qrContent) {
        this.qrName = qrName;
        this.qrContent = qrContent;
    }

    public String getQrName() {
        return qrName;
    }

    public String getQrContent() {
        return qrContent;
    }
}
