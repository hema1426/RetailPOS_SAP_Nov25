package com.winapp.retailpos_sap.ui.newtransfer

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
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
import com.winapp.retailpos_sap.ui.activity.BaseActivity
import com.winapp.retailpos_sap.ui.activity.StockRequestListActivity
import com.winapp.retailpos_sap.ui.activity.TransferListProductActivity
import com.winapp.retailpos_sap.ui.model.ItemGroupList
import com.winapp.retailpos_sap.ui.newtransfer.LocationModel.LocationDetails
import com.winapp.retailpos_sap.ui.newtransfer.TransferInModel.TransferInDetails
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

class TransferInAddActivity : BaseActivity() {
    var pDialog: SweetAlertDialog? = null
    private var transferInModels: ArrayList<TransferInModel>? = null
    private var transferInDetailsl: ArrayList<TransferInDetails>? = null
    private var locationDetailsl: ArrayList<LocationDetails>? = null
    var transferInAdapter: TransferInAdapter? = null
    var linerLayoutManager: LinearLayoutManager? = null
    var transferInView: RecyclerView? = null
    var pdtsizel: TextView? = null
    var fromlocationl: TextView? = null
    var tolocationl: TextView? = null
    var toolbar: Toolbar? = null

    //    public LinearLayout toolbarImglay;
    //    public ImageView saveImg;
    var count = 0
    private var itemGroup: ArrayList<ItemGroupList>? = null
    private var groupspinner: AppCompatSpinner? = null
    var tolocationlay: LinearLayout? = null
    var fromlocationlay: LinearLayout? = null
    var fromWarehouseCode: String? = ""
    var toWarehouseCode: String? = ""
    var fromWarehouseName = ""
    var search_ed: EditText? = null
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
    private var transferType: String? = null
    private val settingUOMval = "PCS"
    private var islocationPermission: String? = null
    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    var isscanpdt: Boolean? = false
    var isqtygreat: Boolean? = false
    private var scannedData = StringBuilder()

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_in)
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
        transferInView = findViewById(R.id.rv_transferInList)
        tolocationl = findViewById(R.id.tolocation_transfer)
        fromlocationl = findViewById(R.id.fromlocation_transfer)
        tolocationlay = findViewById(R.id.toloc_lay)
        fromlocationlay = findViewById(R.id.fromloc_lay)
        search_ed = findViewById(R.id.searchBar_transfer)
        emptytxt = findViewById(R.id.empty_txt)
        pdtsizel = findViewById(R.id.pdtsize)
        groupspinner = findViewById(R.id.spinner_status)
        //        toolbar= findViewById(R.id.toolbar_trans);

//        saveImg= findViewById(R.id.save_image);
//        toolbarImglay= findViewById(R.id.iv_customtoolbar_img);
//        toolbartxt= findViewById(R.id.tv_customtoolbar_title);
        default_uom_transfl!!.setText(settingUOMval)
        transferInDetailsl = ArrayList()
        val c = Calendar.getInstance().time
        println("Current time => $c")
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        currentDate = df1.format(c)
        if (intent != null) {
            transferType = intent.getStringExtra("transferType")
            //            toolbartxt.setText(transferType);
            setTitle(transferType)
            if (transferType == "Stock Request") {
                fromlocationlay!!.setEnabled(true)
                tolocationlay!!.setEnabled(false)
                tolocationl!!.setText(locationCode)
                toWarehouseCode = locationCode
            } else {
                if (transferType == "Transfer In") {
                    if (islocationPermission.equals("Y", ignoreCase = true)) {
                        fromlocationlay!!.setEnabled(true)
                        tolocationlay!!.setEnabled(true)
                    } else {
                        fromlocationlay!!.setEnabled(true)
                        tolocationlay!!.setEnabled(false)
                    }
                    tolocationl!!.setText(locationCode)
                    toWarehouseCode = locationCode
                } else {
                    if (islocationPermission.equals("Y", ignoreCase = true)) {
                        fromlocationlay!!.setEnabled(true)
                        tolocationlay!!.setEnabled(true)
                    } else {
                        fromlocationlay!!.setEnabled(false)
                        tolocationlay!!.setEnabled(true)
                    }
                    fromlocationl!!.setText(locationCode)
                    fromWarehouseCode = locationCode
                }
            }
        }

