package com.winapp.retailpos_sap.ui.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.adapter.ConvertTransferAddAdapter
import com.winapp.retailpos_sap.ui.model.TransferDetailModel
import com.winapp.retailpos_sap.ui.model.TransferDetailModel.TransferDetails
import com.winapp.retailpos_sap.ui.newtransfer.LocationModel
import com.winapp.retailpos_sap.ui.newtransfer.LocationModel.LocationDetails
import com.winapp.retailpos_sap.ui.utils.CaptureSignatureView
import com.winapp.retailpos_sap.ui.utils.Constants
import com.winapp.retailpos_sap.ui.utils.ImageUtil
import com.winapp.retailpos_sap.ui.utils.SessionManager
import com.winapp.retailpos_sap.ui.utils.SharedPreferenceUtil
import com.winapp.retailpos_sap.ui.utils.Utils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects

class ConvertTransferAddActivity : BaseActivity() {
    var pDialog: SweetAlertDialog? = null
//    private var transferInModels: ArrayList<TransferInModel>? = null
//    private var transferInDetailsl: ArrayList<TransferInDetails>? = null
    private var locationDetailsl: ArrayList<LocationDetails>? = null
    var convertTransAdapter: ConvertTransferAddAdapter? = null
    var linerLayoutManager: LinearLayoutManager? = null
    var convertTransferView: RecyclerView? = null
    var pdtsizel: TextView? = null
    var count = 0
    var emptytxt: TextView? = null
    var companyCode: String? = null
    var username: String? = null
    var user: HashMap<String, String>? = null
    var session: SessionManager? = null
    var companyName: String? = null
    var locationCode: String? = null
    private val cancelSheet: ImageView? = null
    private var cancelButton: Button? = null
    private var okButton: Button? = null
    private var alert: AlertDialog? = null
    var invoicePrintCheck: CheckBox? = null
    var saveTitle: TextView? = null
    var signatureCapture: ImageView? = null
    var attachement_layoutInvl: LinearLayout? = null
    private var signatureAlert: AlertDialog? = null
    private var saveMessage: TextView? = null
    private var default_uom_transfl: TextView? = null
    private var request_no_convertl: TextView? = null
    private var stockReqNo: String? = null
    private val settingUOMval = "PCS"
    private var islocationPermission: String? = null
    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    var isscanpdt: Boolean? = false
    var isqtygreat: Boolean? = false
    private var scannedData = StringBuilder()
    private var transferDetailModels: ArrayList<TransferDetailModel>? = null
    private var transferDetailsList:ArrayList<TransferDetailModel.TransferDetails>? = null
    private var reqFromLoc: String? = ""
    private var reqToLoc: String? = ""

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert_transfer_add)
        Log.w("activity_cg", javaClass.getSimpleName().toString())
        session = SessionManager(this)
        user = session!!.getUserDetails()
        progressDialog = ProgressDialog(this)
        sharedPreferenceUtil = SharedPreferenceUtil(this)
        //  settingUOMval = sharedPreferenceUtil.getStringPreference(sharedPreferenceUtil.KEY_SETTING_TRANS_UOM, "");
        Log.w("transferUOM..", "" + settingUOMval)
        companyCode = user!!.get(SessionManager.KEY_COMPANY_CODE)
        companyName = user!!.get(SessionManager.KEY_COMPANY_NAME)
        username = user!!.get(SessionManager.KEY_USER_NAME)
        locationCode = user!!.get(SessionManager.KEY_LOCATION_CODE)
        islocationPermission = user!!.get(SessionManager.IS_LOCATION_PERMISSION)
        default_uom_transfl = findViewById(R.id.default_uom_transf)
        convertTransferView = findViewById(R.id.rv_convert_transfList)
        request_no_convertl = findViewById(R.id.request_no_convert)

        emptytxt = findViewById(R.id.empty_txt)
        pdtsizel = findViewById(R.id.pdtsize_conv)

        default_uom_transfl!!.setText(settingUOMval)

        val c = Calendar.getInstance().time
        println("Current time => $c")
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        currentDate = df1.format(c)
        if (intent != null) {
            stockReqNo = intent.getStringExtra("convertTranferNo")
            request_no_convertl!!.setText(stockReqNo);
            setTitle("Convert To Transfer")
        }
        getStockRequestDetails(stockReqNo!!)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var count = 0
            for (i in transferDetailsList!!.indices) {
                if (!transferDetailsList!![i].qty.isEmpty()) {
                    count += transferDetailsList!![i].qty.toDouble().toInt()
                }
            }
            if (count > 0) {
                showDeleteAlert()
            } else {
                finish()
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun showDeleteAlert() {
        val builder1 = AlertDialog.Builder(this@ConvertTransferAddActivity)
        builder1.setMessage("Data Will be Cleared are you sure want to back?")
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "Yes"
        ) { dialog, id ->
            finish()
            dialog.cancel()
        }
        builder1.setNegativeButton(
            "No"
        ) { dialog, id -> dialog.cancel() }
        val alert11 = builder1.create()
        alert11.show()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setRequestAdapter(transferInList: ArrayList<TransferDetailModel.TransferDetails>) {
        //  try {
        convertTransferView!!.visibility = View.VISIBLE
        pdtsizel!!.visibility = View.VISIBLE
        emptytxt!!.visibility = View.GONE
        pdtsizel!!.text = transferInList.size.toString() + " Products"
        convertTransAdapter =
            ConvertTransferAddAdapter(applicationContext, transferInList)
        linerLayoutManager =
            LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
        convertTransferView!!.setLayoutManager(
            linerLayoutManager
        )
        convertTransferView!!.setItemAnimator(DefaultItemAnimator())
        convertTransferView!!.setAdapter(convertTransAdapter)
        convertTransAdapter!!.notifyDataSetChanged()
        //categoriesView.setVisibility(View.VISIBLE);
        //emptyLayout.setVisibility(View.GONE);
//        } catch (ex: Exception) {
//            Log.e("TAG", "Error in Populating the data:" + ex.message)
//        }
    }
    @SuppressLint("NotifyDataSetChanged")
//    fun setTransferInAdapter(transferInList: ArrayList<TransferInDetails>) {
//      //  try {
//            convertTransferView!!.visibility = View.VISIBLE
//            pdtsizel!!.visibility = View.VISIBLE
//            emptytxt!!.visibility = View.GONE
//            pdtsizel!!.text = transferInList.size.toString() + " Products"
//            convertTransAdapter =
//                ConvertTransferAddAdapter(applicationContext, transferInList, transferType!!)
//            linerLayoutManager =
//                LinearLayoutManager(
//                    applicationContext,
//                    LinearLayoutManager.VERTICAL,
//                    false
//                )
//            convertTransferView!!.setLayoutManager(
//                linerLayoutManager
//            )
//            convertTransferView!!.setItemAnimator(DefaultItemAnimator())
//            convertTransferView!!.setAdapter(convertTransAdapter)
//            convertTransAdapter!!.notifyDataSetChanged()
//            //categoriesView.setVisibility(View.VISIBLE);
//            //emptyLayout.setVisibility(View.GONE);
////        } catch (ex: Exception) {
////            Log.e("TAG", "Error in Populating the data:" + ex.message)
////        }
//    }

    fun showSaveAlert() {
        try {
            // create an alert builder
            val builder = AlertDialog.Builder(this)
            // set the custom layout
            builder.setCancelable(false)
            val customLayout = layoutInflater.inflate(R.layout.invoice_save_option, null)
            builder.setView(customLayout)
            // add a button
            okButton = customLayout.findViewById(R.id.btn_ok)
            cancelButton = customLayout.findViewById(R.id.btn_cancel)
            invoicePrintCheck = customLayout.findViewById(R.id.invoice_print_check)
            saveMessage = customLayout.findViewById(R.id.save_message)
            saveTitle = customLayout.findViewById(R.id.save_title)
            signatureCapture = customLayout.findViewById(R.id.signature_capture)
            attachement_layoutInvl = customLayout.findViewById(R.id.attachement_layoutInv)
            attachement_layoutInvl!!.setVisibility(View.GONE)
            val noOfCopy = customLayout.findViewById<TextView>(R.id.no_of_copy)
            val copyPlus = customLayout.findViewById<Button>(R.id.increase)
            val copyMinus = customLayout.findViewById<Button>(R.id.decrease)
            val signatureButton = customLayout.findViewById<Button>(R.id.btn_signature)
            val copyLayout = customLayout.findViewById<LinearLayout>(R.id.print_layout)

                saveTitle!!.setText("Save Transfer")
                saveMessage!!.setText("Are you sure want to save Transfer?")
                invoicePrintCheck!!.setText("Transfer Print")

            invoicePrintCheck!!.setOnClickListener(View.OnClickListener {
                if (invoicePrintCheck!!.isChecked()) {
                    isPrintEnable = true
                } else {
                    isPrintEnable = false
                }
            })
            okButton!!.setOnClickListener(View.OnClickListener { view1: View? ->
                try {
                    alert!!.dismiss()
                        createJsonObject()
                } catch (exception: Exception) {
                }
            })
            copyPlus.setOnClickListener {
                val copyvalue = noOfCopy.getText().toString()
                var copy = copyvalue.toInt()
                copy++
                noOfCopy.text = copy.toString() + ""
            }
            copyMinus.setOnClickListener {
                if (noOfCopy.getText().toString() != "1") {
                    val copyvalue = noOfCopy.getText().toString()
                    var copy = copyvalue.toInt()
                    copy--
                    noOfCopy.text = copy.toString() + ""
                }
            }
            signatureButton.setOnClickListener { showSignatureAlert() }
            cancelButton!!.setOnClickListener(View.OnClickListener { alert!!.dismiss() })
            // create and show the alert dialog
            alert = builder.create()
            alert!!.show()
        } catch (exception: Exception) {
        }
    }

    fun showSignatureAlert() {
        val alertDialog = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.signature_layout, null)
        alertDialog.setView(customLayout)
        val acceptButton = customLayout.findViewById<Button>(R.id.buttonYes)
        val cancelButton = customLayout.findViewById<Button>(R.id.buttonNo)
        val clearButton = customLayout.findViewById<Button>(R.id.buttonClear)
        val mContent = customLayout.findViewById<LinearLayout>(R.id.signature_layout)
        acceptButton.setEnabled(false)
        acceptButton.setAlpha(0.4f)
        val mSig = CaptureSignatureView(this@ConvertTransferAddActivity, null) {
            acceptButton.setEnabled(true)
            acceptButton.setAlpha(1f)
        }
        mContent.addView(
            mSig,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        acceptButton.setOnClickListener { // byte[] signature = captureSignatureView.getBytes();
            val signature = mSig.getBitmap()
            signatureCapture!!.setImageBitmap(signature)
            signatureString = ImageUtil.convertBimaptoBase64(signature)
            Utils.setSignature(signatureString)
            signatureAlert!!.dismiss()
            Log.w("SignatureString:", signatureString)
        }
        cancelButton.setOnClickListener { signatureAlert!!.dismiss() }
        clearButton.setOnClickListener {
            signatureString = ""
            Utils.setSignature("")
            mSig.ClearCanvas()
        }
        signatureAlert = alertDialog.create()
        signatureAlert!!.setCanceledOnTouchOutside(false)
        signatureAlert!!.show()
    }

    @Throws(JSONException::class)
    private fun createJsonObject() {
        // transferInDetailsl = transferInAdapter.getTransferInlist();
        val rootJson = JSONObject()
        var itemsObject = JSONObject()
        val itemsArray = JSONArray()

        rootJson.put("ReqDocNum", "")
        rootJson.put("FromWhsCode", reqFromLoc)
        rootJson.put("ToWhsCode", reqToLoc)
        rootJson.put("DocDate", currentDate)
        rootJson.put("DocDueDate", currentDate)
        rootJson.put("User", username)
        rootJson.put("Remarks", "")
        // Sales Details Add to the Objects
        var index = 1
        for (model in transferDetailsList!!) {
            if (model.sentQty != null && !model.sentQty.isEmpty() && model.sentQty.toInt() > 0) {
                Log.w("converttrans", "" + model.sentQty)
                itemsObject = JSONObject()
                itemsObject.put("ItemCode", model.itemCode)
                itemsObject.put("ItemName", model.description)
                itemsObject.put("Qty", model.sentQty.toString())
                itemsObject.put("FromWhsCode", reqFromLoc)
                itemsObject.put("ToWhsCode", reqToLoc)
                itemsObject.put("UomCode", "PCS")
                itemsObject.put("BatchNum", "")
                itemsObject.put("SerialNum", "")
                itemsArray.put(itemsObject)
                index++
            }
        }
        rootJson.put("ItItem", itemsArray)
        Log.w("convertreq_Json:", rootJson.toString())
        saveTransferOrRequest(rootJson)
    }

    fun saveTransferOrRequest(jsonBody: JSONObject) {
        try {
            pDialog = SweetAlertDialog(this@ConvertTransferAddActivity, SweetAlertDialog.PROGRESS_TYPE)
            pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
            pDialog!!.setCancelable(false)
            val requestQueue = Volley.newRequestQueue(this)
            Log.w("GivenInvoiceRequest:", jsonBody.toString())
            var URL = ""
                URL = Constants.BASEURL + "PostingInventoryTransferDraft"
                Log.w("Given_StockRequestApi:", URL)
                pDialog!!.setTitleText("Saving Transfer Draft...")

            pDialog!!.show()
            val salesOrderRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                URL,
                jsonBody,
                Response.Listener { response: JSONObject ->
                    Log.w("Transfer_ResponseSap:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val message = response.optString("statusMessage")
                    var responseData: JSONObject? = null
                    responseData = response.optJSONObject("responseData")
                    if (statusCode == "1") {

                            assert(responseData != null)
                            val docNum = responseData.optString("docNum")
                            Toast.makeText(
                                applicationContext,
                                "Transfer Saved Success...!",
                                Toast.LENGTH_SHORT).show()
                            val intent =
                                Intent(applicationContext, TransferDraftListActivity::class.java)
//                            if (isPrintEnable) {
//                                intent.putExtra("docNum", docNum)
//                                intent.putExtra("transferType", transferType)
//                            }
                            startActivity(intent)
                            finish()
                    } else {
                        /* Intent intent=new Intent(getApplicationContext(),TransferListProductActivity.class);
                    if (isPrintEnable) {
                        intent.putExtra("docNum","22010004");
                        intent.putExtra("transferType",transferType);
                    }
                    startActivity(intent);
                    finish();*/
                        if (responseData != null) {
                            Toast.makeText(
                                applicationContext,
                                responseData.optString("error"),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Error in Saving Data...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.w("SalesOrder_Response:", error.toString())
                    pDialog!!.dismiss()
                }) {
                /* @Override
                 public byte[] getBody() {
                     return jsonBody.toString().getBytes();
                 }*/
                override fun getBodyContentType(): String {
                    return "application/json"
                }

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val creds = String.format(
                        "%s:%s",
                        Constants.API_SECRET_CODE,
                        Constants.API_SECRET_PASSWORD
                    )
                    val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                    params["Authorization"] = auth
                    return params
                }
            }
            salesOrderRequest.setRetryPolicy(object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            })
            requestQueue.add(salesOrderRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun filter(text: String) {
        try {
            //new array list that will hold the filtered data
            val filterProducts = ArrayList<TransferDetailModel.TransferDetails>()
            //looping through existing elements
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            emptytxt!!.visibility = View.GONE
            for (s in transferDetailsList!!) {
                //if the existing elements contains the search input
                if (s.description.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault())) ||
                    s.itemCode.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault()))
                ) {
                    //adding the element to filtered list
                    filterProducts.add(s)
                    emptytxt!!.visibility = View.GONE
                }
            }
            //calling a method of the adapter class and passing the filtered list
            convertTransAdapter!!.updateList(filterProducts)
            Log.e("filter", "" + filterProducts)
            if (filterProducts.isEmpty()) {
                emptytxt!!.visibility = View.VISIBLE
            }

            //setAdapter(filterProducts);
            pdtsizel!!.text = filterProducts.size.toString() + " Products"
        } catch (ex: Exception) {
            Log.e("Error_in_filter", Objects.requireNonNull(ex.message)!!)
        }
    }
    @Throws(JSONException::class)
    private fun getStockRequestDetails(transferNo: String) {
        // Initialize a new RequestQueue instance
        val jsonBody = JSONObject()
        jsonBody.put("InvTransReqNo", transferNo)
        val requestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "InventoryTransferRequestDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url + jsonBody)
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
        pDialog!!.setTitleText("Generating Print Preview...")
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        transferDetailModels = java.util.ArrayList<TransferDetailModel>()
        transferDetailsList = java.util.ArrayList<TransferDetails>()

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener<JSONObject> { response: JSONObject ->
                try {
                    Log.w("TransferDetail:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray = response.optJSONArray("responseData")!!
                        val detailObject = transferDetailsArray!!.optJSONObject(0)
                        val model = TransferDetailModel()
                        model.number = detailObject.optString("invTransReqNo")
                        model.status = detailObject.optString("invTransReqStatus")
                        model.date = detailObject.optString("docDate")
                        model.fromLocation = detailObject.optString("fromWhsCode")
                        model.toLocation = detailObject.optString("toWhsCode")
                        model.fromLocationName = detailObject.optString("fromWarehouseName")
                        model.toLocationName = detailObject.optString("toWarehouseName")

                        reqFromLoc = detailObject.optString("fromWhsCode")
                        reqToLoc = detailObject.optString("toWhsCode")

                        val itemsArray = detailObject.optJSONArray("itItem")
                        for (i in 0 until Objects.requireNonNull<JSONArray>(itemsArray)
                            .length()) {
                            val objectItem = itemsArray.optJSONObject(i)

                            val transferModel = TransferDetails()
                            transferModel.description = objectItem.optString("itemName")
                            transferModel.qty = objectItem.optString("qty")
                            transferModel.sentQty = ""
                            transferModel.itemCode = objectItem.optString("itemCode")
                            transferModel.stock = objectItem.optInt("stockInHand")
                            transferModel.uomCode = objectItem.optString("uomCode")
                            transferDetailsList!!.add(transferModel)
                        }
                        model.setTransferDetailsList(transferDetailsList)
                        transferDetailModels!!.add(model)
                        // printTransfer(transferNo,transferDetailModels,type);
                        if (transferDetailsList!!.size > 0) {
                            emptytxt!!.setVisibility(View.GONE)
                            pdtsizel!!.setVisibility(View.VISIBLE)
                            convertTransferView!!.setVisibility(View.VISIBLE)
                            setRequestAdapter(transferDetailsList!!)
                        }else{
                            emptytxt!!.setVisibility(View.VISIBLE)
                            pdtsizel!!.setVisibility(View.GONE)
                            convertTransferView!!.setVisibility(View.GONE)
                        }
                    } else {
                        emptytxt!!.setVisibility(View.VISIBLE)
                        pdtsizel!!.setVisibility(View.GONE)
                        convertTransferView!!.setVisibility(View.GONE)

                        Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                pDialog!!.dismiss()
                Log.w("Error_throwing:", error.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = java.util.HashMap<String, String>()
                val creds =
                    String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD)
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params["Authorization"] = auth
                return params
            }
        }
        jsonObjectRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

 //   @RequiresApi(api = Build.VERSION_CODES.M)
//    private fun getTransferIn(warehouseCode: String?, itemGroupCode: String) {
//        val url: String
//        try {
//            val jsonObj = JSONObject()
//            jsonObj.put("WarehouseCode", warehouseCode)
//            jsonObj.put("ItemGroupCode", itemGroupCode)
//            val requestQueue = Volley.newRequestQueue(this)
//            url = Constants.BASEURL + "ProductList"
//            Log.w("pdtlist_urlTransAdd:", url + jsonObj)
//            pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
//            pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
//            pDialog!!.setTitleText("Loading...")
//            pDialog!!.setCancelable(false)
//            pDialog!!.show()
//            transferInModels = ArrayList()
//            transferInDetailsl = ArrayList()
//            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
//                Method.POST,
//                url,
//                jsonObj,
//                Response.Listener { response: JSONObject ->
//                    try {
//                        pDialog!!.dismiss()
//                        Log.w("pdtlistTransAdd:", response.toString())
//
//                        //pDialog.dismiss();
//                        val statusCode = response.optString("statusCode")
//                        val statusMessage = response.optString("statusMessage")
//                        if (statusCode == "1") {
//                            val transferInModel = TransferInModel()
//                            val pdtArray = response.optJSONArray("responseData")
//                            for (i in 0 until pdtArray.length()) {
//                                val jsonObject = pdtArray.getJSONObject(i)
//                                if (transferType == "Transfer In") {
//                                    if (jsonObject.optInt("stockInHand") > 0) {
//                                        val transferInDetails = TransferInDetails()
//                                        transferInDetails.productName =
//                                            jsonObject.optString("productName")
//                                        transferInDetails.productCode =
//                                            jsonObject.optString("productCode")
//                                        transferInDetails.stockInHand =
//                                            jsonObject.optInt("stockInHand")
//                                        transferInDetails.qty = ""
//                                        transferInDetails.inventoryUOM =
//                                            jsonObject.optString("defaultInventoryUOM")
//                                        transferInDetailsl!!.add(transferInDetails)
//                                    }
//                                } else {
//                                    val transferInDetails = TransferInDetails()
//                                    transferInDetails.productName =
//                                        jsonObject.optString("productName")
//                                    transferInDetails.productCode =
//                                        jsonObject.optString("productCode")
//                                    transferInDetails.stockInHand = jsonObject.optInt("stockInHand")
//                                    transferInDetails.qty = ""
//                                    transferInDetails.inventoryUOM =
//                                        jsonObject.optString("defaultInventoryUOM")
//                                    transferInDetailsl!!.add(transferInDetails)
//                                }
//                            }
//                            Log.w("entrTransddd", "" + transferInDetailsl!!.size)
//                            if (transferInDetailsl!!.size > 0) {
//                                transferInModel.transferInDetails = transferInDetailsl
//                                setTransferInAdapter(transferInDetailsl!!)
//                                Log.w("entrTrans", "" + transferInDetailsl!!.size)
//                            }
//                        } else {
//                            convertTransAdapter!!.notifyDataSetChanged()
//                            transferInDetailsl!!.clear()
//                            convertTransferView!!.setAdapter(null)
//                            pdtsizel!!.text = "0 Products"
//                            Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT)
//                                .show()
//                            Log.w("entrTransff", "")
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }, Response.ErrorListener { error: VolleyError ->
//                    // Do something when error occurred
//                    // pDialog.dismiss();
//                    Log.w("Error_throwing:", error.toString())
//                }) {
//                override fun getHeaders(): Map<String, String> {
//                    val params = HashMap<String, String>()
//                    val creds = String.format(
//                        "%s:%s",
//                        Constants.API_SECRET_CODE,
//                        Constants.API_SECRET_PASSWORD
//                    )
//                    val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
//                    params["Authorization"] = auth
//                    return params
//                }
//            }
//            jsonObjectRequest.setRetryPolicy(object : RetryPolicy {
//                override fun getCurrentTimeout(): Int {
//                    return 50000
//                }
//
//                override fun getCurrentRetryCount(): Int {
//                    return 50000
//                }
//
//                @Throws(VolleyError::class)
//                override fun retry(error: VolleyError) {
//                }
//            })
//            // Add JsonArrayRequest to the RequestQueue
//            requestQueue.add(jsonObjectRequest)
//        } catch (e: Exception) {
//        }
//    }

    @get:Throws(JSONException::class)
    private val locationlist: Unit
        private get() {
            val requestQueue = Volley.newRequestQueue(this)
            val url = Constants.BASEURL + "WarehouseList"
            // Initialize a new JsonArrayRequest instance
            Log.w("Given_url_location:", url)
            pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
            pDialog!!.setTitleText("Loading Warehouses...")
            pDialog!!.setCancelable(false)
            pDialog!!.show()
            locationDetailsl = ArrayList()
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response: JSONObject ->
                    try {
                        Log.w("locationlist:", response.toString())
                        pDialog!!.dismiss()
                        val statusCode = response.optString("statusCode")
                        val statusMessage = response.optString("statusMessage")
                        if (statusCode == "1") {
                            val locationModel = LocationModel()
                            val locationArray = response.optJSONArray("responseData")
                            for (i in 0 until locationArray.length()) {
                                val jsonObject = locationArray.getJSONObject(i)
                                val locationDetails = LocationDetails()
                                locationDetails.setLocationName(jsonObject.optString("whsName"))
                                locationDetails.setLocationCode(jsonObject.optString("whsCode"))
                                locationDetailsl!!.add(locationDetails)
                            }
                            if (locationDetailsl!!.size > 0) {
                                locationModel.setLocationDetailsArrayList(locationDetailsl)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error: VolleyError ->
                    // Do something when error occurred
                    pDialog!!.dismiss()
                    Log.w("Error_throwing:", error.toString())
                }) {
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val creds = String.format(
                        "%s:%s",
                        Constants.API_SECRET_CODE,
                        Constants.API_SECRET_PASSWORD
                    )
                    val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                    params["Authorization"] = auth
                    return params
                }
            }
            jsonObjectRequest.setRetryPolicy(object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            })
            // Add JsonArrayRequest to the RequestQueue
            requestQueue.add(jsonObjectRequest)
        }

    fun scanBarTxt(barcode: String) {
        //update product list
        isscanpdt = false
        isqtygreat = false

        var isSearched = false
        var currentIndex = 0
        for ((index, prod) in this.transferDetailsList!!.withIndex()) {
            Log.e("picklss", "" + prod.itemCode + "  $barcode")

            if (prod.itemCode == barcode) {

                isscanpdt = true
//                if (prod.quantity > prod.pickedQuantity) {
//                    if (prod.stockInHand > prod.pickedQuantity) {
                isqtygreat = true

//                        prod.pickedQuantity = prod.pickedQuantity + 1
//                        prod.balance = 1 - prod.balance

//                        transferInAdapter.let {
//                            it!!.notifyDataSetChanged()
//                        }
                if (convertTransAdapter != null) {
                    convertTransAdapter!!.updateQty(prod, true)
                    isSearched = true
                    currentIndex = index
//                        break;
                }

                Log.e("pick_compar", "" + prod.itemCode + "..." + barcode)
                Toast.makeText(
                    this,
                    "pick qty added-> " + barcode,
                    Toast.LENGTH_SHORT
                )
                    .show()
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "No stock this product",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//
//                    }
//                } else {
//                    Toast.makeText(
//                        this,
//                        "Entered value greater than total order qty ",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
                if (isSearched) {
                    linerLayoutManager.let {
                        it?.scrollToPositionWithOffset(currentIndex, 0) // SCROLL TO TOP
                    }
//                }
//                    transferInView!!.scrollToPosition(currentIndex)
            }

        }
//                    if (prod.stockInHand <= prod.quantity) {
//                        Toast.makeText(
//                            this,
//                            "Avaiable Stock : "+prod.stockInHand,
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }

    }

    if (!isscanpdt!!)
    {
        Log.e("pickbarco", "..")
        Toast.makeText(this, "no product matched", Toast.LENGTH_SHORT).show()
    }
}

fun scanFromFragment() {
    fragmentLauncher.launch(ScanOptions())
}

private val fragmentLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
    ScanContract()
) { result ->
    if (result.contents == null) {
        Toast.makeText(this@ConvertTransferAddActivity, "No Product Found1", Toast.LENGTH_LONG)
            .show()
    } else {
        val barcodeTxt = result.contents
        //     barcodeText!!.setText(barcodeTxt)

        val mp = MediaPlayer.create(this, R.raw.beep) // sound is inside res/raw/mysound
        mp.start()
        scanBarTxt(barcodeTxt)

        Toast.makeText(
            this@ConvertTransferAddActivity,
            "Product" + "${result.contents}",
            Toast.LENGTH_LONG
        ).show()

        Log.e("scan_barcode.. ", "${result.contents}")

    }
}

