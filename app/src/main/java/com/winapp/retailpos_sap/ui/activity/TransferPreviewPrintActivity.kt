package com.winapp.retailpos_sap.ui.activity

import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.adapter.TransferPreviewPrintAdapter
import com.winapp.retailpos_sap.ui.model.TransferDetailModel
import com.winapp.retailpos_sap.ui.utils.Constants
import com.winapp.retailpos_sap.ui.utils.SessionManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Objects

class TransferPreviewPrintActivity : AppCompatActivity() {
    private var companyNametext: TextView? = null
    private var companyAddress1Text: TextView? = null
    private var companyAddress2Text: TextView? = null
    private var companyAddress3Text: TextView? = null
    private var companyPhoneText: TextView? = null
    private var companyGstText: TextView? = null
    private var company_name: String? = null
    private var company_address1: String? = null
    private var company_address2: String? = null
    private var company_address3: String? = null
    var sharedPreferences: SharedPreferences? = null
    var printerMacId: String? = null
    var printerType: String? = null
    private var transferDetailModels: ArrayList<TransferDetailModel>? = null
    private var transferDetailsList: ArrayList<TransferDetailModel.TransferDetails>? = null
    private var transfertype: TextView? = null
    private var transfertitle: TextView? = null
    private var transferno: TextView? = null
    private var from_locat: TextView? = null
    private var to_locat: TextView? = null
    private var transferdate: TextView? = null
    private var toloc_namel: TextView? = null
    private var fromloc_namel: TextView? = null
    private var transferListView: RecyclerView? = null
    private var adapter: TransferPreviewPrintAdapter? = null
    var session: SessionManager? = null
    var user: HashMap<String, String>? = null
    var companyId: String? = null
    var company_phone: String? = null
    var company_gst: String? = null
    var pDialog: SweetAlertDialog? = null
    var alert11: AlertDialog? = null
    var transferNo: String? = null
    var type: String? = null
    private var mainLayout: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_preview_print)
        session = SessionManager(this)
        user = session!!.userDetails
        companyId = user!![SessionManager.KEY_COMPANY_CODE]
        company_name = user!![SessionManager.KEY_COMPANY_NAME]
        company_address1 = user!![SessionManager.KEY_ADDRESS1]
        company_address2 = user!![SessionManager.KEY_ADDRESS2]
        company_address3 = user!![SessionManager.KEY_ADDRESS3]
        company_phone = user!![SessionManager.KEY_PHONE_NO]
        company_gst = user!![SessionManager.KEY_COMPANY_REG_NO]
        Log.w("activity_cg", javaClass.simpleName)
        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")
        companyNametext = findViewById<View>(R.id.company_name) as TextView
        companyAddress1Text = findViewById<View>(R.id.company_addr1) as TextView
        companyAddress2Text = findViewById<View>(R.id.company_addr2) as TextView
        companyAddress3Text = findViewById<View>(R.id.company_addr3) as TextView
        companyGstText = findViewById<View>(R.id.company_gst) as TextView
        companyPhoneText = findViewById<View>(R.id.company_phone) as TextView
        transfertype = findViewById<View>(R.id.transfer_type) as TextView
        transferno = findViewById<View>(R.id.transfer_no) as TextView
        from_locat = findViewById<View>(R.id.from_loc) as TextView
        to_locat = findViewById<View>(R.id.to_loc) as TextView
        toloc_namel = findViewById<View>(R.id.toloc_name) as TextView
        fromloc_namel = findViewById<View>(R.id.fromloc_name) as TextView
        transferdate = findViewById<View>(R.id.transfer_date) as TextView
        transfertitle = findViewById<View>(R.id.title_trans) as TextView
        mainLayout = findViewById<View>(R.id.main_layout) as LinearLayout
        transferListView = findViewById<View>(R.id.rv_transferlist) as RecyclerView
        setCompanyDetails()
        try {
            if (intent != null) {
                if (intent.getStringExtra("title") == "Stock Request") {
                    transferNo = intent.getStringExtra("transferNumber")
                    type = intent.getStringExtra("title")
                    Objects.requireNonNull<ActionBar>(supportActionBar)
                        .setDisplayHomeAsUpEnabled(true)
                    supportActionBar!!.title = type
                    transfertitle!!.text = type
                    // setTitle(getIntent().getStringExtra("title"));
                    getStockRequestDetails(transferNo, "Stock Request")
                } else {
                    transferNo = intent.getStringExtra("transferNumber")
                    type = intent.getStringExtra("title")
                    Objects.requireNonNull<ActionBar>(supportActionBar)
                        .setDisplayHomeAsUpEnabled(true)
                    supportActionBar!!.title = type
                    transfertitle!!.text = type

                    // setTitle(getIntent().getStringExtra("title"));
                    getTransferDetails(transferNo, "TransferIn")
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun setCompanyDetails() {
        companyNametext!!.text = company_name
        if (company_address1 != null && !company_address1!!.isEmpty()) {
            companyAddress1Text!!.visibility = View.VISIBLE
            companyAddress1Text!!.text = company_address1
        }
        if (company_address2 != null && !company_address2!!.isEmpty()) {
            companyAddress2Text!!.visibility = View.VISIBLE
            companyAddress2Text!!.text = company_address2
        }
        if (company_address3 != null && !company_address3!!.isEmpty()) {
            companyAddress3Text!!.visibility = View.VISIBLE
            companyAddress3Text!!.text = company_address3
        }
        if (company_phone != null && !company_phone!!.isEmpty()) {
            companyPhoneText!!.text = "TEL : $company_phone"
            companyPhoneText!!.visibility = View.VISIBLE
        }
        if (company_gst != null && !company_gst!!.isEmpty()) {
            companyGstText!!.text = "CO REG NO : $company_gst"
            companyGstText!!.visibility = View.VISIBLE
        }
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
        val textviewTitle = viewActionBar.findViewById<View>(R.id.actionbar_textview) as TextView
        textviewTitle.text = title
        Objects.requireNonNull(abar)!!.setCustomView(viewActionBar, params)
        abar!!.setDisplayShowCustomEnabled(true)
        abar.setDisplayShowTitleEnabled(false)
        abar.setDisplayHomeAsUpEnabled(true)
        abar.setHomeButtonEnabled(true)
    }

    @Throws(JSONException::class)
    private fun getStockRequestDetails(transferNo: String?, type: String) {
        // Initialize a new RequestQueue instance
        val jsonBody = JSONObject()
        jsonBody.put("InvTransReqNo", transferNo)
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "InventoryTransferRequestDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url + jsonBody)
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog!!.titleText = "Generating Print Preview..."
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        transferDetailModels = ArrayList()
        transferDetailsList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonBody, { response ->
                try {
                    Log.w("TransferDetail:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray = response.optJSONArray("responseData")
                        Objects.requireNonNull(transferDetailsArray)
                        val detailObject = transferDetailsArray!!.optJSONObject(0)
                        val model = TransferDetailModel()
                        model.setNumber(detailObject.optString("invTransReqNo"))
                        model.status = detailObject.optString("invTransReqStatus")
                        model.date = detailObject.optString("docDate")
                        model.fromLocation = detailObject.optString("fromWhsCode")
                        model.toLocation = detailObject.optString("toWhsCode")
                        model.fromLocationName = detailObject.optString("fromWarehouseName")
                        model.toLocationName = detailObject.optString("toWarehouseName")
                        val itemsArray = detailObject.optJSONArray("itItem")
                        for (i in 0 until Objects.requireNonNull(itemsArray)!!.length()) {
                            val objectItem = itemsArray!!.optJSONObject(i)
                            val transferModel = TransferDetailModel.TransferDetails()
                            transferModel.description = objectItem.optString("itemName")
                            transferModel.itemCode = objectItem.optString("itemCode")
                            transferModel.qty = objectItem.optString("qty")
                            transferModel.uomCode = objectItem.optString("uomCode")
                            transferDetailsList!!.add(transferModel)
                        }
                        model.transferDetailsList = transferDetailsList
                        transferDetailModels!!.add(model)

                        // printTransfer(transferNo,transferDetailModels,type);
                        if (transferDetailsList!!.size > 0) {
                            setTransferAdapter()
                            transfertype!!.text = type
                        }
                    } else {
                        Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { error -> // Do something when error occurred
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
        jsonObjectRequest.retryPolicy = object : com.android.volley.RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    private fun getTransferDetails(transferNo: String?, type: String) {
        // Initialize a new RequestQueue instance
        val jsonBody = JSONObject()
        jsonBody.put("InvTransNo", transferNo)
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "InventoryTransferDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_urlTrans:", "$url..$jsonBody")
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog!!.titleText = "Generating Print Preview..."
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        transferDetailModels = ArrayList()
        transferDetailsList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonBody, { response ->
                try {
                    Log.w("TransferDetail:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray = response.optJSONArray("responseData")
                        Objects.requireNonNull(transferDetailsArray)
                        val detailObject = transferDetailsArray!!.optJSONObject(0)
                        val model = TransferDetailModel()
                        model.setNumber(detailObject.optString("invTransNo"))
                        model.status = detailObject.optString("invTransStatus")
                        model.date = detailObject.optString("docDate")
                        model.fromLocation = detailObject.optString("fromWhsCode")
                        model.toLocation = detailObject.optString("toWhsCode")
                        model.fromLocationName = detailObject.optString("fromWarehouseName")
                        model.toLocationName = detailObject.optString("toWarehouseName")
                        val itemsArray = detailObject.optJSONArray("itItem")
                        for (i in 0 until Objects.requireNonNull(itemsArray)!!.length()) {
                            val objectItem = itemsArray!!.optJSONObject(i)
                            val transferModel = TransferDetailModel.TransferDetails()
                            transferModel.description = objectItem.optString("itemName")
                            transferModel.qty = objectItem.optString("qty")
                            transferModel.uomCode = objectItem.optString("uomCode")
                            transferDetailsList!!.add(transferModel)
                        }
                        model.transferDetailsList = transferDetailsList
                        transferDetailModels!!.add(model)

                        // printTransfer(transferNo,transferDetailModels,type);
                        if (transferDetailsList!!.size > 0) {
                            setTransferAdapter()
                            transfertype!!.text = type
                        }
                    } else {
                        Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { error -> // Do something when error occurred
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
        jsonObjectRequest.retryPolicy = object : com.android.volley.RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    fun setTransferAdapter() {
        try {
            for (model in transferDetailModels!!) {
                transferno!!.text = transferNo
                transferdate!!.text = model.getDate()
                from_locat!!.text = model.getFromLocation()
                to_locat!!.text = model.getToLocation()
                Log.w("tran_toLoc", "" + model.getToLocationName())
                toloc_namel!!.text = model.getToLocationName()
                fromloc_namel!!.text = model.getFromLocationName()
            }
            transferListView!!.setHasFixedSize(true)
            transferListView!!.layoutManager =
                LinearLayoutManager(this@TransferPreviewPrintActivity, LinearLayoutManager.VERTICAL, false)
            adapter =
                TransferPreviewPrintAdapter(this@TransferPreviewPrintActivity, transferDetailsList, "Transfer Detail")
            transferListView!!.adapter = adapter
            mainLayout!!.visibility = View.VISIBLE
        } catch (exception: Exception) {
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_print) {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Toast.makeText(
                    applicationContext,
                    "This device does not support bluetooth",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!mBluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled :)
                Toast.makeText(
                    applicationContext,
                    "Enable bluetooth and connect the printer",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Bluetooth is enabled
                if (!printerType!!.isEmpty()) {
                    showPrintAlert()
                } else {
                    Toast.makeText(applicationContext, "Please configure Printer", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            return true
        } else if (id == android.R.id.home) {
            finish()
        } else if (id == R.id.action_pdf) {
        }
        return super.onOptionsItemSelected(item)
    }

    fun showPrintAlert() {
        val builder1 = AlertDialog.Builder(this@TransferPreviewPrintActivity)
        builder1.setMessage("Do you want to print this Transfer ?.")
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "YES"
        ) { dialog, id -> // alertInterface = dialog;
            if (printerType == "TSC Printer") {
                dialog.dismiss()
                try {
                    printTransfer(transferNo, transferDetailModels, type)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        builder1.setNegativeButton(
            "NO"
        ) { dialog, id -> dialog.cancel() }
        alert11 = builder1.create()
        alert11!!.show()
    }



    fun printTransfer(
        transferNo: String?,
        transferDetailModels: ArrayList<TransferDetailModel>?,
        type: String?
    ) {
//        if (transferDetailModels.size()>0){
//            TSCPrinter printer=new TSCPrinter(this,printerMacId,"Transfer");
//            try {
//                printer.printTransferDetail(1,transferNo,type,transferDetailModels);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.print_menu, menu)
        return true
    }
}