//        toolbarImglay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int count=0;
//                for(int i = 0; i<transferInDetailsl.size(); i++){
//                    if(!transferInDetailsl.get(i).getQty().isEmpty()){
//                        count+=Integer.parseInt(transferInDetailsl.get(i).getQty());
//                    }
//                }
//                if (count>0){
//                    showDeleteAlert();
//                }else {
//                    finish();
//                }
//            }
//        });

//        saveImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for(int i = 0; i<transferInDetailsl.size(); i++){
//                    if(!transferInDetailsl.get(i).getQty().isEmpty()){
//                        count+=Integer.parseInt(transferInDetailsl.get(i).getQty());
//                    }
//                }
//                Log.e("qqty",""+count);
//                if (fromWarehouseCode!=null && toWarehouseCode!=null && !toWarehouseCode.isEmpty() &&
//                        !fromWarehouseCode.isEmpty()){
//                    if (count > 0){
//                        try {
//                            showSaveAlert(transferType);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }else {
//                        Toast.makeText(getApplicationContext(),"Add product first...!",Toast.LENGTH_SHORT).show();
//                    }
//                }else {
//                    Toast.makeText(getApplicationContext(),"Select Locations...!",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        fromlocationlay!!.setOnClickListener(View.OnClickListener {
            getfromlocationDialog(
                locationDetailsl
            )
        })
        tolocationlay!!.setOnClickListener(View.OnClickListener {
            if (!fromWarehouseCode!!.isEmpty() && fromWarehouseCode != null) {
                gettolocationDialog(locationDetailsl)
            } else {
                Toast.makeText(this@TransferInAddActivity, "Select from location", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        if (transferInDetailsl == null) {
            emptytxt!!.setVisibility(View.VISIBLE)
            pdtsizel!!.setVisibility(View.GONE)
            search_ed!!.setEnabled(false)
            transferInView!!.setVisibility(View.GONE)
        }
        try {
            // getGrouplist();
            locationlist
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        search_ed!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (!s.toString().isEmpty()) {
                    val searchtxt = s.toString()
                    // if (!searchtxt.isEmpty()) {
                    filter(searchtxt.toString())
                    //}
//                    else{
//                        setTransferInAdapter(transferInDetailsl);
//                    }
                    //  Log.w("transFiltSize",""+transferInDetailsl.size());
                } else {
                    Log.w("transFiltSizeaa", "" + transferInDetailsl!!.size)
                    setTransferInAdapter(transferInDetailsl!!)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var count = 0
            for (i in transferInDetailsl!!.indices) {
                if (!transferInDetailsl!![i].qty.isEmpty()) {
                    count += transferInDetailsl!![i].qty.toInt()
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
        val builder1 = AlertDialog.Builder(this@TransferInAddActivity)
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
    fun setTransferInAdapter(transferInList: ArrayList<TransferInDetails>) {
        try {
            transferInView!!.visibility = View.VISIBLE
            pdtsizel!!.visibility = View.VISIBLE
            search_ed!!.setEnabled(true)
            emptytxt!!.visibility = View.GONE
            pdtsizel!!.text = transferInList.size.toString() + " Products"
            transferInAdapter =
                TransferInAdapter(applicationContext, transferInList, transferType!!)
            linerLayoutManager =
                LinearLayoutManager(
                    applicationContext,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            transferInView!!.setLayoutManager(
                linerLayoutManager
            )
            transferInView!!.setItemAnimator(DefaultItemAnimator())
            transferInView!!.setAdapter(transferInAdapter)
            transferInAdapter!!.notifyDataSetChanged()
            //categoriesView.setVisibility(View.VISIBLE);
            //emptyLayout.setVisibility(View.GONE);
        } catch (ex: Exception) {
            Log.e("TAG", "Error in Populating the data:" + ex.message)
        }
    }

    fun showSaveAlert(mode: String?) {
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

            //invoicePrintCheck.setVisibility(View.GONE);
            if (mode == "Transfer In" || mode == "Transfer Out" || mode == "Covert Transfer") {
                saveTitle!!.setText("Save Transfer")
                saveMessage!!.setText("Are you sure want to save Transfer?")
                invoicePrintCheck!!.setText("Transfer Print")
            } else {
                saveTitle!!.setText("Save Stock Request")
                saveMessage!!.setText("Are you sure want to save Stock Request?")
                invoicePrintCheck!!.setText("Stock Request Print")
            }
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
                    if (mode == "Transfer In" || mode == "Transfer Out" || mode == "Covert Transfer" || mode == "Stock Request") {
                        createJsonObject()
                    }
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
        val mSig = CaptureSignatureView(this@TransferInAddActivity, null) {
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
        if (transferType == "Covert Transfer") {
            rootJson.put("invTransReqNo", "")
        } else {
            rootJson.put("invTransReqNo", "")
        }
        rootJson.put("invTransNo", "")
        rootJson.put("invTransStatus", "")
        rootJson.put("customerCode", "")
        rootJson.put("customerName", "")
        rootJson.put("fromWhsCode", fromWarehouseCode)
        rootJson.put("toWhsCode", toWarehouseCode)
        rootJson.put("docDate", currentDate)
        rootJson.put("docDueDate", currentDate)
        rootJson.put("transferType", "")
        rootJson.put("user", username)

        // Sales Details Add to the Objects
        var index = 1
        for (model in transferInDetailsl!!) {
            if (model.qty != null && !model.qty.isEmpty() && model.qty.toInt() > 0) {
                Log.w("transFerQtyaa", "" + model.qty)
                itemsObject = JSONObject()
                itemsObject.put("itemCode", model.productCode)
                itemsObject.put("itemName", model.productName)
                itemsObject.put("qty", model.qty.toString())
                itemsObject.put("fromWhsCode", fromWarehouseCode)
                itemsObject.put("toWhsCode", toWarehouseCode)
                if (transferType == "Transfer In" || transferType == "Transfer Out") {
                    if (model.inventoryUOM != "null" && model.inventoryUOM != null && model.inventoryUOM != ""
                    ) {
                        itemsObject.put("UomCode", model.inventoryUOM)
                    } else {
                        itemsObject.put("UomCode", settingUOMval)
                    }
                } else {
                    itemsObject.put("UomCode", settingUOMval)
                }
                itemsObject.put("docEntry", "")
                itemsObject.put("objectType", "")
                itemsObject.put("lineNum", "")
                itemsObject.put("batchNum", "")
                itemsObject.put("serialNum", "")
                itemsArray.put(itemsObject)
                index++
            }
        }
        rootJson.put("itItem", itemsArray)
        Log.w("GivenStockRequest:", rootJson.toString())
        saveTransferOrRequest(rootJson, 1, transferType)
    }

    fun saveTransferOrRequest(jsonBody: JSONObject, copy: Int, transferType: String?) {
        try {
            pDialog = SweetAlertDialog(this@TransferInAddActivity, SweetAlertDialog.PROGRESS_TYPE)
            pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
            pDialog!!.setCancelable(false)
            val requestQueue = Volley.newRequestQueue(this)
            Log.w("GivenInvoiceRequest:", jsonBody.toString())
            var URL = ""
            if (transferType == "Transfer In" || transferType == "Transfer Out" || transferType == "Covert Transfer") {
                URL = Constants.BASEURL + "PostingInventoryTransfer"
                Log.w("Given_TransferApi:", URL)
                pDialog!!.setTitleText("Saving Transfer...")
            } else {
                URL = Constants.BASEURL + "PostingInventoryTransferRequest"
                Log.w("Given_StockRequestApi:", URL)
                pDialog!!.setTitleText("Saving Stock Request...")
            }
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
                        if (transferType == "Transfer In" || transferType == "Transfer Out" || transferType == "Covert Transfer") {
                            assert(responseData != null)
                            val docNum = responseData.optString("docNum")
                            Toast.makeText(
                                applicationContext,
                                "Transfer Saved Success...!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent =
                                Intent(applicationContext, TransferListProductActivity::class.java)
                            if (isPrintEnable) {
                                intent.putExtra("docNum", docNum)
                                intent.putExtra("transferType", transferType)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            assert(responseData != null)
                            val docNum = responseData.optString("docNum")
                            Toast.makeText(
                                applicationContext,
                                "Stock Request Saved Success...!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent =
                                Intent(applicationContext, StockRequestListActivity::class.java)
                            if (isPrintEnable) {
                                intent.putExtra("docNum", docNum)
                                intent.putExtra("transferType", transferType)
                            }
                            startActivity(intent)
                            finish()
                        }
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
            val filterProducts = ArrayList<TransferInDetails>()
            //looping through existing elements
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            emptytxt!!.visibility = View.GONE
            for (s in transferInDetailsl!!) {
                //if the existing elements contains the search input
                if (s.productName.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault())) ||
                    s.productCode.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault()))
                ) {
                    //adding the element to filtered list
                    filterProducts.add(s)
                    emptytxt!!.visibility = View.GONE
                }
            }
            //calling a method of the adapter class and passing the filtered list
            transferInAdapter!!.updateList(filterProducts)
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

    //    void filter(String text){
    //        ArrayList<TransferInModel.TransferInDetails> temp = new ArrayList<>();
    //        for(TransferInModel.TransferInDetails d: transferInDetailsl){
    //
    //            String item = d.toString().toLowerCase();
    //            if(item.contains(text)){
    //                temp.add(d);
    //                Log.e("temp",""+temp);
    //            }
    //        }
    //        //update recyclerview
    //        transferInAdapter.updateList(temp);
    //    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun getTransferIn(warehouseCode: String?, itemGroupCode: String) {
        val url: String
        try {
            val jsonObj = JSONObject()
            jsonObj.put("WarehouseCode", warehouseCode)
            jsonObj.put("ItemGroupCode", itemGroupCode)
            val requestQueue = Volley.newRequestQueue(this)
            url = Constants.BASEURL + "ProductList"
            Log.w("pdtlist_urlTransAdd:", url + jsonObj)
            pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
            pDialog!!.setTitleText("Loading...")
            pDialog!!.setCancelable(false)
            pDialog!!.show()
            transferInModels = ArrayList()
            transferInDetailsl = ArrayList()
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                url,
                jsonObj,
                Response.Listener { response: JSONObject ->
                    try {
                        pDialog!!.dismiss()
                        Log.w("pdtlistTransAdd:", response.toString())

                        //pDialog.dismiss();
                        val statusCode = response.optString("statusCode")
                        val statusMessage = response.optString("statusMessage")
                        if (statusCode == "1") {
                            val transferInModel = TransferInModel()
                            val pdtArray = response.optJSONArray("responseData")
                            for (i in 0 until pdtArray.length()) {
                                val jsonObject = pdtArray.getJSONObject(i)
                                if (transferType == "Transfer In") {
                                    if (jsonObject.optInt("stockInHand") > 0) {
                                        val transferInDetails = TransferInDetails()
                                        transferInDetails.productName =
                                            jsonObject.optString("productName")
                                        transferInDetails.productCode =
                                            jsonObject.optString("productCode")
                                        transferInDetails.stockInHand =
                                            jsonObject.optInt("stockInHand")
                                        transferInDetails.qty = ""
                                        transferInDetails.inventoryUOM =
                                            jsonObject.optString("defaultInventoryUOM")
                                        transferInDetailsl!!.add(transferInDetails)
                                    }
                                } else {
                                    val transferInDetails = TransferInDetails()
                                    transferInDetails.productName =
                                        jsonObject.optString("productName")
                                    transferInDetails.productCode =
                                        jsonObject.optString("productCode")
                                    transferInDetails.stockInHand = jsonObject.optInt("stockInHand")
                                    transferInDetails.qty = ""
                                    transferInDetails.inventoryUOM =
                                        jsonObject.optString("defaultInventoryUOM")
                                    transferInDetailsl!!.add(transferInDetails)
                                }
                            }
                            Log.w("entrTransddd", "" + transferInDetailsl!!.size)
                            if (transferInDetailsl!!.size > 0) {
                                transferInModel.transferInDetails = transferInDetailsl
                                setTransferInAdapter(transferInDetailsl!!)
                                Log.w("entrTrans", "" + transferInDetailsl!!.size)
                            }
                        } else {
                            transferInAdapter!!.notifyDataSetChanged()
                            transferInDetailsl!!.clear()
                            transferInView!!.setAdapter(null)
                            pdtsizel!!.text = "0 Products"
                            Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT)
                                .show()
                            Log.w("entrTransff", "")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error: VolleyError ->
                    // Do something when error occurred
                    // pDialog.dismiss();
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
        } catch (e: Exception) {
        }
    }

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

    private fun getfromlocationDialog(locationDetailsArrayList: ArrayList<LocationDetails>?) {
        val builderSingle = AlertDialog.Builder(this)
        builderSingle.setTitle("Select From Warehouse")
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.selection_single_dialog)
        for (i in locationDetailsArrayList!!.indices) {
            arrayAdapter.add(locationDetailsArrayList[i].getLocationName())
        }
        val checkedItem = -1
        builderSingle.setSingleChoiceItems(
            arrayAdapter,
            checkedItem
        ) { dialog, which -> // user checked an item
            val strName = arrayAdapter.getItem(which)
            fromlocationl!!.text = strName
            for (i in locationDetailsArrayList.indices) {
                if (strName == locationDetailsArrayList[i].getLocationName()) {
                    Log.e("fromlocatcode", "" + locationDetailsArrayList[i].getLocationCode())
                    fromWarehouseCode = locationDetailsArrayList[i].getLocationCode()
                    fromWarehouseName = locationDetailsArrayList[i].getLocationName()
                }
            }
            if (fromWarehouseCode != toWarehouseCode) {
                getTransferIn(fromWarehouseCode, "All")
                dialog.dismiss()
            } else {
                Toast.makeText(
                    applicationContext,
                    "From warehouse and To warehouse should not be same...!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builderSingle.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        builderSingle.setCancelable(false)
        builderSingle.show()
    }

    private fun gettolocationDialog(locationDetailsArrayList: ArrayList<LocationDetails>?) {
        val builderSingle = AlertDialog.Builder(this)
        builderSingle.setTitle("Select To Warehouse")
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.selection_single_dialog)
        for (i in locationDetailsArrayList!!.indices) {
            arrayAdapter.add(locationDetailsArrayList[i].getLocationName())
        }
        val checkedItem = -1
        builderSingle.setSingleChoiceItems(arrayAdapter, checkedItem) { dialog, which ->
            // user checked an item
            val strName = arrayAdapter.getItem(which)
            Log.e("frmlocapi", "$strName...$fromWarehouseName")
            if (!strName!!.isEmpty() && !fromWarehouseCode!!.isEmpty() && fromWarehouseCode != null) {
                if (!fromWarehouseName.equals(strName, ignoreCase = true)) {
                    tolocationl!!.text = strName
                    for (i in locationDetailsArrayList.indices) {
                        val locatName = locationDetailsArrayList[i].getLocationName()
                        val locatCode = locationDetailsArrayList[i].getLocationCode()
                        if (strName == locationDetailsArrayList[i].getLocationName()) {
                            Log.e("locatcode", "" + locationDetailsArrayList[i].getLocationCode())
                            toWarehouseCode = locationDetailsArrayList[i].getLocationCode()
                        }
                    }
                    if (fromWarehouseCode != toWarehouseCode) {
                        getTransferIn(toWarehouseCode, "All")
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "From warehouse and To warehouse should not be same...!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    /*  try {
                                getGrouplist(fromWarehouseCode);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }*/

                    // getTransferIn(fromWarehouseCode,"All");
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        this@TransferInAddActivity,
                        "From location & to location should not be same",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        builderSingle.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        builderSingle.setCancelable(false)
        builderSingle.show()
    }

    @Throws(JSONException::class)
    private fun getGrouplist(fromLocation: String): ArrayList<ItemGroupList> {
        val requestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "ItemGroupList"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_group:", url)
        //        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//        pDialog.setTitleText("Loading Groups...");
//        pDialog.setCancelable(false);
//        pDialog.show();
        itemGroup = ArrayList()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("grouplist:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val groupArray = response.optJSONArray("responseData")
                        for (i in 0 until groupArray.length()) {
                            val jsonObject = groupArray.getJSONObject(i)
                            val groupName = jsonObject.getString("itemGroupName")
                            val groupCode = jsonObject.getString("itemGroupCode")
                            val itemGroupList = ItemGroupList(groupCode, groupName)
                            itemGroup!!.add(itemGroupList)
                        }
                        if (itemGroup!!.size > 0) {
                            setupGroup(itemGroup!!, fromLocation)
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
        return itemGroup!!
    }

    private fun setupGroup(itemGroupLists: ArrayList<ItemGroupList>, fromLocation: String) {
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemGroupLists)
        groupspinner!!.setAdapter(myAdapter)
        groupspinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                val itemCode = itemGroup!![i].groupCode
                val itemName = itemGroup!![i].groupName
                Log.e("selectspinn", "" + itemName)
                getTransferIn(fromLocation, itemCode)
                //                try {
//                    getLocationlist();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                return
            }
        }
    }

    fun scanBarTxt(barcode: String) {
        //update product list
        isscanpdt = false
        isqtygreat = false

        var isSearched = false
        var currentIndex = 0
        for ((index, prod) in this.transferInDetailsl!!.withIndex()) {
            Log.e("picklss", "" + prod.productCode + "  $barcode")

            if (prod.productCode == barcode) {

                isscanpdt = true
//                if (prod.quantity > prod.pickedQuantity) {
//                    if (prod.stockInHand > prod.pickedQuantity) {
                isqtygreat = true

//                        prod.pickedQuantity = prod.pickedQuantity + 1
//                        prod.balance = 1 - prod.balance

//                        transferInAdapter.let {
//                            it!!.notifyDataSetChanged()
//                        }
                if (transferInAdapter != null) {
                    transferInAdapter!!.updateQty(prod, true)
                    isSearched = true
                    currentIndex = index
//                        break;
                }

                Log.e("pick_compar", "" + prod.productCode + "..." + barcode)
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
        Toast.makeText(this@TransferInAddActivity, "No Product Found1", Toast.LENGTH_LONG)
            .show()
    } else {
        val barcodeTxt = result.contents
        //     barcodeText!!.setText(barcodeTxt)

        val mp = MediaPlayer.create(this, R.raw.beep) // sound is inside res/raw/mysound
        mp.start()
        scanBarTxt(barcodeTxt)

        Toast.makeText(
            this@TransferInAddActivity,
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

override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.transfer_add1_menu, menu)
    return true
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) { //finish();
        var count = 0
        for (i in transferInDetailsl!!.indices) {
            if (!transferInDetailsl!![i].qty.isEmpty()) {
                count += transferInDetailsl!![i].qty.toInt()
            }
        }
        if (count > 0) {
            showDeleteAlert()
        } else {
            onBackPressed()
        }
    } else if (item.itemId == R.id.action_save1) {
        for (i in transferInDetailsl!!.indices) {
            if (transferInDetailsl!![i].qty.isNotEmpty()) {
                count += transferInDetailsl!![i].qty.toInt()
            }
        }
        Log.w("transiz_qty", "" + count)
        Log.w("transsiz", "" + transferInDetailsl!!.size)
        if (fromWarehouseCode != null && toWarehouseCode != null && !toWarehouseCode!!.isEmpty() &&
            !fromWarehouseCode!!.isEmpty()
        ) {
            if (count > 0) {
                try {
                    showSaveAlert(transferType)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(applicationContext, "Add product first...!", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(applicationContext, "Select Locations...!", Toast.LENGTH_SHORT)
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

fun setTitle(title: String?) {
    //Customize the ActionBar
    val abar = supportActionBar
    val viewActionBar = layoutInflater.inflate(R.layout.action_bar_title, null)
    val params = ActionBar.LayoutParams( //Center the textview in the ActionBar !
        ActionBar.LayoutParams.WRAP_CONTENT,
        ActionBar.LayoutParams.MATCH_PARENT,
        Gravity.CENTER
    )
    val textviewTitle = viewActionBar.findViewById<TextView>(R.id.actionbar_textview)
    textviewTitle.text = title
    Objects.requireNonNull(abar)!!.setCustomView(viewActionBar, params)
    abar!!.setDisplayShowCustomEnabled(true)
    abar.setDisplayShowTitleEnabled(false)
    abar.setDisplayHomeAsUpEnabled(true)
    abar.setHomeButtonEnabled(true)
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