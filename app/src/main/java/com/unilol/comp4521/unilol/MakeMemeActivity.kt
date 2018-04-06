package com.unilol.comp4521.unilol

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import ly.img.android.ui.activities.ImgLyIntent
import ly.img.android.ui.activities.CameraPreviewBuilder
import ly.img.android.sdk.models.state.EditorSaveSettings
import ly.img.android.sdk.models.state.CameraSettings
import ly.img.android.sdk.models.state.manager.SettingsList
import android.app.Activity
import android.net.Uri
import android.util.Log
import ly.img.android.PESDK
import ly.img.android.sdk.models.constant.Directory
import ly.img.android.ui.utilities.PermissionRequest
import java.io.File


class MakeMemeActivity : Activity(), PermissionRequest.Response {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val settingsList = SettingsList()

        settingsList
                .getSettingsModel(CameraSettings::class.java)
                .setExportDir(Directory.DCIM, FOLDER)
                .setExportPrefix("camera_")

                .getSettingsModel(EditorSaveSettings::class.java)
                .setExportDir(Directory.DCIM, FOLDER)
                .setExportPrefix("result_")
                .setJpegQuality(80, false).savePolicy = EditorSaveSettings.SavePolicy.KEEP_SOURCE_AND_CREATE_ALWAYS_OUTPUT

        CameraPreviewBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, CAMERA_PREVIEW_RESULT)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        Log.i("Image editor", requestCode.toString() + " " + resultCode + ".")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_PREVIEW_RESULT) {

            val resultPath = data!!.getStringExtra(ImgLyIntent.RESULT_IMAGE_PATH)
            val sourcePath = data.getStringExtra(ImgLyIntent.SOURCE_IMAGE_PATH)

            if (resultPath != null) {
                // Add result file to Gallery
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(resultPath))))
            }

            if (sourcePath != null) {
                // Add sourceType file to Gallery
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(sourcePath))))
            }

            // save image and finish image editor activity
            Toast.makeText(PESDK.getAppContext(), "Image saved on: " + resultPath!!, Toast.LENGTH_LONG).show()
            // start post activity
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == CAMERA_PREVIEW_RESULT && data != null) {
            val sourcePath = data.getStringExtra(ImgLyIntent.SOURCE_IMAGE_PATH)
//            Toast.makeText(PESDK.getAppContext(), "Editor canceled, sourceType image is:\n$sourcePath", Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun permissionGranted() {

    }

    override fun permissionDenied() {
        finish()
        System.exit(0)
    }

    override fun onBackPressed() {
        Log.i("image editor", "back pressed")
    }

    companion object {

        private val FOLDER = "ImgLy"
        var CAMERA_PREVIEW_RESULT = 1
    }
}

