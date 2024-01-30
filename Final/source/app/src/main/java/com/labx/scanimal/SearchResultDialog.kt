package com.labx.scanimal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.labx.scanimal.databinding.ResultSearchBinding
import com.labx.scanimal.api.ObjectSearchResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class SearchResultDialog(context_: Context) {

    private val viewbind = ResultSearchBinding.inflate(LayoutInflater.from(context_))
    private var bottom_sheet = BottomSheetDialog(context_)
    private val context = context_
    private var results: List<ObjectSearchResult> = emptyList()

    constructor(context: Context, bitmap: Bitmap, input: List<ObjectSearchResult>) : this(context) {
        bottom_sheet = BottomSheetDialog(context)
        bottom_sheet.setContentView(viewbind.root)
        bottom_sheet.setCanceledOnTouchOutside(true)
        bitmap?.let {
            viewbind.llImage.setImageBitmap(bitmap)
        }
        bottom_sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottom_sheet.behavior.setBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    bottom_sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            }
        )
        bottom_sheet.behavior.maxHeight = getDisplayHeight() * 7 / 8
        viewbind.topBar.setOnClickListener {
            bottom_sheet.dismiss()
        }

        results = input
        if(results.size > 0) {
            setTextView(viewbind.title,0)
        }
        if(results.size > 1) {
            viewbind.guide.visibility = View.VISIBLE
            setTextView(viewbind.secTitle,1)
        }
        if(results.size > 2) {
            setTextView(viewbind.trdTitle,2)
        }
    }

    private fun setTextView(textView: TextView,index: Int){
        textView.setText(results[index].name)
        textView.visibility = View.VISIBLE
        textView.setOnClickListener {
            setWebView(index)
            setTitleStyle(textView)
            for(i in 0 until results.size){
                when(i){
                    index -> continue
                    0 -> setSecondStyle(viewbind.title)
                    1 -> setSecondStyle(viewbind.secTitle)
                    2 -> setSecondStyle(viewbind.trdTitle)
                }
            }
        }
    }

    private fun setTitleStyle(textView: TextView) {
        textView?.let {
            it.setTypeface(null,Typeface.BOLD)
            it.setTextColor(ContextCompat.getColor(context,R.color.outline))
            it.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25F)
        }
    }

    private fun setSecondStyle(textView: TextView) {
        textView?.let {
            it.setTypeface(null,Typeface.NORMAL)
            it.setTextColor(ContextCompat.getColor(context,R.color.primary))
            it.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
        }
    }


    private fun setWebView(index: Int) {
        if(index >= results.size)
            return
        val webView = viewbind.wvResult
        webView.loadUrl(results[index].url?:"")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
    }

    fun show(){
        setTextView(viewbind.title,0)
        setWebView(0)
        bottom_sheet.show()
    }

    private fun getDisplayHeight() : Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }
}