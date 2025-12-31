package com.winapp.retailpos_sap.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
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
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
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
import com.winapp.retailpos_sap.ui.adapter.TransferDraftListAdapter
import com.winapp.retailpos_sap.ui.adapter.TransferDraftListAdapter.UploadClickListener
import com.winapp.retailpos_sap.ui.db.DBHelper
import com.winapp.retailpos_sap.ui.model.TransferDetailModel
import com.winapp.retailpos_sap.ui.model.TransferDetailModel.TransferDetails
import com.winapp.retailpos_sap.ui.model.TransferModel
import com.winapp.retailpos_sap.ui.utils.CaptureSignatureView
import com.winapp.retailpos_sap.ui.utils.Constants
import com.winapp.retailpos_sap.ui.utils.FileCompressor
import com.winapp.retailpos_sap.ui.utils.ImageUtil
import com.winapp.retailpos_sap.ui.utils.SessionManager
import com.winapp.retailpos_sap.ui.utils.Utils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects

class TransferDraftListActivity : NavigationActivity(), View.OnClickListener, UploadClickListener {
    var stockRequestList: RecyclerView? = null
    var requestAdapter: TransferDraftListAdapter? = null
    var requestList: ArrayList<TransferModel?>? = null
    var dbHelper: DBHelper? = null
    private var pDialog: SweetAlertDialog? = null
    private var username: String? = null
    private var locationCode: String? = null
    private var stockRequestText: EditText? = null
    var dialog: ProgressDialog? = null
    var sharedPreferences: SharedPreferences? = null
    var transferDetailModels: ArrayList<TransferDetailModel?>? = null
    var transferDetailsList: ArrayList<TransferDetails>? = null
    var transferType: String? = "Transfer In"
    var addRequest: Button? = null
    var emptyText: TextView? = null
    var transferSize: TextView? = null
    var requestNoTitle: TextView? = null
    var currentDate: String = ""
    private var fromDate: TextView? = null
    private var toDate: TextView? = null
    private var searchButton: TextView? = null
    private var btn_cancel: TextView? = null
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private val mHour = 0
    private val mMinute = 0
    private var requestSentView: View? = null
    private var requestReceiveView: View? = null
    private var mode = "In"
    var user: HashMap<String?, String?>? = null
    var session: SessionManager? = null
    var searchFilterView: View? = null
    private val requestMode = "Receive"
    var alertUpload: AlertDialog? = null
    var alertUploadView: AlertDialog? = null
    var signatureString = ""
    var addSignat_Imgl: ImageView? = null
    var signatureCapture: ImageView? = null
    var captureSignatureView: CaptureSignatureView? = null
    var uploadImgDialog_txt: TextView? = null
    var uploadImgDialogLay: LinearLayout? = null
    var uploadImgLay: LinearLayout? = null
    var mPhotoFile: File? = null
    var spinnertxt_dialog: String? = "";
    var packStatusStr = ""
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_GALLERY_PHOTO = 2
    var mCompressor: FileCompressor? = null
    var currentSaveDateTime: String? = ""
    var imageString: String? = ""
    var current_latitude = "0.00"
    var current_longitude = "0.00"
    private var spinner_pickStatus: Spinner? = null
    var alert: AlertDialog? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentFrameLayout = findViewById<FrameLayout?>(R.id.content_frame)

        //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_transfer_draft_list, contentFrameLayout)
        getSupportActionBar()!!.setTitle("Transfer Draft List")
        Log.w("activity_cg", javaClass.getSimpleName().toString())

        dbHelper = DBHelper(this)

        stockRequestList = findViewById<RecyclerView?>(R.id.transferProductList)
        stockRequestText = findViewById<EditText?>(R.id.transfer_search)
        addRequest = findViewById<Button?>(R.id.add_transfer)
        emptyText = findViewById<TextView?>(R.id.empty_text)
        transferSize = findViewById<TextView?>(R.id.stockreq_size)
        requestNoTitle = findViewById<TextView?>(R.id.transfer_no)
        requestSentView = findViewById<View?>(R.id.request_sent_view)
        requestReceiveView = findViewById<View?>(R.id.request_receive_view)
        searchButton = findViewById<TextView?>(R.id.btn_search_Trans)
        fromDate = findViewById<TextView?>(R.id.from_date_Trans)
        toDate = findViewById<TextView?>(R.id.to_date_Trans)
        btn_cancel = findViewById<TextView?>(R.id.btn_cancel_Trans)
        searchFilterView = findViewById<View?>(R.id.search_filter_trans_draft)
        mCompressor = FileCompressor(this)

