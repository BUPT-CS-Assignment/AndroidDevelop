package com.labx.scanimal.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

class SearchAPI(context: Context) {

    companion object {
        const val TAG = "SearchAPI"
        const val SEARCH_API_URL =
            "https://aip.baidubce.com/rest/2.0/image-classify/v1/animal"
        const val TOKEN_GET_URL =
            "https://aip.baidubce.com/oauth/2.0/token"
        const val SEARCH_API_KEY = "rppIiSCxnLR8WVvBr1N3hMAb"
        const val SEARCH_SEC_KEY = "O95Xmo3EGsnZgvyY4bqhSygXlQLa0OGi"
    }

    val sharedPreferences = context.getSharedPreferences("ScAnimal", Context.MODE_PRIVATE)
    private val requestQueue = Volley.newRequestQueue(context)

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
        val byteArray: ByteArray = byteStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun checkToken() {
        Log.d(TAG,"checking token")
        val token: String = sharedPreferences.getString("token", "")?: ""
        if (token == "") {
            updateToken()
        }
    }

    fun updateToken(){
        Log.d(TAG,"updating token")
        requestQueue.add(object :
            StringRequest(
                Method.POST,
                "${TOKEN_GET_URL}?grant_type=client_credentials&client_id=${SEARCH_API_KEY}&client_secret=${SEARCH_SEC_KEY}",
                { response ->
                    Log.d(TAG,"got response")
                    val json = JSONObject(response.toString())
                    val token = json.getString("access_token")?: ""
                    if(token != ""){
                        Log.d(TAG,"set new token")
                        val editor = sharedPreferences.edit()
                        editor.putString("token", token)
                        editor.apply()
                    }
                },
                { error -> error.printStackTrace()}
            ){}
        )
    }

    fun searchImage(image: Bitmap): Task<List<ObjectSearchResult>> {
        val apiSource = TaskCompletionSource<List<ObjectSearchResult>>()
        val apiTask = apiSource.task

        var base64: String = bitmapToBase64(image)
        var base64Url = URLEncoder.encode(base64, "UTF-8")

        val token: String = sharedPreferences.getString("token", "")?: ""
        Log.d(TAG,"Sending search request")
        val req = "image=${base64Url}&top_num=3&baike_num=3"

        requestQueue.add(object :
            JsonObjectRequest(
                Method.POST,
                "$SEARCH_API_URL?access_token=${token}",
                null,
                { response ->
                    Log.d(TAG,"got search response")
                    if(response.has("error_code")){
                        val errorResponse = apiErrorToObject(response)
                        Log.d(TAG,"error code: ${errorResponse.error_code}")
                        apiSource.setException(Exception(errorResponse.error_msg))
                        if(errorResponse.error_code == 110){
                            updateToken()
                        }
                    } else {
                        val searchList = apiResponseToObject(response)
                        Log.d(TAG,"search results sized ${searchList.size}")
                        apiSource.setResult(searchList)
                    }
                },
                { error ->
                    Log.d(TAG,"HTTP request error: ${error.message}")
                    apiSource.setException(error)
                }
            ) {
            override fun getBodyContentType() = "application/x-www-form-urlencoded"
            override fun getBody(): ByteArray {
                return req.toByteArray()
            }
        }.apply {
            setShouldCache(false)
        })

        return apiTask
    }

    @Throws(JsonSyntaxException::class)
    private fun apiResponseToObject(response: JSONObject): List<ObjectSearchResult> {
        val objectSearchResults = mutableListOf<ObjectSearchResult>()
        val searchResult =
            Gson().fromJson(response.toString(), SearchResultResponse::class.java)

        searchResult.result.forEach {
            objectSearchResults.add(
                ObjectSearchResult(
                    it.name,
                    it.score,
                    it.baike_info?.baike_url,
                    it.baike_info?.description
                )
            )
        }
        return objectSearchResults
    }

    @Throws(JsonSyntaxException::class)
    private fun apiErrorToObject(response: JSONObject): ErrorResponse {
        val errorResponse =
            Gson().fromJson(response.toString(), ErrorResponse::class.java)
        return errorResponse
    }
}