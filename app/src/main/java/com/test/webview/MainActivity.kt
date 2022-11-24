package com.test.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.lzyzsd.jsbridge.BridgeWebView
import org.json.JSONObject

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val dataProvider = object : DataProvider {
        override fun getUserName(id: Int): String {
            return "LiLei_$id"
        }
    }
    private val bridgeHelper by lazy { NativeHelper(findViewById(R.id.wv), dataProvider) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        findViewById<Button>(R.id.btn_back).setOnClickListener { webView.goBack() }
 */
        findViewById<Button>(R.id.btn_test).setOnClickListener {
            bridgeHelper.getIdFromJS { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        bridgeHelper.initBridge()
    }


    interface DataProvider {
        fun getUserName(id: Int): String
    }

    /**
     * @Description
     * @Author Naruto Yang
     * @CreateDate 2022/11/23 0023
     * @Note
     */
    abstract class Helper(protected val dataProvider: DataProvider) : BaseBridge() {
        abstract fun initBridge()
        abstract fun getIdFromJS(callback: (String) -> Unit)
        override fun dispatchAction(action: String, param: JSONObject): JSONObject {
            val response = JSONObject()
            when (action) {
                "getUserName" -> {
                    val name = dataProvider.getUserName(param.getInt("id"))
                    response.put("name", name)
                }
            }
            return response
        }
    }

    /**
     * @Description
     * @Author Naruto Yang
     * @CreateDate 2022/11/23 0023
     * @Note
     */
    class JsBridgeHelper(private val webView: BridgeWebView, dataProvider: DataProvider) :
        Helper(dataProvider) {
        override fun getIdFromJS(callback: (String) -> Unit) {
            val json = JSONObject().apply { put("action", "getId") }
            webView.send(json.toString()) { callback(it) }
        }

        @SuppressLint("SetJavaScriptEnabled")
        override fun initBridge() {
            webView.loadUrl("file:///android_asset/jsBridge.html")
            webView.settings.javaScriptEnabled = true
            webView.setDefaultHandler { data, function ->
                val json = JSONObject(data)
                val action = json.getString("action")
                function.onCallBack(dispatchAction(action, json).toString())
            }
        }
    }


    /**
     * @Description
     * @Author Naruto Yang
     * @CreateDate 2022/11/23 0023
     * @Note
     */
    class NativeHelper(private val webView: WebView, dataProvider: DataProvider) :
        Helper(dataProvider) {
        @SuppressLint("SetJavaScriptEnabled")
        override fun initBridge() {
            webView.loadUrl("file:///android_asset/native.html")
            webView.settings.javaScriptEnabled = true
            webView.addJavascriptInterface(this, "bridge")
        }

        override fun getIdFromJS(callback: (String) -> Unit) {
            callJsFunc("getId()", callback)
        }

        fun callJsFunc(script: String, callback: (String) -> Unit) {
            webView.evaluateJavascript(script, callback)
        }
    }
}

