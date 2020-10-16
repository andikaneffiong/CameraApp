package com.example.andikan

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.fotoapparat.Fotoapparat
import io.fotoapparat.FotoapparatBuilder
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.Flash
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import io.fotoapparat.selector.off
import io.fotoapparat.selector.torch
import io.fotoapparat.view.CameraView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    var fotoapparat: Fotoapparat? = null
    val filename = "test.png"
    @RequiresApi(30)
    val storageSpace  = Environment.getStorageDirectory()
    val dest = File(storageSpace,filename)
    var fotoapparateState: FotoapparatState? = null
    var cameraStatus: CameraState? = null
    var flashState: FlashState? = null



    val  permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createFotoapparat()
        cameraStatus = CameraState.Back
        flashState = FlashState.OFF
       fotoapparateState = FotoapparatState.OFF

        fab_camera.setOnClickListener(){
            takephoto()

        }
        fab_switch.setOnClickListener() {
            switchcamera()
        }
        fab_flash.setOnClickListener(){
            changeFlashState()
        }


    }
private fun createFotoapparat() {
    val cameraView = findViewById<CameraView>(R.id.Camera_view)
    fotoapparat = Fotoapparat(
        context = this,
        view = cameraView,
        scaleType = ScaleType.CenterCrop,
        lensPosition = back(),
        logger = loggers(
            logcat()
        ),
        cameraErrorCallback = { error ->
            println("Recorder errors: $error")
        }


    )
}
private  fun changeFlashState(){
    fotoapparat?.updateConfiguration(
        CameraConfiguration(flashMode = if(flashState == FlashState.TORCH) off(

        )else torch())
    )
    if (flashState == FlashState.TORCH) flashState == FlashState.OFF
    else flashState = FlashState.TORCH
}
    private fun switchcamera(){
        fotoapparat?.switchTo(
            lensPosition =   if(cameraStatus == CameraState.Back) front() else back(),
            cameraConfiguration = CameraConfiguration()
        )
        if(cameraStatus == CameraState.Back) cameraStatus = CameraState.FRONT
        else cameraStatus = CameraState.Back
    }
    private fun takephoto(){
        if(hasNoPermission()){
            requestPermissions()
        }else {
            fotoapparat
                ?.takePicture()
                ?.saveToFile(dest)
        }


    }

    override fun onStart() {
        super.onStart()
        if (hasNoPermission()){
            requestPermissions()
        }else{
            fotoapparat?.start()
            fotoapparateState = FotoapparatState.ON
        }
    }







    private  fun hasNoPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }
    fun  requestPermissions(){
        ActivityCompat.requestPermissions(this,permissions, 0)
    }

    override fun onStop() {
        super.onStop()
            fotoapparat?.stop()
            FotoapparatState.OFF

    }
override  fun  onResume(){
    super.onResume()
    if (!hasNoPermission()&& fotoapparateState ==FotoapparatState.OFF){
        val intent = Intent(baseContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
}
enum class CameraState{
    FRONT,Back
}
enum class FlashState{
    TORCH,OFF
}
enum class FotoapparatState{
    ON,OFF
}