override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
    if (event != null && event.action == KeyEvent.ACTION_DOWN) {
        val unicodeChar = event.unicodeChar
        if (unicodeChar != 0) {
            scannedData.append(unicodeChar.toChar())
        }

        // Check for ENTER / LINE FEED at end of scanning
        if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
            val result = scannedData.toString()
            scanBarTxt(result)
            scannedData.clear()

            // onScanCompleted(result) // Your handler
            return true
        }
    }
    return super.dispatchKeyEvent(event)
}
    fun setTitle(title: String?) {
        //Customize the ActionBar
        val abar = supportActionBar
        val viewActionBar = layoutInflater.inflate(R.layout.action_bar_title, null)
        val params = ActionBar.LayoutParams( //Center the textview in the ActionBar !
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.MATCH_PARENT,
            Gravity.LEFT
        )
        val textviewTitle = viewActionBar.findViewById<TextView>(R.id.actionbar_textview)
        textviewTitle.text = title
        Objects.requireNonNull(abar)!!.setCustomView(viewActionBar, params)
        abar!!.setDisplayShowCustomEnabled(true)
        abar.setDisplayShowTitleEnabled(false)
        abar.setDisplayHomeAsUpEnabled(true)
        abar.setHomeButtonEnabled(true)
    }
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.transfer_add1_menu, menu)
    return true
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) { //finish();
        var count = 0
        for (i in transferDetailsList!!.indices) {
            if (!transferDetailsList!![i].qty.isEmpty()) {
                count += transferDetailsList!![i].qty.toDouble().toInt()
            }
        }
        if (count > 0) {
            showDeleteAlert()
        } else {
            onBackPressed()
        }
    } else if (item.itemId == R.id.action_save1) {
        for (i in transferDetailsList!!.indices) {
            if (transferDetailsList!![i].qty.isNotEmpty()) {
                val qty = transferDetailsList!![i].qty.toDouble().toInt()
                count += qty
            }
        }
        Log.w("transiz_qty", "" + count)
        Log.w("transsiz", "" + transferDetailsList!!.size)
            if (count > 0) {
                try {
                    showSaveAlert()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(applicationContext, "Add product first...!", Toast.LENGTH_SHORT)
                    .show()
            }

    } else if (item.itemId == R.id.action_scan) {

        if (checkPermission()) {
            scanFromFragment()
        }
        //    scannedBarcode = ""
    }

    return true
}
override fun onBackPressed() {
    super.onBackPressed()
    finish()
}

override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
}

companion object {
    var currentDate: String? = null
    var progressDialog: ProgressDialog? = null
    var customerCode: String? = null
    var isPrintEnable = false
    var selectedBank: TextView? = null
    var amountText: EditText? = null
    var signatureString = ""
    var imageString: String? = null
}
}