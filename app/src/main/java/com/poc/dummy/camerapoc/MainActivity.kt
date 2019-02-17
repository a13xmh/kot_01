package com.poc.dummy.camerapoc

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import java.io.*


class MainActivity : AppCompatActivity() {

    private val url = "https://s3-us-west-2.amazonaws.com/webviewtonative/index.html"
    //private val url = "file:///android_asset/index.html"
    private val IMAGE_CAPTURE_CODE = 1001


    var a: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val setts = web_view.settings


        setts.javaScriptEnabled = true

        // cache
        setts.setAppCacheEnabled(false)
        setts.cacheMode = WebSettings.LOAD_NO_CACHE
        setts.setAppCachePath(cacheDir.path)


        setts.textZoom = 500


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setts.safeBrowsingEnabled = true  // api 26
        }


        setts.useWideViewPort = true
        setts.loadWithOverviewMode = true
        setts.javaScriptCanOpenWindowsAutomatically = true
        setts.mediaPlaybackRequiresUserGesture = false



        // WebView settings
        web_view.fitsSystemWindows = true
        web_view.addJavascriptInterface(InjectorJavaScriptInterface(this), "Android")
        web_view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        web_view.loadUrl(url)

    }





    override  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK){

            try {
                Log.d("TAG:::::....", a.toString())
            }catch (e :IOException){
                e.printStackTrace()
            }

        }

    }


    internal class InjectorJavaScriptInterface(private val activity: MainActivity) {

        private val PERMISSION_CODE = 1000
        private val IMAGE_CAPTURE_CODE = 1001
        var image_uri: Uri? = null

        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(activity, toast, Toast.LENGTH_SHORT).show()
        }
            @JavascriptInterface
        fun photoFlash() {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

                    //permission denied
                    Toast.makeText(activity, "request permissions", Toast.LENGTH_SHORT).show()

                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE
                    )


                }else{
                    //permission granted
                    openCamera()
                }
            }else{
                //
            }

        }


        @JavascriptInterface
        fun getData(): String {
            if(activity.a !== null){


                var byteArrayContent = convertToByteArrayOutputStream(activity.contentResolver.openInputStream(activity.a)).toByteArray()

                return android.util.Base64.encodeToString(byteArrayContent, 0)

            }

            return "null"
        }



        fun convertToByteArrayOutputStream(stream: InputStream): ByteArrayOutputStream {
            val out = ByteArrayOutputStream(8192)
            val buffer = ByteArray(8192)

            try {

                while (true) {

                    val length = stream.read(buffer)

                    if (length <= 0)

                        break

                    out.write(buffer, 0, length)

                }

                out.flush()

                return out

            } catch (t: Throwable) { }

         return out
    }


        private fun openCamera(){
            //Toast.makeText(mContext, "click flash", Toast.LENGTH_SHORT).show()

            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.TITLE, "new picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "from the camera")

            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

            image_uri = activity.contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values)



            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)


            activity.a = image_uri



            activity.startActivityForResult(intent, IMAGE_CAPTURE_CODE)
        }

    }

}
