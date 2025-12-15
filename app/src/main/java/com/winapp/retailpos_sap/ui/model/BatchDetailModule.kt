package com.winapp.retailpos_sap.ui.model

import com.google.gson.annotations.SerializedName

data class BatchDetailModule(
    @SerializedName("BatchNo")
    var batchNo: String?,
    @SerializedName("BatchQty")
    var batchQty: String = "0",
    @SerializedName("ItemCode")
    var itemCode: String?,
    var updateTime: String = "",
    var avlQty: String? = "0",

    ){
    var isRemove : Boolean = false
}