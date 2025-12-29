package com.winapp.retailpos_sap.ui.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.winapp.retailpos_sap.R;
import com.winapp.retailpos_sap.ui.adapter.TransferDraftListAdapter;
import com.winapp.retailpos_sap.ui.db.DBHelper;
import com.winapp.retailpos_sap.ui.model.TransferDetailModel;
import com.winapp.retailpos_sap.ui.model.TransferModel;
import com.winapp.retailpos_sap.ui.utils.Constants;
import com.winapp.retailpos_sap.ui.utils.SessionManager;
import com.winapp.retailpos_sap.ui.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TransferDraftListActivity extends NavigationActivity implements
        View.OnClickListener  {

    public RecyclerView stockRequestList;
    public TransferDraftListAdapter requestAdapter;
    public ArrayList<TransferModel> requestList;
    public DBHelper dbHelper;
    private SweetAlertDialog pDialog;
    private String username;
    private String locationCode;
    private EditText stockRequestText;
    ProgressDialog dialog;
    static String printerMacId;
    static String printerType;
    public SharedPreferences sharedPreferences;
    ArrayList<TransferDetailModel> transferDetailModels;
    ArrayList<TransferDetailModel.TransferDetails> transferDetailsList;
    public String transferType = "Transfer In";
    public Button addRequest;
    public TextView emptyText , transferSize;
    public TextView requestNoTitle;
    public String currentDate = "";
    private TextView fromDate;
    private TextView toDate;
    private TextView searchButton , btn_cancel;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private View requestSentView;
    private View requestReceiveView;
    private String mode = "In";
    HashMap<String ,String> user;
    SessionManager session;
    View searchFilterView;
    private String requestMode = "Receive" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_transfer_draft_list, contentFrameLayout);
        getSupportActionBar().setTitle("Transfer Draft List");
        Log.w("activity_cg",getClass().getSimpleName().toString());

        dbHelper=new DBHelper(this);
        stockRequestList =findViewById(R.id.transferProductList);
        stockRequestText =findViewById(R.id.transfer_search);
        addRequest =findViewById(R.id.add_transfer);
        emptyText=findViewById(R.id.empty_text);
        transferSize = findViewById(R.id.stockreq_size);
        requestNoTitle=findViewById(R.id.transfer_no);
        requestSentView=findViewById(R.id.request_sent_view);
        requestReceiveView=findViewById(R.id.request_receive_view);
        searchButton = findViewById(R.id.btn_search_Trans);
        fromDate = findViewById(R.id.from_date_Trans);
        toDate = findViewById(R.id.to_date_Trans);
        btn_cancel = findViewById(R.id.btn_cancel_Trans);
        searchFilterView=findViewById(R.id.search_filter_trans_draft);

        session=new SessionManager(this);
        user=session.getUserDetails();
        username=user.get(SessionManager.KEY_USER_NAME);
        locationCode=user.get(SessionManager.KEY_LOCATION_CODE);

        requestNoTitle.setText("REQUEST NO");

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        currentDate = df1.format(c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        fromDate.setText(formattedDate);
        toDate.setText(formattedDate);

        dbHelper.removeAllInvoiceItems();
        dbHelper.removeAllReturn();

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType=sharedPreferences.getString("printer_type","");
        printerMacId=sharedPreferences.getString("mac_address","");

        if (getIntent()!=null){
            String docNumber=getIntent().getStringExtra("docNum");
//            assert docNumber != null;
            if (docNumber!=null && !docNumber.isEmpty()){
                transferType=getIntent().getStringExtra("transferType");
                try {
                    getStockRequestDetails(1,docNumber,transferType,"Print");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        stockRequestText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (requestAdapter !=null){
                    requestAdapter.getFilter().filter(s.toString());
                }
            }
        });

        getTransferdraftList(currentDate,currentDate);
        mode="In";

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // fromDate.setText(Utils.getDate(StockRequestListActivity.this,fromDate));
                getDate(fromDate);
            }
        });

        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toDate.setText(Utils.getDate(StockRequestListActivity.this,toDate));
                getDate(toDate);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!fromDate.getText().toString().isEmpty() && !toDate.getText().toString().isEmpty()){
                    String fromdate=Utils.convertDate(fromDate.getText().toString(),"dd-MM-yyyy","yyyyMMdd");
                    String todate=Utils.convertDate(toDate.getText().toString(),"dd-MM-yyyy","yyyyMMdd");
                    getTransferdraftList(fromdate,todate);
                }else {
                    Toast.makeText(getApplicationContext(),"Select the Date to Search..!",Toast.LENGTH_SHORT).show();
                }
            }

        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDate.setText(formattedDate);
                toDate.setText(formattedDate);
                searchFilterView.setVisibility(View.GONE);
                //  getTransferInRequest(transferMode, currentDate, currentDate);

            }

        });
    }

    public void getDate(TextView dateTextView){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(TransferDraftListActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }



    public void getTransferdraftList(String fromdate, String todate){
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Constants.BASEURL +"InventoryTransferDraftList";
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("User",username);
            jsonObject.put("FromDate",fromdate);
            jsonObject.put("ToDate",todate);
            jsonObject.put("DocStatus","");
            jsonObject.put("WarehouseCode" ,locationCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w("Given_url:",url+"---"+jsonObject.toString());

        if (dialog != null && dialog.isShowing())
            dialog.cancel();
        dialog=new ProgressDialog(TransferDraftListActivity.this);
        dialog.setMessage("Loading TransferDraft List...");
        dialog.setCancelable(false);
//        if (!dialog.isShowing()) {
        dialog.show();
//
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try {
                        dialog.dismiss();
                        Log.w("Response_StockRequest:", response.toString());
                        requestList =new ArrayList<>();

                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray transferDetailsArray =response.optJSONArray("responseData");
                            for (int i = 0; i< transferDetailsArray.length(); i++){
                                JSONObject object= transferDetailsArray.optJSONObject(i);
                                TransferModel model=new TransferModel();
                                model.setTransferNo(object.optString("docNum"));
                                model.setDate(object.optString("docDate"));
                                model.setFromLocation(object.optString("fromWarehouse"));
                                model.setToLocation(object.optString("toWarehouse"));
                                model.setUser(object.optString("user"));
                                model.setStatus(object.optString("docStatus"));
                                requestList.add(model);
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
                            if (requestList.size()>0){
                                stockRequestList.setVisibility(View.VISIBLE);
                                emptyText.setVisibility(View.GONE);
                                searchFilterView.setVisibility(View.GONE);
                                setTransferListAdapter(requestList);
                            }else {
                                stockRequestList.setVisibility(View.GONE);
                                emptyText.setVisibility(View.VISIBLE);
                                transferSize.setText("(0) Products") ;
                            }
                        }else {
                            stockRequestList.setVisibility(View.GONE);
                            emptyText.setVisibility(View.VISIBLE);
                            transferSize.setText("(0) Products") ;
                            Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                },
                error -> {
                    dialog.dismiss();
                    // Do something when error occurred
                    Log.w("Error_throwing:",error.toString());
                }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public void setTransferListAdapter(ArrayList<TransferModel> stockRequestList){
        //try {
            transferSize.setText("(" + stockRequestList.size() + ")" + " Products") ;

            requestAdapter = new TransferDraftListAdapter(this, stockRequestList, new TransferDraftListAdapter.CallBack() {
                @Override
                public void callDescription(String requestNo,String mode) {
                    Log.w("GivenTransferNo::", requestNo.toString());
                    if (mode.equals("Print")){
                        try {
                            getStockRequestDetails(1, requestNo.toString(),"Stock Request","Print");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Intent intent=new Intent(getApplicationContext(),TransferPreviewPrintActivity.class);
                        intent.putExtra("title","Stock Request");
                        intent.putExtra("transferNumber",requestNo);
                        startActivity(intent);
                    }
                }

                @Override
                public void convertTransfer(String requestId) {
                    try {
                        getStockRequestDetails(1, requestId.toString(),"Stock Request","");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            int mNoOfColumns = Utils.calculateNoOfColumns(getApplicationContext(),200);
            this.stockRequestList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            this.stockRequestList.setItemAnimator(new DefaultItemAnimator());
            this.stockRequestList.setAdapter(requestAdapter);
            //categoriesView.setVisibility(View.VISIBLE);
            //emptyLayout.setVisibility(View.GONE);
//        }catch (Exception ex){
//            Log.e("TAG","Error in Populating the data:"+ex.getMessage());
//        }
    }

    private void getStockRequestDetails(int copy, String transferNo, String type,String action) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("InvTransReqNo",transferNo);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url=Constants.BASEURL +"InventoryTransferRequestDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        if (action.equals("Print")){
            pDialog.setTitleText("Generating Print Preview...");
        }else {
            pDialog.setTitleText("Processing Please wait...");
        }
        pDialog.setCancelable(false);
        pDialog.show();
        transferDetailModels =new ArrayList<>();
        transferDetailsList =new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try{
                        Log.w("StockRequestDetails:",response.toString());

                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray  transferDetailsArray=response.optJSONArray("responseData");
                            assert transferDetailsArray != null;
                            JSONObject detailObject=transferDetailsArray.optJSONObject(0);
                            TransferDetailModel model=new TransferDetailModel();
                            model.setNumber(detailObject.optString("invTransReqNo"));
                            model.setStatus(detailObject.optString("invTransReqStatus"));
                            model.setDate(detailObject.optString("docDate"));
                            model.setFromLocation(detailObject.optString("fromWhsCode"));
                            model.setToLocation(detailObject.optString("toWhsCode"));
                            model.setFromLocationName(detailObject.optString("fromWarehouseName"));
                            model.setToLocationName(detailObject.optString("toWarehouseName"));

                            JSONArray itemsArray=detailObject.optJSONArray("itItem");
                            for (int i = 0; i< Objects.requireNonNull(itemsArray).length(); i++){
                                JSONObject objectItem= itemsArray.optJSONObject(i);
                                TransferDetailModel.TransferDetails transferModel =new TransferDetailModel.TransferDetails();
                                transferModel.setItemCode(objectItem.optString("itemCode"));
                                transferModel.setDescription(objectItem.optString("itemName"));
                                transferModel.setQty(objectItem.optString("qty"));
                                transferModel.setUomCode(objectItem.optString("uomCode"));
                                transferDetailsList.add(transferModel);
                            }
                            model.setTransferDetailsList(transferDetailsList);
                            transferDetailModels.add(model);

                            if (action.equals("Print")){
                                printTransfer(transferNo,transferDetailModels,type);
                            }else {
                                convertAndRedirect(transferNo,transferDetailModels,transferDetailsList);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            // pDialog.dismiss();
            Log.w("Error_throwing:",error.toString());
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
    public void convertAndRedirect(String requestNo,ArrayList<TransferDetailModel> transferDetailModels,ArrayList<TransferDetailModel.TransferDetails> transferDetail){
        String return_qty="0";
        String price_value ="0.00";
        String qty_value ="0";
        dbHelper.removeAllInvoiceItems();
        for (TransferDetailModel.TransferDetails model:transferDetail){
            dbHelper.insertCreateInvoiceCart(
                    model.getItemCode(),
                    model.getDescription(),
                    model.getUomCode(),
                    "",
                    (int)Double.parseDouble(model.getQty())+"",
                    return_qty,
                    (int)Double.parseDouble(model.getQty())+"", "0",
                    price_value,
                    "0",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00","",
                    "",
                    "",
                    "",
                    "0",
                    "","","","",""
            );
        }
        int count=dbHelper.numberOfRowsInInvoice();
        String fromLocation=transferDetailModels.get(0).getFromLocation();
        String toLocation=transferDetailModels.get(0).getToLocation();
        String invoiceDate=transferDetailModels.get(0).getDate();
        String transferType="Convert Transfer";
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

    public void printTransfer(String transferNo,ArrayList<TransferDetailModel> transferDetailModels,String type ){
        if (transferDetailModels.size()>0){
            if (Utils.validatePrinterConfiguration(this,printerType,printerMacId)) {

              //  TSCPrinter printer=new TSCPrinter(this,printerMacId,"Transfer");
//            try {
//                printer.printTransferDetail(1,transferNo,type,transferDetailModels);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.transfer_list_menu, menu);

        MenuItem action_add = menu.findItem(R.id.action_add);
        action_add.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//finish();
            onBackPressed();
        } else if (item.getItemId() == R.id.action_add) {
          //  transferType = transferMode;
            // Intent intent=new Intent(getApplicationContext(),TransferActivity.class);
            //intent.putExtra("transferType",transferType);
            // startActivity(intent);
            Intent intent=new Intent(getApplicationContext(), StockRequestAddActivity.class);
            intent.putExtra("requestType",requestMode);
            startActivity(intent);
        }else if (item.getItemId()==R.id.action_filter) {
            if (searchFilterView.getVisibility() == View.VISIBLE) {
                searchFilterView.setVisibility(View.GONE);
//                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
            } else {
                searchFilterView.setVisibility(View.VISIBLE);
//                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
            }
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void showPopupMenu(TransferModel transferModels ,View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.transfer_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.convert_transfer_menu) {

             Intent intent=new Intent(TransferDraftListActivity.this, ConvertTransferAddActivity.class);
             intent.putExtra("convertTranferNo",transferModels.transferNo);

             startActivity(intent);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}