package com.winapp.retailpos_sap.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.winapp.KHDelivery.model.PickIistDeliveryListingModel
import com.winapp.retailpos_sap.BuildConfig
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.adapter.TransferPreviewPrintAdapter
import com.winapp.retailpos_sap.ui.model.TransferDetailModel
import com.winapp.retailpos_sap.ui.model.TransferDetailModel.TransferDetails
import com.winapp.retailpos_sap.ui.utils.CaptureSignatureView
import com.winapp.retailpos_sap.ui.utils.Constants
import com.winapp.retailpos_sap.ui.utils.FileCompressor
import com.winapp.retailpos_sap.ui.utils.ImageUtil
import com.winapp.retailpos_sap.ui.utils.SessionManager
import com.winapp.retailpos_sap.ui.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class DeliveryPreviewPrintActivity : AppCompatActivity() {
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
    private var transferDetailsList: ArrayList<TransferDetails>? = null
    private var transfertype: TextView? = null
    private var transferno: TextView? = null
    private var transfertitle: TextView? = null
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
    var username: String? = ""
    var pDialog: SweetAlertDialog? = null
    var alert11: AlertDialog? = null
    var transferNo: String? = ""
    var type: String? = null
    private var mainLayout: LinearLayout? = null
    var addSignat_Imgl: ImageView? = null
    var signatureCapture: ImageView? = null
    var captureSignatureView: CaptureSignatureView? = null
    var uploadImgDialog_txt: TextView? = null
    var uploadImgDialogLay: LinearLayout? = null
    private var spinner_pickStatus: Spinner? = null
    var alert: AlertDialog? = null
    var alertUpload: AlertDialog? = null
    var alertUploadView: AlertDialog? = null
    var signatureString = ""
    var imageString: String? = ""
    var mPhotoFile: File? = null
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_GALLERY_PHOTO = 2
    var mCompressor: FileCompressor? = null
    var currentSaveDateTime: String? = ""
    var current_latitude = "0.00"
    var current_longitude = "0.00"
    var packStatusStr = ""
    var current_addr = ""
    var spinnertxt_dialog: String? = "";
    var spinnertxt: String? = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_preview_print)
        session = SessionManager(this)
        user = session!!.getUserDetails()
        companyId = user!!.get(SessionManager.KEY_COMPANY_CODE)
        company_name = user!!.get(SessionManager.KEY_COMPANY_NAME)
        company_address1 = user!!.get(SessionManager.KEY_ADDRESS1)
        company_address2 = user!!.get(SessionManager.KEY_ADDRESS2)
        company_address3 = user!!.get(SessionManager.KEY_ADDRESS3)
        company_phone = user!!.get(SessionManager.KEY_PHONE_NO)
        company_gst = user!!.get(SessionManager.KEY_COMPANY_REG_NO)
        username = user!!.get(SessionManager.KEY_USER_NAME)

        Log.w("activity_cg", javaClass.getSimpleName().toString())
        mCompressor = FileCompressor(this)

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")
        companyNametext = findViewById(R.id.company_name)
        companyAddress1Text = findViewById(R.id.company_addr1)
        companyAddress2Text = findViewById(R.id.company_addr2)
        companyAddress3Text = findViewById(R.id.company_addr3)
        companyGstText = findViewById(R.id.company_gst)
        companyPhoneText = findViewById(R.id.company_phone)
        transfertype = findViewById(R.id.transfer_type)
        transferno = findViewById(R.id.transfer_no)
        from_locat = findViewById(R.id.from_loc)
        to_locat = findViewById(R.id.to_loc)
        toloc_namel = findViewById(R.id.toloc_name)
        fromloc_namel = findViewById(R.id.fromloc_name)
        transferdate = findViewById(R.id.transfer_date)
        mainLayout = findViewById(R.id.main_layout)
        transferListView = findViewById(R.id.rv_transferlist)
        transfertitle = findViewById(R.id.title_trans)

        spinnertxt = ""
        spinnertxt_dialog = ""
        packStatusStr = ""
        imageString = ""
        signatureString = ""

        setCompanyDetails()
        if (intent != null) {
            transferNo = intent.getStringExtra("transferNumber")
            type = intent.getStringExtra("title")
            // setTitle("Delivery");
            Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = "Delivery"
            transfertitle!!.setText(type)
            try {
                getTransferDetails(transferNo, "TransferIn")
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }
        //        try {
//            if (getIntent()!=null){
//                if (getIntent().getStringExtra("title").equals("Stock Request")){
//                    transferNo=getIntent().getStringExtra("transferNumber");
//                    type=getIntent().getStringExtra("title");
//                    setTitle(getIntent().getStringExtra("title"));
//                    getStockRequestDetails(transferNo,"Stock Request");
//                }else {
//                    transferNo=getIntent().getStringExtra("transferNumber");
//                    type=getIntent().getStringExtra("title");
//                    setTitle(getIntent().getStringExtra("title"));
//                    getTransferDetails(transferNo,"TransferIn");
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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
        val textviewTitle = viewActionBar.findViewById<TextView>(R.id.actionbar_textview)
        textviewTitle.text = title
        Objects.requireNonNull(abar)!!.setCustomView(viewActionBar, params)
        abar!!.setDisplayShowCustomEnabled(true)
        abar.setDisplayShowTitleEnabled(false)
        abar.setDisplayHomeAsUpEnabled(true)
        abar.setHomeButtonEnabled(true)
    }

    @Throws(JSONException::class)
    private fun getStockRequestDetails(transferNo: String, type: String) {
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
        transferDetailModels = ArrayList()
        transferDetailsList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("TransferDetail:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray = response.optJSONArray("responseData")!!
                        val detailObject = transferDetailsArray.optJSONObject(0)
                        val model = TransferDetailModel()
                        model.number = detailObject.optString("invTransReqNo")
                        model.status = detailObject.optString("invTransReqStatus")
                        model.date = detailObject.optString("docDate")
                        model.fromLocation = detailObject.optString("fromWhsCode")
                        model.toLocation = detailObject.optString("toWhsCode")
                        model.fromLocationName = detailObject.optString("fromWarehouseName")
                        model.toLocationName = detailObject.optString("toWarehouseName")
                        val itemsArray = detailObject.optJSONArray("itItem")
                        for (i in 0 until Objects.requireNonNull(itemsArray).length()) {
                            val objectItem = itemsArray.optJSONObject(i)
                            val transferModel = TransferDetails()
                            transferModel.description = objectItem.optString("itemName")
                            transferModel.qty = objectItem.optString("qty")
                            transferModel.uomCode = objectItem.optString("uomCode")
                            transferDetailsList!!.add(transferModel)
                        }
                        model.setTransferDetailsList(transferDetailsList)
                        transferDetailModels!!.add(model)

                        // printTransfer(transferNo,transferDetailModels,type);
                        if (transferDetailsList!!.size > 0) {
                            setTransferAdapter()
                            transfertype!!.text = type
                        }
                    } else {
                        Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT).show()
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
    }

    @Throws(JSONException::class)
    private fun getTransferDetails(transferNo: String?, type: String) {
        // Initialize a new RequestQueue instance
        val jsonBody = JSONObject()
        jsonBody.put("InvTransNo", transferNo)
        val requestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "InventoryTransferDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_urlTrans:", "$url..$jsonBody")
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.setBarColor(Color.parseColor("#A5DC86"))
        pDialog!!.setTitleText("Generating Print Preview...")
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        transferDetailModels = ArrayList()
        transferDetailsList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("TransferDetail:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray = response.optJSONArray("responseData")!!
                        val detailObject = transferDetailsArray.optJSONObject(0)
                        val model = TransferDetailModel()
                        model.number = detailObject.optString("invTransNo")
                        model.status = detailObject.optString("invTransStatus")
                        model.date = detailObject.optString("docDate")
                        model.fromLocation = detailObject.optString("fromWhsCode")
                        model.toLocation = detailObject.optString("toWhsCode")
                        model.fromLocationName = detailObject.optString("fromWarehouseName")
                        model.toLocationName = detailObject.optString("toWarehouseName")
                        val itemsArray = detailObject.optJSONArray("itItem")
                        for (i in 0 until Objects.requireNonNull(itemsArray).length()) {
                            val objectItem = itemsArray.optJSONObject(i)
                            val transferModel = TransferDetails()
                            transferModel.description = objectItem.optString("itemName")
                            transferModel.qty = objectItem.optString("qty")
                            transferModel.uomCode = objectItem.optString("uomCode")
                            transferDetailsList!!.add(transferModel)
                        }
                        model.setTransferDetailsList(transferDetailsList)
                        transferDetailModels!!.add(model)

                        // printTransfer(transferNo,transferDetailModels,type);
                        if (transferDetailsList!!.size > 0) {
                            setTransferAdapter()
                            transfertype!!.text = type
                        }
                    } else {
                        Toast.makeText(applicationContext, statusMessage, Toast.LENGTH_SHORT).show()
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
    }

    fun setTransferAdapter() {
        try {
            for (model in transferDetailModels!!) {
                transferno!!.text = transferNo
                transferdate!!.text = model.date
                from_locat!!.text = model.fromLocation
                to_locat!!.text = model.toLocation
                Log.w("tran_toLoc", "" + model.toLocationName)
                toloc_namel!!.text = model.toLocationName
                fromloc_namel!!.text = model.fromLocationName
            }
            transferListView!!.setHasFixedSize(true)
            transferListView!!.setLayoutManager(
                LinearLayoutManager(
                    this@DeliveryPreviewPrintActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            )
            adapter = TransferPreviewPrintAdapter(
                this@DeliveryPreviewPrintActivity,
                transferDetailsList,
                "Transfer Detail"
            )
            transferListView!!.setAdapter(adapter)
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
                    Toast.makeText(
                        applicationContext,
                        "Please configure Printer",
                        Toast.LENGTH_SHORT
                    ).show()
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
        val builder1 = AlertDialog.Builder(this@DeliveryPreviewPrintActivity)
        builder1.setMessage("Do you want to print this Transfer ?.")
        builder1.setCancelable(false)
        builder1.setPositiveButton("YES") { dialog, id -> // alertInterface = dialog;
            if (printerType == "TSC Printer") {
                dialog.dismiss()
                try {
                    printTransfer(transferNo, transferDetailModels, type)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        builder1.setNegativeButton("NO") { dialog, id -> dialog.cancel() }
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

    fun showUploadImageAlert() {
        val alertDialog = AlertDialog.Builder(this@DeliveryPreviewPrintActivity)
        val customLayout: View = layoutInflater.inflate(R.layout.pick_image_upload_dialog, null)
        alertDialog.setView(customLayout)
        uploadImgDialogLay = customLayout.findViewById<LinearLayout>(R.id.attachement_layout_inv)
        uploadImgDialog_txt = customLayout.findViewById<TextView>(R.id.select_Img_pickdel)
        addSignat_Imgl = customLayout.findViewById<ImageView>(R.id.addSignat_Img)
        signatureCapture = customLayout.findViewById(R.id.signature_capture)
        val submit_imgl = customLayout.findViewById<TextView>(R.id.submit_img_inv)
        val invNo_txt = customLayout.findViewById<TextView>(R.id.invNo_txt_edit)
        val close_btn_edit_invl = customLayout.findViewById<ImageView>(R.id.close_btn_pickdel)
        spinner_pickStatus = customLayout.findViewById<Spinner>(R.id.spinner_status_pickD)

        val mSig = CaptureSignatureView(this@DeliveryPreviewPrintActivity, null)
        // mContent.addView(mSig, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        invNo_txt.text = transferNo
//        Log.w("pickmodelaa:", pickModel.code!!)

        uploadImgDialog_txt!!.setOnClickListener {
            if (uploadImgDialog_txt!!.getTag() == "view_image") {
                showImage()
            } else {
                selectImage()
            }
        }

        if (mPhotoFile != null && mPhotoFile!!.length() > 0) {
            uploadImgDialog_txt!!.setText("View Image")
            uploadImgDialog_txt!!.setTag("view_image")
        } else {
            uploadImgDialog_txt!!.setText("Select Image")
            uploadImgDialog_txt!!.setTag("select_image")
        }

        val status = arrayOf("Picked", "Not Picked")

        val langAdapter = ArrayAdapter<CharSequence>(this, R.layout.cust_spinner_item, status)
        langAdapter.setDropDownViewResource(R.layout.item_grouplist_spinner)
        spinner_pickStatus!!.setAdapter(langAdapter)

//        uploadImgDialogLay!!.setOnClickListener(OnClickListener {
//            if (uploadImgDialog_txt!!.getTag() == "view_image") {
//                showImage()
//            } else {
//                selectImage()
//            }
//        })
        addSignat_Imgl!!.setOnClickListener {
            showSignatureAlert(signatureCapture!!)
        }

        close_btn_edit_invl.setOnClickListener {
            imageString = ""
            signatureString = ""
            mPhotoFile = null
            alertUpload!!.dismiss()
        }

        submit_imgl.setOnClickListener {

            if(signatureString.isNotEmpty() || imageString!!.isNotEmpty()){
                spinnertxt_dialog = "OC"
                packStatusStr = "Picked" // todo

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val currentDateandTime = sdf.format(Date())
                currentSaveDateTime = currentDateandTime

                try {
                    val obj = JSONObject()
                    obj.put("DocNum", transferNo)
                    obj.put("currentDateTime", currentSaveDateTime)
                    obj.put("Username", username)
                    obj.put("status", spinnertxt_dialog)
                    obj.put("PackStatus", packStatusStr)
                    obj.put("latitude", current_latitude)
                    obj.put("longitude", current_longitude)
                    obj.put("CurrentAddress", current_addr)
                    obj.put("SendMail", "")
                    obj.put("image", imageString)
                    obj.put("signature", signatureString)

                    Log.w("imgSign_","$obj")

                    savePicklistDeliveryApi(obj)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }else{
                Toast.makeText(applicationContext,  "Choose any one of the option !", Toast.LENGTH_SHORT).show()

//                if (spinner_pickStatus!!.selectedItem.equals("Picked")) {
//                    spinnertxt_dialog = "OC"
//                } else  {
//                    spinnertxt_dialog = "O"
//                }
                // //  spinnertxt_dialog = "OC"
            }
//            {"invoiceNumber":"18","currentDateTime":"20250616_171118","customerCode":"0005","Username":"ST01",
//            "status":"C",
//                "latitude":"10.96440894","longitude":"78.44143506","image":"","signature":""}

        }
        alertUpload = alertDialog.create()
        alertUpload!!.setCanceledOnTouchOutside(false)
        alertUpload!!.show()
    }
    fun savePicklistDeliveryApi(jsonBody: JSONObject) {
        try {
            pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog!!.setCancelable(false)
            val requestQueue = Volley.newRequestQueue(this)
            Log.w("del_request:", jsonBody.toString())
            var URL = ""
            URL = Constants.BASEURL + "PostingSignImageInventory"

            Log.w("url_Del_save:", URL)
            pDialog!!.setTitleText("Saving Delivery...")
            pDialog!!.show()

            val salesOrderRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, URL, jsonBody,
                Response.Listener { response: JSONObject ->
                    Log.w("picklis_del_sav:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    val message = response.optString("statusMessage")

                    var responseData: JSONObject? = null
                    responseData = response.optJSONObject("responseData")
                    if (statusCode == "1") {
                        //  val docNum = responseData.optString("docNum")
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                        val intent = Intent(applicationContext, NavigationActivity::class.java)
                        startActivity(intent)
                        finish()
                        alertUpload!!.dismiss()
                        imageString = ""
                        signatureString = ""
//                        if (StockTakeAddActivity.isPrintEnable) {
//                            intent.putExtra("docNum", docNum)
//                        }
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
                    val params = java.util.HashMap<String, String>()
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
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun showImage() {
        val builder = AlertDialog.Builder(this@DeliveryPreviewPrintActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.image_view_layout, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.invoice_image)
        Glide.with(this)
            .load(mPhotoFile)
            .error(R.drawable.no_image_found)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            }).into(imageView)
        builder.setCancelable(false)
        builder.setTitle("Invoice Image")
        builder.setView(dialogView)
        builder.setNeutralButton(
            "NEW IMAGE"
        ) { dialogInterface, i -> selectImage() }
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            uploadImgDialog_txt!!.setTag("view_image")
            uploadImgDialog_txt!!.setText("View Image")
            dialog.dismiss()
        }.create().show()
    }
    fun showSignatureAlert(signatureCaptu:ImageView) {
        val alertDialog = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.signature_layout, null)
        alertDialog.setView(customLayout)
        val acceptButton = customLayout.findViewById<Button>(R.id.buttonYes)
        val cancelButton = customLayout.findViewById<Button>(R.id.buttonNo)
        val clearButton = customLayout.findViewById<Button>(R.id.buttonClear)

        val mContent = customLayout.findViewById<LinearLayout>(R.id.signature_layout)
        acceptButton.setEnabled(false)
        acceptButton.setAlpha(0.4f)
        val mSig = CaptureSignatureView(this@DeliveryPreviewPrintActivity, null) {
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
            signatureCaptu!!.setImageBitmap(signature)
            signatureString = ImageUtil.convertBimaptoBase64(signature)
            //  Utils.setSignature(signatureString)
            alert!!.dismiss()
            Log.w("SignatureString:", signatureString)
        }
        cancelButton.setOnClickListener { alert!!.dismiss() }
        clearButton.setOnClickListener { mSig.ClearCanvas() }
        alert = alertDialog.create()
        alert!!.setCanceledOnTouchOutside(false)
        alert!!.show()
    }
    fun selectImage() {
        val items = arrayOf<CharSequence>(
            "Take Photo",  /* "Choose from Library",*/
            "Cancel"
        )
        val builder = AlertDialog.Builder(this@DeliveryPreviewPrintActivity)
        builder.setItems(
            items
        ) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Take Photo") {
                requestStoragePermission(true)
            } //else if (items[item].equals("Choose from Library")) {
            else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }
    fun getRealPathFromUri(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri!!, proj, null, null, null)
            assert(cursor != null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }
    private fun requestStoragePermission(isCamera: Boolean) {
        var permission = arrayOf<String?>(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
        }
        Dexter.withContext(this)
            .withPermissions(*permission)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        } else {
                            dispatchGalleryIntent()
                        }
                    }
                    for (i in report.deniedPermissionResponses.indices) {
                        Log.d(
                            "cg_perm", report.deniedPermissionResponses[i].permissionName
                        )
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener { error: DexterError? ->
                Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT)
                    .show()
            }
            .onSameThread()
            .check()
    }
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage(
            "This app needs permission to use this feature. You can grant them in app settings."
        )
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, 101)
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mCompressor!!.compressToFile(mPhotoFile)
                    imageString = ImageUtil.getBase64StringImage(mPhotoFile)
                    //  Log.w("GivenImage1:",imageString);
                    Utils.w("GivenImage1Pick", imageString)
                    showImage()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                /* Glide.with(MainActivity.this)
                        .load(mPhotoFile)
                        .apply(new RequestOptions().centerCrop()
                                .circleCrop()
                                .placeholder(R.drawable.profile_pic_place_holder))
                        .into(imageViewProfilePic);*/
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                val selectedImage = data!!.data
                try {
                    mPhotoFile =
                        mCompressor!!.compressToFile(File(getRealPathFromUri(selectedImage)))
                    uploadImgDialog_txt!!.setText(selectedImage.toString())
                    imageString = ImageUtil.getBase64StringImage(mPhotoFile)
                    // Log.w("GivenImage2:",imageString);
                    Utils.w("GivenImage2Pick", imageString)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
//            if (requestCode == CUST_RESULT_CODE) {
//                if(data != null) {
//                    val customername = data!!.getStringExtra("customerName")
//                    val customercode = data.getStringExtra("customerCode")
//                    custFilterAutol!!.setText(customername)
//                    selectCustomerCode = customercode
//                    selectCustomerName = customername
//                }
//            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
                mPhotoFile = photoFile
                Log.w("uploadImgpic",""+mPhotoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    /**
     * Select image fro gallery
     */
    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO)
    }
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.delivery_print_menu, menu)
        val uploadItem = menu.findItem(R.id.upload_pick_menu)

        uploadItem.setOnMenuItemClickListener {
            //  showUploadImageAlert(pickModel)
            showUploadImageAlert()
            true
        }
        return true
    }
}