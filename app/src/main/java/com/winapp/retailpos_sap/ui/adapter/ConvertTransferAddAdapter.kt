package com.winapp.retailpos_sap.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.winapp.retailpos_sap.CommonMethods
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.model.TransferDetailModel

class ConvertTransferAddAdapter(
    private val context: Context,
    var transferInlist: ArrayList<TransferDetailModel.TransferDetails>)
    : RecyclerView.Adapter<ConvertTransferAddAdapter.MyViewHolder>() {
    lateinit var selectedModel: TransferDetailModel.TransferDetails
    var istrue: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.convert_transf_add_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(transferInlist[position])
    }

    override fun getItemCount(): Int {
        return transferInlist.size
    }

    inner class MyViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var pdtnametxt: TextView
        var pdtcodetxt: TextView
        var stocktxt: TextView
        var transferinlay: CardView
        var transferinlay1: LinearLayout
        var textWatcher: TextWatcher? = null
        var receivQtytxt: TextView
        var sentQtytxt: EditText

        init {
            val pos = getAdapterPosition()
            pdtnametxt = itemView.findViewById(R.id.pdtname_transfer_item)
            pdtcodetxt = itemView.findViewById(R.id.pdtcode_transfer_item)
            sentQtytxt = itemView.findViewById(R.id.transSent_qty_item)
            receivQtytxt = itemView.findViewById(R.id.transReceiv_qty_item)
            stocktxt = itemView.findViewById(R.id.stock_transfer_item)
            transferinlay = itemView.findViewById(R.id.transferin_lay)
            transferinlay1 = itemView.findViewById(R.id.transferin_lay1)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setData(transferInItem: TransferDetailModel.TransferDetails) {
            pdtnametxt.text = transferInItem.description
            pdtcodetxt.text = transferInItem.itemCode
            receivQtytxt.setText(transferInItem.qty.toString())
            sentQtytxt.setText(transferInItem.sentQty.toString())
            stocktxt.text = transferInItem.stock.toString()
            Log.w("stockinHand", "" + transferInItem.stock)

            if (::selectedModel.isInitialized
                && selectedModel.itemCode == transferInItem.itemCode
//                && selectedModel.location == pickItem.location
                && istrue
            ) {
                Log.e("graadd_entry", "")

                object : CountDownTimer(1000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        //  CommonMethods.setBlinkingText(pdtnametxt)
                        CommonMethods.setBlinkingLay(transferinlay1)
                        //    CommonMethods.setBlinkingText(qtyTextviewl)
                    }

                    override fun onFinish() {
                        transferinlay1.clearAnimation()
                        istrue = false
                        //      qtyTextviewl.clearAnimation()
                    }
                }.start()
            } else {
                Log.e("graadd_enss", "")
                transferinlay1.clearAnimation()
            }
            sentQtytxt.removeTextChangedListener(textWatcher)
            sentQtytxt.setSelection(sentQtytxt.getText().length)
            sentQtytxt.setSelectAllOnFocus(true)

            sentQtytxt.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    val pos = getAdapterPosition()
                    if (pos != -1) {
                        Log.w("editabl_s", "" + s.toString())
                        Log.w("stockinHand11", "" + transferInlist[pos].stock)
                        if (!s.toString().isEmpty()) {
//                            if (transferMode == "Stock Request") {
 //                               transferInlist[pos].qty = s.toString()
                                //  notifyDataSetChanged();
//                            } else {
                               // if (transferInlist[pos].stock >= s.toString().toInt()) {
                                    transferInlist[pos].sentQty = s.toString()
//                                    notifyItemChanged(pos)
//                                } else {
//                                    sentQtytxt.setText("")
//                                    Toast.makeText(context, "Low stock !", Toast.LENGTH_SHORT)
//                                        .show()
//                                }
//                            }
                        } else {
                            transferInlist[pos].sentQty = ""
                        }
                    }
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateQty(pdtName: TransferDetailModel.TransferDetails, istrueVal: Boolean) {
        selectedModel = pdtName
        istrue = istrueVal
        notifyDataSetChanged()
    }

    fun updateList(list: ArrayList<TransferDetailModel.TransferDetails>) {
        transferInlist = list
        notifyDataSetChanged()
    }

    interface PickListInvoiceClickListener {
        fun pickListInvoiceSelected(position: Int?)
    }
}
