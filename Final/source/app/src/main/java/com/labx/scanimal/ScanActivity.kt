package com.labx.scanimal

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.labx.scanimal.api.ObjectSearchResult
import com.labx.scanimal.api.SearchAPI
import com.labx.scanimal.databinding.ActivityScanBinding
import com.xiasuhuei321.loadingdialog.view.LoadingDialog
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


data class SearchResult(
    var bitmap:Bitmap,
    var detectedObject: DetectedObject,
    var data: List<ObjectSearchResult>?,
    var dialog: SearchResultDialog?
)

class ScanActivity : AppCompatActivity() {
    companion object{
        const val TAG = "ScanActivity"
        const val D_TAG = "Detector"
        private const val REQUEST_IMAGE_CAPTURE = 1000
        private const val REQUEST_IMAGE_GALLERY = 1001
    }

    private lateinit var viewBinding: ActivityScanBinding
    private lateinit var searchApi: SearchAPI
    private lateinit var waitingDialog: WaitingDialog
    private var cameraPhotoUri: Uri? = null
    private var detectResultNum: Int = 0
    private var searchedResultNum: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"created")
        searchApi = SearchAPI(this)
        searchApi.checkToken()
        viewBinding = ActivityScanBinding.inflate(layoutInflater)
        waitingDialog = WaitingDialog(this)

        setContentView(viewBinding.root)
        with(viewBinding) {
            ivCapture.setOnClickListener{ dispathCaptureIntent() }
            ivGallery.setOnClickListener { pictureFromGallery() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle the image pick result
        if(resultCode == RESULT_OK){
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> cameraPhotoUri?.let {
                    this.setViewAndDetect(
                        getBitmapFromUri(it)
                    )
                }
                REQUEST_IMAGE_GALLERY -> data?.data?.let{
                    this.setViewAndDetect(getBitmapFromUri(it))
                }
            }
        }
    }


    private fun setViewAndDetect(bitmap: Bitmap?){
        detectResultNum = 0
        searchedResultNum = 0
        viewBinding.ivPreview.clearResults()

        bitmap?.let {
            viewBinding.ivPreview.setImageBitmap(bitmap)
            waitingDialog.setLoadingText("搜索ing...").show()
            Thread {
                Log.d(D_TAG,"create detector")
                val image = InputImage.fromBitmap(bitmap, 0)
                val options = ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .build()
                val objectDetector = ObjectDetection.getClient(options)
                Log.d(D_TAG,"start detecting")
                objectDetector.process(image)
                    .addOnSuccessListener { results ->
                        Thread{
                            Log.d(D_TAG,"detected ${results.size} objects")
                            val filterResults = results.filter { result ->
                                result.labels.size == 0
                            }
                            detectResultNum = filterResults.size
                            Log.d(TAG,"filter with ${detectResultNum} objects")
                            if(detectResultNum >= 6) {
                                Log.w(D_TAG,"too many objects")
                                runOnUiThread{
                                    waitingDialog.setFailedText("物体太多啦").loadFailed()
                                }

                            }else{
                                if(filterResults.size == 0) {
                                    runOnUiThread{
                                        waitingDialog.setFailedText("没有发现小动物").loadFailed()
                                    }
                                }
                                filterResults.forEach{
                                    var cropBitmap = Bitmap.createBitmap(
                                        bitmap,
                                        it.boundingBox.left,
                                        it.boundingBox.top,
                                        it.boundingBox.width(),
                                        it.boundingBox.height()
                                    )
                                    startSearching(SearchResult(cropBitmap,it, emptyList(),null))
                                }
                            }
                        }.start()
                    }
                    .addOnFailureListener{
                        Log.e(D_TAG,it.message.toString())
                        if(waitingDialog.onShowing){
                            runOnUiThread{
                                waitingDialog.setFailedText("搜索失败").loadFailed()
                            }
                        }
                    }
            }.start()
        }
    }


    private fun startSearching(searchResult: SearchResult){
        searchResult?.let {
            searchApi.searchImage(it.bitmap)
                .addOnSuccessListener {response ->
                    synchronized(this){
                        searchedResultNum++
                        Log.d(TAG,"start searching ${searchedResultNum} / ${detectResultNum}")
                    }

                    if((response.size > 0) && (response[0].name != "非动物")){
                        it.data = response
                        it.dialog = SearchResultDialog(this,it.bitmap,response)
                        synchronized(this){
                            viewBinding.ivPreview.addDetectionResult(it)
                        }
                        if (waitingDialog.onShowing) {
                            waitingDialog.setSuccessText("搜索完成").loadSuccess()
                        }
                        if(detectResultNum == 1){
                            it.dialog?.show()
                        }
                    }else{
                        Log.w(TAG,"not an animal")
                        when (searchedResultNum) {
                            detectResultNum -> {
                                Log.w(TAG,"no animals detected")
                                waitingDialog.setFailedText("似乎没有小动物").loadFailed()
                            }
                        }
                    }
                }
                .addOnFailureListener{
                    Log.e(TAG,"search failed")
                    if(waitingDialog.onShowing){
                        waitingDialog.setFailedText("${it.message}").loadFailed()
                    }
                }
        }
    }

    private fun dispathCaptureIntent(){
        Log.d(TAG,"start capture")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile("TEMP_IMAGE")
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    cameraPhotoUri = FileProvider.getUriForFile(
                        this,
                        "com.labx.scanimal.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } ?: run {
                Toast.makeText(this, "No Camera Found!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun pictureFromGallery(){
        Log.d(TAG,"choose from gallery")
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }, REQUEST_IMAGE_GALLERY)
    }

    @Throws(IOException::class)
    private fun createImageFile(fileName: String): File{
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            fileName,
            ".jpg",
            storageDir
        )
    }

    private fun getBitmapFromUri(imageUri: Uri): Bitmap? {
        val bitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }
        } catch (ex: IOException) {
            null
        }
        if(bitmap == null) return null;
        val baos = ByteArrayOutputStream()
        var options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG,options,baos)
        Log.d(TAG,"original image size: ${baos.toByteArray().size / 1024} bytes")
        // compress
        while(options > 20 && baos.toByteArray().size / 1024 > 256){
            options -= 20;
            baos.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG,options,baos)
        }
        return BitmapFactory.decodeStream(ByteArrayInputStream(baos.toByteArray()))
    }

}

class WaitingDialog : LoadingDialog {
    var onShowing: Boolean = false
    constructor(context: Context) : super(context) {
        setInterceptBack(true)
        closeFailedAnim()
        closeSuccessAnim()
    }

    override fun show() {
        super.show()
        onShowing = true
    }

    override fun loadSuccess() {
        super.loadSuccess()
        onShowing = false
    }

    override fun loadFailed() {
        super.loadFailed()
        onShowing = false
    }

    override fun close() {
        super.close()
        onShowing = false
    }
}