package com.test.webview

import android.webkit.JavascriptInterface
import org.json.JSONObject

abstract class BaseBridge {
    @JavascriptInterface
    fun callAndroidFunc(action: String, jsonString: String): String {
        return dispatchAction(action, JSONObject(jsonString)).toString()
    }

    abstract fun dispatchAction(action: String, param: JSONObject): JSONObject
}