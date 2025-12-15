package com.winapp.retailpos_sap.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winapp.retailpos_sap.R;
import com.winapp.retailpos_sap.ui.model.BatchDetailModule;
import com.winapp.retailpos_sap.ui.model.CreateInvoiceModel;
import com.winapp.retailpos_sap.ui.activity.GoodReceiptPreviewActivity;

import java.util.ArrayList;
import java.util.Objects;

public class GoodReceiptAddPreviewAdapter extends RecyclerView.Adapter<GoodReceiptAddPreviewAdapter.ViewHolder>{

    private ArrayList<CreateInvoiceModel> summaryList;
    public CallBack callBack;
    public Context context;
    public ArrayList<BatchDetailModule> batchList ;

    public GoodReceiptAddPreviewAdapter(Context context, ArrayList<CreateInvoiceModel> customers) {
        this.summaryList = customers;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.good_receipt_preview_items, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @SuppressLint("RecyclerView") int i) {
        try {
            batchList = new ArrayList<>() ;
            CreateInvoiceModel model= summaryList.get(i);
            viewHolder.productName.setText(model.getProductName().trim());
            viewHolder.productCode.setText(model.getProductCode());

            viewHolder.netQty.setText(String.valueOf(model.getNetQty()));
//            viewHolder.priceValue.setText(model.getPrice());
//            viewHolder.netTotalValue.setText(model.getNetTotal());
            viewHolder.uomtxt.setText(String.valueOf(model.getUomCode()));

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
        });

//            viewHolder.showHide_Img.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (viewHolder.showHide_Img.getTag().equals("hide")){
//                        viewHolder.batch_lay.setVisibility(View.VISIBLE);
//                        viewHolder.showHide_Img.setTag("show");
//                        viewHolder.showHide_Img.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
//                    }else {
//                        viewHolder.batch_lay.setVisibility(View.GONE);
//                        viewHolder.showHide_Img.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
//                        viewHolder.showHide_Img.setTag("hide");
//                    }
//                }
//            });
            getBatchList(viewHolder,i,model.getProductCode() , model.getUpdateTime());

        }catch (Exception ex){
            Log.w("Error_in_products:", Objects.requireNonNull(ex.getMessage()));
        }

    }
    public void setBatchAdapter(@NonNull RecyclerView.ViewHolder  viewHolder, int position,
                                ArrayList<BatchDetailModule> batchList){
        ((ViewHolder) viewHolder).rv_batchList.setHasFixedSize(true);
        ((ViewHolder) viewHolder).rv_batchList.setLayoutManager
                (new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        BatchReceiptPreviewAdapter adapter=new BatchReceiptPreviewAdapter(context,batchList);
        ((ViewHolder) viewHolder).rv_batchList.setAdapter(adapter);
        // notifyDataSetChanged();
    }

    public void getBatchList(ViewHolder viewHolder, int position,String pdtCode , String timeStamp){
        batchList = GoodReceiptPreviewActivity.dbHelper.getBatchProducts(pdtCode, timeStamp);
        if(batchList != null && batchList.size() > 0){
            viewHolder.batch_lay.setVisibility(View.VISIBLE);
         //   viewHolder.batchList_empty.setVisibility(View.GONE);
            setBatchAdapter(viewHolder,position,batchList);
        }else{
            viewHolder.batch_lay.setVisibility(View.GONE);
           // viewHolder.batchList_empty.setVisibility(View.VISIBLE);
        }
    }

    public int getQty(String qty){
        double val=Double.parseDouble(qty);
        return (int)val;
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView productName;
        private TextView productCode;
        private TextView netQty , batchList_empty;
        private TextView netTotalValue, uomtxt;
        private ImageView showHide_Img;
        private LinearLayout batch_lay;
        private RecyclerView rv_batchList;
        public ViewHolder(View view) {
            super(view);
             productCode = view.findViewById(R.id.item_code_prev);
            productName =view.findViewById(R.id.product_name_prev);
            netQty=view.findViewById(R.id.net_qty_prev);
            uomtxt=view.findViewById(R.id.uom_item_prev);
            showHide_Img=view.findViewById(R.id.show_hide_prev);
            batch_lay=view.findViewById(R.id.batch_lay_prev);
            rv_batchList=view.findViewById(R.id.rv_batchList_prev);
            batchList_empty=view.findViewById(R.id.empty_batch_Pre);
        }

    }

    public interface CallBack{
        void searchCustomer(String letter, int pos);
        void removeItem(String pid,String updateTime);
        void editItem(CreateInvoiceModel model);
    }
    public ArrayList<CreateInvoiceModel> getList(){
        return summaryList;
    }

    public void filterList(ArrayList<CreateInvoiceModel> filterdNames) {
        this.summaryList = filterdNames;
        notifyDataSetChanged();
    }
}