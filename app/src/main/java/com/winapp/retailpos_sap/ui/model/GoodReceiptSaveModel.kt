package com.winapp.retailpos_sap.ui.model

import com.winapp.retailpos_sap.model.GoodReceiptSaveDetail

data class GoodReceiptSaveModel(
    val DocDate: String,
    val Remark: String,
    val GoodsReceiveRemark: String,
    val GoodReceiveDetails: List<GoodReceiptSaveDetail>
)