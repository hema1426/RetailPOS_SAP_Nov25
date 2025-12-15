package com.winapp.retailpos_sap.model

import com.google.gson.annotations.SerializedName
import com.winapp.retailpos_sap.ui.model.BatchDetailModule

data class GoodReceiptSaveDetail(
    @SerializedName("BatchDetails")
    val BatchDetails: List<BatchDetailModule>,
    val ItemCode: String,
    val Price: String,
    val UomCode: String,
    val WarehouseCode: String,
    val qty: String
)