        session = SessionManager(this)
        user = session!!.getUserDetails()
        username = user!!.get(SessionManager.KEY_USER_NAME)
        locationCode = user!!.get(SessionManager.KEY_LOCATION_CODE)

        requestNoTitle!!.setText("REQUEST NO")

        val c = Calendar.getInstance().getTime()
        println("Current time => " + c)
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        currentDate = df1.format(c)

        val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        fromDate!!.setText(formattedDate)
        toDate!!.setText(formattedDate)

        dbHelper!!.removeAllInvoiceItems()
        dbHelper!!.removeAllReturn()

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")

        if (getIntent() != null) {
            val docNumber = getIntent().getStringExtra("docNum")
            //            assert docNumber != null;
            if (docNumber != null && !docNumber.isEmpty()) {
                transferType = getIntent().getStringExtra("transferType")
                try {
                    getStockRequestDetails(1, docNumber, transferType, "Print")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }


        stockRequestText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (requestAdapter != null) {
                    requestAdapter!!.getFilter().filter(s.toString())
                }
            }
        })

        getTransferdraftList(currentDate, currentDate)
        mode = "In"

        fromDate!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // fromDate.setText(Utils.getDate(StockRequestListActivity.this,fromDate));
                getDate(fromDate!!)
            }
        })

        toDate!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //toDate.setText(Utils.getDate(StockRequestListActivity.this,toDate));
                getDate(toDate!!)
            }
        })

        searchButton!!.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun onClick(v: View?) {
                if (!fromDate!!.getText().toString().isEmpty() && !toDate!!.getText().toString()
                        .isEmpty()
                ) {
                    val fromdate =
                        Utils.convertDate(fromDate!!.getText().toString(), "dd-MM-yyyy", "yyyyMMdd")
                    val todate =
                        Utils.convertDate(toDate!!.getText().toString(), "dd-MM-yyyy", "yyyyMMdd")
                    getTransferdraftList(fromdate, todate)
                } else {
                    Toast.makeText(
                        getApplicationContext(),
                        "Select the Date to Search..!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
        btn_cancel!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                fromDate!!.setText(formattedDate)
                toDate!!.setText(formattedDate)
                searchFilterView!!.setVisibility(View.GONE)

                //  getTransferInRequest(transferMode, currentDate, currentDate);
            }
        })
    }

    fun getDate(dateTextView: TextView) {
        // Get Current Date
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this@TransferDraftListActivity,
            object : OnDateSetListener {
                override fun onDateSet(
                    view: DatePicker?,
                    year: Int,
                    monthOfYear: Int,
                    dayOfMonth: Int
                ) {
                    dateTextView.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                }
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }


    fun getTransferdraftList(fromdate: String?, todate: String?) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "InventoryTransferDraftList"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("User", username)
            jsonObject.put("FromDate", fromdate)
            jsonObject.put("ToDate", todate)
            jsonObject.put("DocStatus", "")
            jsonObject.put("WarehouseCode", locationCode)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.w("Given_url:", url + "---" + jsonObject.toString())

        if (dialog != null && dialog!!.isShowing()) dialog!!.cancel()
        dialog = ProgressDialog(this@TransferDraftListActivity)
        dialog!!.setMessage("Loading TransferDraft List...")
        dialog!!.setCancelable(false)
        //        if (!dialog.isShowing()) {
        dialog!!.show()
        //
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject? ->
                try {
                    dialog!!.dismiss()
                    Log.w("Response_StockRequest:", response.toString())
                    requestList = ArrayList<TransferModel?>()

                    val statusCode = response!!.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray = response.optJSONArray("responseData")
                            for (i in 0 until transferDetailsArray.length()) {

                                val `object` = transferDetailsArray.optJSONObject(i)
                            val model = TransferModel()
                            model.setTransferNo(`object`.optString("docNum"))
                            model.setDate(`object`.optString("docDate"))
                            model.setFromLocation(`object`.optString("fromWarehouse"))
                            model.setToLocation(`object`.optString("toWarehouse"))
                            model.setUser(`object`.optString("user"))
                            model.setStatus(`object`.optString("docStatus"))
                            requestList!!.add(model)
                        }
                        //                            "docNum": "6",
//                                    "docEntry": "4",
//                                    "docStatus": "O",
//                                    "docDate": "22/12/20",
//                                    "docDueDate": "22/12/20",
//                                    "docTotal": "207.500000",
//                                    "remark": "",
//                                    "fromWarehouse": "01",
//                                    "toWarehouse": "01",
//                                    "fromWarehouseName": "Head Office",
//                                    "toWarehouseName": "Head Office"
                        if (requestList!!.size > 0) {
                            stockRequestList!!.setVisibility(View.VISIBLE)
                            emptyText!!.setVisibility(View.GONE)
                            searchFilterView!!.setVisibility(View.GONE)
                            setTransferListAdapter(requestList!!)
                        } else {
                            stockRequestList!!.setVisibility(View.GONE)
                            emptyText!!.setVisibility(View.VISIBLE)
                            transferSize!!.setText("(0) Products")
                        }
                    } else {
                        stockRequestList!!.setVisibility(View.GONE)
                        emptyText!!.setVisibility(View.VISIBLE)
                        transferSize!!.setText("(0) Products")
                        Toast.makeText(getApplicationContext(), statusMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError? ->
                dialog!!.dismiss()
                // Do something when error occurred
                Log.w("Error_throwing:", error.toString())
            }) {
            override fun getHeaders(): MutableMap<String?, String?> {
                val params = HashMap<String?, String?>()
                val creds =
                    String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD)
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params.put("Authorization", auth)
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
            override fun retry(error: VolleyError?) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add<JSONObject?>(jsonObjectRequest)
    }

    fun setTransferListAdapter(stockRequestList: ArrayList<TransferModel?>) {
        //try {
        transferSize!!.setText("(" + stockRequestList.size + ")" + " Products")

        requestAdapter = TransferDraftListAdapter(
            this,
            stockRequestList,
            object : TransferDraftListAdapter.CallBack {
                override fun callDescription(requestNo: String, mode: String) {
                    Log.w("GivenTransferNo::", requestNo.toString())
                    if (mode == "Print") {
                        try {
                            getStockRequestDetails(
                                1,
                                requestNo.toString(),
                                "Stock Request",
                                "Print"
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        val intent = Intent(
                            getApplicationContext(),
                            TransferPreviewPrintActivity::class.java
                        )
                        intent.putExtra("title", "Stock Request")
                        intent.putExtra("transferNumber", requestNo)
                        startActivity(intent)
                    }
                }

                override fun convertTransfer(requestId: String) {
                    try {
                        getStockRequestDetails(1, requestId.toString(), "Stock Request", "")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            },this@TransferDraftListActivity)
        val mNoOfColumns = Utils.calculateNoOfColumns(getApplicationContext(), 200f)
        this.stockRequestList!!.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        this.stockRequestList!!.setItemAnimator(DefaultItemAnimator())
        this.stockRequestList!!.setAdapter(requestAdapter)
        //categoriesView.setVisibility(View.VISIBLE);
        //emptyLayout.setVisibility(View.GONE);
//        }catch (Exception ex){
//            Log.e("TAG","Error in Populating the data:"+ex.getMessage());
//        }
    }

    @Throws(JSONException::class)
    private fun getStockRequestDetails(
        copy: Int,
        transferNo: String?,
        type: String?,
        action: String
    ) {
        // Initialize a new RequestQueue instance
        val jsonBody = JSONObject()
        jsonBody.put("InvTransReqNo", transferNo)
        val requestQueue = Volley.newRequestQueue(this)
        val url = Constants.BASEURL + "InventoryTransferRequestDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url)
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"))
        if (action == "Print") {
            pDialog!!.setTitleText("Generating Print Preview...")
        } else {
            pDialog!!.setTitleText("Processing Please wait...")
        }
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        transferDetailModels = ArrayList<TransferDetailModel?>()
        transferDetailsList = ArrayList<TransferDetails>()

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener { response: JSONObject? ->
                try {
                    Log.w("StockRequestDetails:", response.toString())

                    pDialog!!.dismiss()
                    val statusCode = response!!.optString("statusCode")
                    val statusMessage = response.optString("statusMessage")
                    if (statusCode == "1") {
                        val transferDetailsArray: JSONArray? =
                            checkNotNull(response.optJSONArray("responseData"))
                        val detailObject = transferDetailsArray!!.optJSONObject(0)
                        val model = TransferDetailModel()
                        model.setNumber(detailObject.optString("invTransReqNo"))
                        model.setStatus(detailObject.optString("invTransReqStatus"))
                        model.setDate(detailObject.optString("docDate"))
                        model.setFromLocation(detailObject.optString("fromWhsCode"))
                        model.setToLocation(detailObject.optString("toWhsCode"))
                        model.setFromLocationName(detailObject.optString("fromWarehouseName"))
                        model.setToLocationName(detailObject.optString("toWarehouseName"))

                        val itemsArray = detailObject.optJSONArray("itItem")

                        for (i in 0 until itemsArray.length()) {

                            val objectItem = itemsArray!!.optJSONObject(i)
                            val transferModel = TransferDetails()
                            transferModel.setItemCode(objectItem.optString("itemCode"))
                            transferModel.setDescription(objectItem.optString("itemName"))
                            transferModel.setQty(objectItem.optString("qty"))
                            transferModel.setUomCode(objectItem.optString("uomCode"))
                            transferDetailsList!!.add(transferModel)
                        }
                        model.setTransferDetailsList(transferDetailsList)
                        transferDetailModels!!.add(model)

                        if (action == "Print") {
                            printTransfer(transferNo, transferDetailModels!!, type)
                        } else {
                            convertAndRedirect(
                                transferNo,
                                transferDetailModels!!,
                                transferDetailsList!!
                            )
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), statusMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError? ->
                // Do something when error occurred
                // pDialog.dismiss();
                Log.w("Error_throwing:", error.toString())
            }) {
            override fun getHeaders(): MutableMap<String?, String?> {
                val params = HashMap<String?, String?>()
                val creds =
                    String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD)
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params.put("Authorization", auth)
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
            override fun retry(error: VolleyError?) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add<JSONObject?>(jsonObjectRequest)
    }

    fun convertAndRedirect(
        requestNo: String?,
        transferDetailModels: ArrayList<TransferDetailModel?>,
        transferDetail: ArrayList<TransferDetails>
    ) {
        val return_qty = "0"
        val price_value = "0.00"
        val qty_value = "0"
        dbHelper!!.removeAllInvoiceItems()
        for (model in transferDetail) {
            dbHelper!!.insertCreateInvoiceCart(
                model.getItemCode(),
                model.getDescription(),
                model.getUomCode(),
                "",
                model.getQty().toDouble().toInt().toString() + "",
                return_qty,
                model.getQty().toDouble().toInt().toString() + "", "0",
                price_value,
                "0",
                "0.00",
                "0.00",
                "0.00",
                "0.00", "",
                "",
                "",
                "",
                "0",
                "", "", "", "", ""
            )
        }
        val count = dbHelper!!.numberOfRowsInInvoice()
        val fromLocation = transferDetailModels.get(0)!!.getFromLocation()
        val toLocation = transferDetailModels.get(0)!!.getToLocation()
        val invoiceDate = transferDetailModels.get(0)!!.getDate()
        val transferType = "Convert Transfer"
        //        if (count==transferDetail.size()){
//            Intent intent=new Intent(getApplicationContext(),TransferProductAddActivity.class);
//            intent.putExtra("fromLocationCode",fromLocation);
//            intent.putExtra("toLocationCode",toLocation);
//            intent.putExtra("currentDate",invoiceDate);
//            intent.putExtra("requestNo",requestNo);
//            intent.putExtra("transferType",transferType);
//            startActivity(intent);
//        }
    }

    fun printTransfer(
        transferNo: String?,
        transferDetailModels: ArrayList<TransferDetailModel?>,
        type: String?
    ) {
        if (transferDetailModels.size > 0) {
            if (Utils.validatePrinterConfiguration(this, printerType, printerMacId)) {
                //  TSCPrinter printer=new TSCPrinter(this,printerMacId,"Transfer");
//            try {
//                printer.printTransferDetail(1,transferNo,type,transferDetailModels);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            }
        } else {
            Toast.makeText(
                getApplicationContext(),
                "Please configure the Printer",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onClick(v: View?) {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.transfer_list_menu, menu)

        val action_add = menu.findItem(R.id.action_add)
        action_add.setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) { //finish();
            onBackPressed()
        } else if (item.getItemId() == R.id.action_add) {
            //  transferType = transferMode;
            // Intent intent=new Intent(getApplicationContext(),TransferActivity.class);
            //intent.putExtra("transferType",transferType);
            // startActivity(intent);
            val intent = Intent(getApplicationContext(), StockRequestAddActivity::class.java)
            intent.putExtra("requestType", requestMode)
            startActivity(intent)
        } else if (item.getItemId() == R.id.action_filter) {
            if (searchFilterView!!.getVisibility() == View.VISIBLE) {
                searchFilterView!!.setVisibility(View.GONE)
                //                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
            } else {
                searchFilterView!!.setVisibility(View.VISIBLE)
                //                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
        }
    }

    fun showPopupMenu(transferModels: TransferModel, view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.transfer_menu)

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            if (item!!.getItemId() == R.id.convert_transfer_menu) {
                val intent =
                    Intent(this@TransferDraftListActivity, ConvertTransferAddActivity::class.java)
                intent.putExtra("convertTranferNo", transferModels.transferNo)

                startActivity(intent)
                true
            }
            false
        })

        popupMenu.show()
    }

    override fun uploadSelected(transferModels: TransferModel?, view: View?) {
        showUploadImageAlert(transferModels!!)
    }

    fun showUploadImageAlert(transferModels: TransferModel) {
        val alertDialog = AlertDialog.Builder(this@TransferDraftListActivity)
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

        val mSig = CaptureSignatureView(this@TransferDraftListActivity, null)
        // mContent.addView(mSig, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        invNo_txt.text = transferModels.transferNo

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

            if(imageString!!.isNotEmpty() || signatureString.isNotEmpty()){
                spinnertxt_dialog = "C"
                packStatusStr = "Delivered"

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val currentDateandTime = sdf.format(Date())
                currentSaveDateTime = currentDateandTime

                try {
                    val obj = JSONObject()

                    obj.put("DraftDocNum", transferModels.transferNo)
                    obj.put("User", username)
                    obj.put("image", imageString)
                    obj.put("signature", signatureString)

                    savePicklistDeliveryApi(obj)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }else{
                Toast.makeText(applicationContext,  "Choose any one option!", Toast.LENGTH_SHORT).show()

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
            Log.w("picklDel_request:", jsonBody.toString())
            var URL = ""
            URL = Constants.BASEURL + "PostingConvertInventoryTransfer"
            Log.w("url_picklDel_save:", URL)
            pDialog!!.setTitleText("Saving Transfer Draft...")
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

                        val intent = Intent(applicationContext, TransferDraftListActivity::class.java)
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

    @SuppressLint("MissingInflatedId")
    fun showViewImageAlert(pickModel: PickIistDeliveryListingModel) {
        val alertDialog = AlertDialog.Builder(this@TransferDraftListActivity)
        val customLayout: View = layoutInflater.inflate(R.layout.pick_view_image_dialog, null)
        alertDialog.setView(customLayout)
        var view_imgl = customLayout.findViewById<ImageView>(R.id.view_pick_img)
        var view_signaturel = customLayout.findViewById<ImageView>(R.id.view_pick_signature)

        val view_img_ok = customLayout.findViewById<Button>(R.id.view_pick_ok)
        val invNo_txt_view = customLayout.findViewById<TextView>(R.id.invNo_pick_view)
        invNo_txt_view.setText(pickModel.invNumber)

        view_img_ok.setOnClickListener {
            alertUploadView!!.dismiss()
        }

        Glide.with(this)
            .load(pickModel.imageUrl)
            .error(R.drawable.no_image_found)
            .into(view_imgl)

        Glide.with(this)
            .load(pickModel.signatureUrl)
            .error(R.drawable.no_image_found)
            .into(view_signaturel)

        alertUploadView = alertDialog.create()
        alertUploadView!!.setCanceledOnTouchOutside(true)
        alertUploadView!!.show()
    }

    fun showImage() {
        val builder = AlertDialog.Builder(this@TransferDraftListActivity)
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
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
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

    fun selectImage() {
        val items = arrayOf<CharSequence>(
            "Take Photo",  /* "Choose from Library",*/
            "Cancel"
        )
        val builder = AlertDialog.Builder(this@TransferDraftListActivity)
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
        val mSig = CaptureSignatureView(this@TransferDraftListActivity, null) {
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
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
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
    companion object {
        var printerMacId: String? = null
        var printerType: String? = null
    }
}