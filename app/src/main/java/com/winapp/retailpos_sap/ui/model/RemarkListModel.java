package com.winapp.retailpos_sap.ui.model;

import androidx.annotation.NonNull;

public class RemarkListModel {

    private String remarkCode;
    private String remarkName;

    public String getRemarkCode() {
        return remarkCode;
    }

    public void setRemarkCode(String remarkCode) {
        this.remarkCode = remarkCode;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    @NonNull
    @Override
    public String toString() {
        return remarkName;
    }
}
