package com.winapp.retailpos_sap.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.winapp.retailpos_sap.R
import com.winapp.retailpos_sap.ui.adapter.GoodReceiptAddPreviewAdapter
import com.winapp.retailpos_sap.ui.db.DBHelper
import com.winapp.retailpos_sap.ui.model.CreateInvoiceModel
import com.winapp.retailpos_sap.ui.utils.SessionManager
import com.winapp.retailpos_sap.ui.utils.SharedPreferenceUtil
import com.winapp.retailpos_sap.ui.activity.GoodReceiptProductAddActivity.Companion.productSummaryAdapter

class GoodReceiptPreviewActivity  : AppCompatActivity(){

    var user: HashMap<String, String>? = null
    var session: SessionManager? = null

    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    var productSummaryView: RecyclerView? = null
    var emptyTxt: TextView? = null
    var size_txt: TextView? = null
    private var productAdapter: GoodReceiptAddPreviewAdapter? = null
    private val productSummaryList: ArrayList<CreateInvoiceModel>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_stock_receipt_add_preview)

    supportActionBar!!.title = "Good Receipt Preview"
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    Log.w("activity_cg", javaClass.simpleName.toString())

    session = SessionManager(this)
    user = session!!.userDetails
    dbHelper = DBHelper(this)
    sharedPreferenceUtil = SharedPreferenceUtil(this)

    productSummaryView = findViewById(R.id.rv_pdtSummaryList)
    emptyTxt = findViewById(R.id.empty_textPre)
    size_txt = findViewById(R.id.count_receipt_prev)

        if(productSummaryAdapter != null &&  productSummaryAdapter!!.getList().size > 0){
            setPdtAdapter(productSummaryAdapter!!.getList())

            emptyTxt!!.visibility = View.GONE
            productSummaryView!!.visibility = View.VISIBLE
        }else{
            emptyTxt!!.visibility = View.VISIBLE
            productSummaryView!!.visibility = View.GONE
        }

    }
    fun setPdtAdapter(productLists: ArrayList<CreateInvoiceModel>) {
        size_txt!!.setText("Products ("+productLists.size+") ")
        productSummaryView!!.setHasFixedSize(true)
        productSummaryView!!.setLayoutManager(
            LinearLayoutManager(
                this@GoodReceiptPreviewActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        productAdapter =
            GoodReceiptAddPreviewAdapter(
                this@GoodReceiptPreviewActivity,
                productLists
            )
        productSummaryView!!.setAdapter(productAdapter)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    companion object {
        lateinit var dbHelper: DBHelper
    }
}