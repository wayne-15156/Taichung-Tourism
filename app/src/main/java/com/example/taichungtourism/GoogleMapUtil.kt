package com.example.taichungtourism

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.Dimension
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Google Map 工具包
 *
 * @property initialize 初始化地圖
 * @property moveCamera 移動地圖位置
 * @property updateCamera 更新地圖位置
 * @property animateCamera 移動地圖位置，並且帶有動畫
 * @property setCompass 設定地圖的指南針位置
 */
@SuppressLint("MissingPermission")
class GoogleMapUtil {
    companion object {
        const val packageName = "com.google.android.apps.maps"
        /**
         * 參數
         */
        const val NORMAL_ZOOM_LEVEL = 15f
        const val MAX_ZOOM_LEVEL = 18f
        const val MIN_ZOOM_LEVEL = 12f

        /**
         * 初始化地圖
         */
        fun GoogleMap.initialize() {
            // 地圖設定
            //isMyLocationEnabled = true
            uiSettings.setAllGesturesEnabled(true)
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }

        /**
         * 移動地圖位置
         *
         * @param location [LatLng] 位置
         * @param zoomLevel [Float] 縮放等級
         */
        fun GoogleMap.moveCamera(
            location: LatLng,
            zoomLevel: Float? = null
        ) {
            val update = zoomLevel?.let { CameraUpdateFactory.newLatLngZoom(location, it) }
                ?: CameraUpdateFactory.newLatLng(location)
            moveCamera(update)
        }

        /**
         * 更新地圖位置
         *
         * @param cameraPosition [CameraPosition] 位置
         */
        fun GoogleMap.updateCamera(
            cameraPosition: CameraPosition
        ) {
            moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        /**
         * 移動地圖位置，並且帶有動畫
         *
         * @param location [LatLng] 位置
         * @param zoomLevel [Float] 縮放等級
         * @param finish [Function] 結束時的回調
         * @param cancel [Function] 取消時的回調
         */
        fun GoogleMap.animateCamera(
            location: LatLng,
            zoomLevel: Float? = null,
            finish: () -> Unit = {},
            cancel: () -> Unit = {}
        ) {
            val update = zoomLevel?.let { CameraUpdateFactory.newLatLngZoom(location, it) }
                ?: CameraUpdateFactory.newLatLng(location)

            animateCamera(
                update,
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() = finish.invoke()
                    override fun onCancel() = cancel.invoke()
                }
            )
        }

        fun GoogleMap.animateCameraWithBounds(
            latLng1: LatLng,
            latLng2: LatLng,
            padding: Int,
            finish: () -> Unit = {},
            cancel: () -> Unit = {}
        ) {
            val southwest = LatLng(
                Math.min(latLng1.latitude, latLng2.latitude),
                Math.min(latLng1.longitude, latLng2.longitude)
            )
            val northeast = LatLng(
                Math.max(latLng1.latitude, latLng2.latitude),
                Math.max(latLng1.longitude, latLng2.longitude)
            )
            val update = CameraUpdateFactory.newLatLngBounds(LatLngBounds(southwest, northeast), padding)
            animateCamera(
                update,
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() = finish.invoke()
                    override fun onCancel() = cancel.invoke()
                }
            )
        }

        /**
         * 啟動googleMap規劃路線
         * @param origin 起點經緯度
         * @param dest 終點經緯度
         */
        fun startGoogleMapRoutePlan(
            activity: Activity,
            origin: LatLng,
            dest: LatLng
        ) {
            val gmmIntentUri  = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(packageName)
            mapIntent.resolveActivity(activity.packageManager)?.let {
                activity.startActivity(mapIntent)
            } ?: Toast.makeText(activity,"尚無支援的應用程式", Toast.LENGTH_SHORT).show()
        }

    }
}
