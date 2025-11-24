package com.winapp.retailpos_sap.ui.newtransfer

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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.winapp.retailpos_sap.CommonMethods
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.newtransfer.TransferInModel.TransferInDetails

class TransferInAdapter(
    private val context: Context,
    var transferInlist: ArrayList<TransferInDetails>,
    var transferMode: String
) : RecyclerView.Adapter<TransferInAdapter.MyViewHolder>() {
    lateinit var selectedModel: TransferInDetails
    var istrue: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.transfer_in_item, parent, false)
        )


    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(transferInlist[position], transferMode)
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
        var qtytxt: EditText

        init {
            val pos = getAdapterPosition()
            pdtnametxt = itemView.findViewById(R.id.pdtname_transfer_item)
            pdtcodetxt = itemView.findViewById(R.id.pdtcode_transfer_item)
            qtytxt = itemView.findViewById(R.id.qty_transfer_item)
            stocktxt = itemView.findViewById(R.id.stock_transfer_item)
            transferinlay = itemView.findViewById(R.id.transferin_lay)
            transferinlay1 = itemView.findViewById(R.id.transferin_lay1)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setData(transferInItem: TransferInDetails, transferMode: String) {
            pdtnametxt.text = transferInItem.productName
            pdtcodetxt.text = transferInItem.productCode
            qtytxt.setText(transferInItem.qty.toString())
            stocktxt.text = transferInItem.stockInHand.toString()
            Log.w("stockinHand", "" + transferInItem.stockInHand)

            if (::selectedModel.isInitialized
                && selectedModel.productCode == transferInItem.productCode
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
            qtytxt.removeTextChangedListener(textWatcher)
            qtytxt.setSelection(qtytxt.getText().length)
            qtytxt.setSelectAllOnFocus(true)
            qtytxt.addTextChangedListener(object : TextWatcher {
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
                        Log.w("stockinHand11", "" + transferInlist[pos].stockInHand)
                        if (!s.toString().isEmpty()) {
                            if (transferMode == "Stock Request") {
                                transferInlist[pos].qty = s.toString()
                                //  notifyDataSetChanged();
                            } else {
                                if (transferInlist[pos].stockInHand >= s.toString().toInt()) {
                                    transferInlist[pos].qty = s.toString()
//                                    notifyItemChanged(pos)
                                } else {
                                    //  qtytxt.setText("");
                                    //  Toast.makeText(context, "Low stock !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            transferInlist[pos].qty = ""
                        }
                    }
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateQty(pdtName: TransferInDetails, istrueVal: Boolean) {
        selectedModel = pdtName
        istrue = istrueVal
        notifyDataSetChanged()
    }

    fun updateList(list: ArrayList<TransferInDetails>) {
        transferInlist = list
        notifyDataSetChanged()
    }

    interface PickListInvoiceClickListener {
        fun pickListInvoiceSelected(position: Int?)
    }
}
