package com.winapp.retailpos_sap.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.winapp.retailpos_sap.R;
import com.winapp.retailpos_sap.ui.activity.StockRequestListActivity;
import com.winapp.retailpos_sap.ui.model.TransferDetailModel;
import com.winapp.retailpos_sap.ui.model.TransferModel;
import com.winapp.retailpos_sap.ui.utils.Constants;
import com.winapp.retailpos_sap.ui.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StockRequestListAdapter extends RecyclerView.Adapter<StockRequestListAdapter.TransferViewHolder> implements Filterable {
    /**
     * Declare the Context and Arraylist variables
     */
    private Context mContext;
    private ArrayList<TransferModel> transferList;
    private ArrayList<TransferModel> transferListFilter;
    private SharedPreferences sharedpreferences;
    private int mContainerId;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private final static String TAG = "DashBoardActivity";
    private CallBack callBack;
    private SessionManager session;
    private String locationCode;
    private HashMap<String,String> user;
     public ConvertClickListener convertClickListener ;
    private ArrayList<TransferDetailModel> transferDetailModels;
    private ArrayList<TransferDetailModel.TransferDetails> transferDetailsList;
    private StockReqListDetailAdapter adapter;
    public static class TransferViewHolder extends RecyclerView.ViewHolder {
        // Declare the Required Variables
        private ImageView thumbnail;
      //  private final ProgressBar progressBar;
        private TextView transferNo;
        private TextView date;
        private TextView tolocation;
        private TextView fromlocation;
        private TextView user;
        private ImageView status;
        private ImageView print;
        private ImageView printPreview ,req_img_down ;
        private ImageView convertTransfer , threedot_reql;
        private TextView snNo ;
        private LinearLayout emptyLay  ,stockDetailLayl ;
        private CardView bottom_lay;
        private RecyclerView rv_stockReqList ;
        LinearLayout statusLayout;

        public TransferViewHolder(View view) {
            super(view);
            transferNo = view.findViewById(R.id.transfer_no);
            date = view.findViewById(R.id.date);
            tolocation = view.findViewById(R.id.to_location);
            fromlocation=view.findViewById(R.id.from_location);
            user = view.findViewById(R.id.user);
            status = view.findViewById(R.id.status);
            print=view.findViewById(R.id.print);
            snNo=view.findViewById(R.id.sn_no);
            thumbnail = view.findViewById(R.id.more_option);
            printPreview=view.findViewById(R.id.print_preview);
            convertTransfer=view.findViewById(R.id.convert);
            threedot_reql=view.findViewById(R.id.three_dot_req);
            req_img_down=view.findViewById(R.id.req_img_down);
            rv_stockReqList=view.findViewById(R.id.stockReq_detailList);
            emptyLay=view.findViewById(R.id.progress_layout);
            bottom_lay=view.findViewById(R.id.bottom_layouta);
            stockDetailLayl=view.findViewById(R.id.stockDetailLay);
           // progressBar = view.findViewById(R.id.progressBar);
           // statusLayout = view.findViewById(R.id.status_layout);
        }
    }

    /**
     * Constructor for the send the arraylist and context
     *
     * @param mContext
     * @param transferList
     */
    public StockRequestListAdapter(Context mContext, ArrayList<TransferModel> transferList, ConvertClickListener convertClickListener
    ,CallBack callBack ) {
        this.mContext = mContext;
        this.transferList = transferList;
        this.callBack = callBack;
        this.convertClickListener = convertClickListener;
        this.transferListFilter = new ArrayList<>(transferList);
        session=new SessionManager(mContext);
        user=session.getUserDetails();
    }

    /**
     * @param parent
     * @param viewType
     * @return
     */

    @Override
    public TransferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stockreq_list_item, parent, false);
        return new TransferViewHolder(itemView);
    }

    /**
     * @param holder
     * @param position
     */

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final TransferViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        try {
            final TransferModel model = transferList.get(position);
            int sn=position+1;
            holder.snNo.setText(sn+"");
            holder.transferNo.setText(model.getTransferNo());
            holder.date.setText(model.getDate());
            holder.tolocation.setText(model.getToLocation());
            holder.fromlocation.setText(model.getFromLocation());
            holder.user.setText(model.getUser());

            if (mContext instanceof StockRequestListActivity){
                locationCode=user.get(SessionManager.KEY_LOCATION_CODE);
                assert locationCode != null;
                if (locationCode.equalsIgnoreCase(model.fromLocation)){
                    holder.convertTransfer.setVisibility(View.GONE);
                }else {
                    holder.convertTransfer.setVisibility(View.GONE);
                }
            }else {
              //  holder.status.setVisibility(View.VISIBLE);
                holder.convertTransfer.setVisibility(View.GONE);
            }
            Log.w("StatusValue:",model.getStatus()+"");
            if (model.getStatus().equals("Open") || model.getStatus().equals("O")){
                holder.status.setImageResource(R.drawable.open);
            }else if (model.getStatus().equals("C") || model.getStatus().equals("Closed")){
                holder.status.setImageResource(R.drawable.closed);
            }else {
                holder.status.setImageResource(R.drawable.hold);
            }

          //  holder.status.setText(model.getStatus());
            holder.req_img_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (  holder.req_img_down.getTag().equals("hide")){
                          holder.bottom_lay.setVisibility(View.VISIBLE);
                          holder.req_img_down.setTag("show");
                          holder.req_img_down.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_keyboard_arrow_up_24));

                            if (model.getTransferDetailsList() != null && model.getTransferDetailsList().size()>0){
                                setAdapter(holder,position,model.getTransferDetailsList());
                                  holder.emptyLay.setVisibility(View.GONE);
                                  holder.bottom_lay.setVisibility(View.VISIBLE);
                            }else {
                                try {
                                    getStockRequestDetails(model.transferNo,holder,model,position);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                model.setShow(true);
                            }
                    }else {
                        model.setShow(false);
                          holder.bottom_lay.setVisibility(View.GONE);
                          holder.req_img_down.setTag("hide");
                          holder.req_img_down.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_keyboard_arrow_down_24));
                    }
                }
            });
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            holder.threedot_reql.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    convertClickListener.convertSelected(model,view);
                }
            });
            holder.print.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.callDescription(model.getTransferNo(),"Print");
                }
            });

            holder.printPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.callDescription(model.getTransferNo(),"Preview");
                }
            });

            holder.convertTransfer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (model.getStatus().equals("O") || model.getStatus().equals("Open")){
                        callBack.convertTransfer(model.getTransferNo().toString());
                    }else {
                        Toast.makeText(mContext,"Request Already Closed...!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception ex) {
            Log.e("Error_in_adapter:", Objects.requireNonNull(ex.getMessage()));
        }
    }

    /**
     * @return array list count
     */
    @Override
    public int getItemCount() {
        return transferList.size();
    }

    public interface CallBack {
        void callDescription(String transferId,String mode);
        void convertTransfer(String requestId);
    }
    public interface ConvertClickListener {
        void convertSelected(TransferModel transferModels, View view);
    }

    public Bitmap getImage(String base64String) {
        String base64Image = base64String.split(",")[1];
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public void filterList(ArrayList<TransferModel> filterdNames) {
        this.transferList = filterdNames;
        notifyDataSetChanged();
    }

    private void getStockRequestDetails(String transferNo,RecyclerView.ViewHolder  viewHolder,
                                        TransferModel transferModel1, int position) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("InvTransReqNo",transferNo);
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        String url= Constants.BASEURL +"InventoryTransferRequestDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url+jsonBody);
//        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//        pDialog.setTitleText("Generating Print Preview...");
//        pDialog.setCancelable(false);
//        pDialog.show();
        transferDetailModels =new ArrayList<>();
        transferDetailsList =new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try{
                        Log.w("TransferDetail:",response.toString());

                       // pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray transferDetailsArray=response.optJSONArray("responseData");
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
                                transferModel.setDescription(objectItem.optString("itemName"));
                                transferModel.setQty(objectItem.optString("qty"));
                                transferModel.setUomCode(objectItem.optString("uomCode"));
                                transferDetailsList.add(transferModel);
                            }
                            model.setTransferDetailsList(transferDetailsList);
                            transferDetailModels.add(model);

                            // printTransfer(transferNo,transferDetailModels,type);
                            if (transferDetailsList.size() > 0) {
                                setAdapter(viewHolder,position,transferDetailsList);
                                ((TransferViewHolder) viewHolder).emptyLay.setVisibility(View.GONE);
                                ((TransferViewHolder) viewHolder).rv_stockReqList.setVisibility(View.VISIBLE);
                                ((TransferViewHolder) viewHolder).bottom_lay.setVisibility(View.VISIBLE);
                                transferModel1.setTransferDetailsList(transferDetailsList);
                                //   transfertype.setText(type);
                            }else{
                                ((TransferViewHolder) viewHolder).emptyLay.setVisibility(View.VISIBLE);
                                ((TransferViewHolder) viewHolder).rv_stockReqList.setVisibility(View.GONE);
                                ((TransferViewHolder) viewHolder).bottom_lay.setVisibility(View.GONE);
                            }
                        }else {
                            ((TransferViewHolder) viewHolder).emptyLay.setVisibility(View.VISIBLE);
                            ((TransferViewHolder) viewHolder).rv_stockReqList.setVisibility(View.GONE);
                            ((TransferViewHolder) viewHolder).bottom_lay.setVisibility(View.GONE);
                            Toast.makeText(mContext,statusMessage,Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
          //  pDialog.dismiss();
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
    public void setAdapter(@NonNull RecyclerView.ViewHolder  viewHolder, int position,
                           ArrayList<TransferDetailModel.TransferDetails> stockReqList){

//            try {
//            for (TransferDetailModel model : transferDetailModels) {
//                transferno.setText(transferNo);
//                transferdate.setText(model.getDate());
//                from_locat.setText(model.getFromLocation());
//                to_locat.setText(model.getToLocation());
//                Log.w("tran_toLoc",""+model.getToLocationName());
//                toloc_namel.setText(model.getToLocationName());
//                fromloc_namel.setText(model.getFromLocationName());
//            }
        ((TransferViewHolder) viewHolder).rv_stockReqList.setHasFixedSize(true);
        ((TransferViewHolder) viewHolder).rv_stockReqList.setLayoutManager(new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false));
            adapter = new StockReqListDetailAdapter(mContext, stockReqList, "");
        ((TransferViewHolder) viewHolder).rv_stockReqList.setAdapter(adapter);
           // mainLayout.setVisibility(View.VISIBLE);
       // }catch (Exception exception){}
    }
    @Override
    public Filter getFilter() {
        return TransferFilter;
    }
    private Filter TransferFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<TransferModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(transferListFilter);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (TransferModel item : transferListFilter) {
                    if (item.transferNo.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            transferList.clear();
            transferList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}