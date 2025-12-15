package com.winapp.retailpos_sap.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.model.BatchDetailModule


class BatchReceiptPreviewAdapter(
    private val context: Context,
    private var dataList: ArrayList<BatchDetailModule>,

    ) : RecyclerView.Adapter<BatchReceiptPreviewAdapter.ViewHolder>() {
    var isRemove: Boolean = false
    private var batchqtyTextWatcher: TextWatcher? = null
    private var batchNoTextWatcher: TextWatcher? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.batch_receipt_prev_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, @SuppressLint("RecyclerView") i: Int) {

        val model = dataList[i]

        viewHolder.batchNotxt.setText(model.batchNo)
        viewHolder.batchQtytxt.setText(model.batchQty.toString())
        viewHolder.sno_prev.setText((i + 1).toString())

        Log.e("remov", ",," + model.isRemove + model.batchQty)

    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    override fun getItemViewType(position: Int): Int {
         return position;
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    public fun getBatchDataList(): ArrayList<BatchDetailModule> {
        return dataList
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val batchNotxt: TextView
        val batchQtytxt: TextView
        val sno_prev: TextView
        init {
            sno_prev = view.findViewById(R.id.sno_prev)
            batchNotxt = view.findViewById(R.id.batchNo_item_prev)
            batchQtytxt = view.findViewById(R.id.batchQty_item_prev)
        }
    }

    fun updateList(arrayList: ArrayList<BatchDetailModule>) {
        dataList = arrayList
        notifyDataSetChanged()
    }

    fun listAdd(isRemovel: Boolean, arrayList: ArrayList<BatchDetailModule>) {
        isRemove = isRemovel
        dataList = arrayList
        notifyDataSetChanged()
    }


}