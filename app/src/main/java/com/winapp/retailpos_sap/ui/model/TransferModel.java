package com.winapp.retailpos_sap.ui.model;

import java.util.ArrayList;

public class TransferModel {

    public String transferNo;
    public String date;
    public String user;
    public String fromLocation;
    public String toLocation;
    public String status;
    private boolean isShow=false;

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public ArrayList<TransferDetailModel.TransferDetails> transferDetailsList;

    public ArrayList<TransferDetailModel.TransferDetails> getTransferDetailsList() {
        return transferDetailsList;
    }

    public void setTransferDetailsList(ArrayList<TransferDetailModel.TransferDetails> transferDetailsList) {
        this.transferDetailsList = transferDetailsList;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public String getTransferNo() {
        return transferNo;
    }

    public void setTransferNo(String transferNo) {
        this.transferNo = transferNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
