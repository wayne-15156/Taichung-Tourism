package com.example.taichungtourism

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taichungtourism.GoogleMapUtil.Companion.animateCamera
import com.example.taichungtourism.GoogleMapUtil.Companion.animateCameraWithBounds
import com.example.taichungtourism.GoogleMapUtil.Companion.initialize
import com.example.taichungtourism.databinding.ActivityMainBinding
import com.example.taichungtourism.databinding.DialogAttractionDetailBinding
import com.example.taichungtourism.databinding.DialogMarkerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gmap: GoogleMap

    private var enableSelectEvent = false

    private val attractions = ArrayList<Attraction>()
    private val attractionMarkers = ArrayList<Marker>()

    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private val spinnerItemList = ArrayList<String>()

    data class Attraction(
        val name: String,
        val lat: Double,
        val lng: Double,
        val address: String,
        val introduction: String,
        val imageUrl: String
    )

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && requestCode == 0) {
            for(result in grantResults)
                if(result != PackageManager.PERMISSION_GRANTED)
                    finish()
            loadMap()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)

        setSpinner()
    }

    private fun setSpinner() = binding.apply {
        spinnerAdapter = ArrayAdapter(this@MainActivity, R.layout.item_spinner, spinnerItemList)
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (!enableSelectEvent) {
                    enableSelectEvent = true
                    return
                }
                showMarkerInfo(attractions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 當沒有選擇任何項目時執行的動作
                Toast.makeText(this@MainActivity, "onNothingSelected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gmap = map
        map.initialize()
        //load CSV檔案加入marker
        loadCSVFile()
        map.animateCameraWithBounds(
            LatLng(24.146551, 120.677378),
            LatLng(24.135753, 120.688449),
            0
        )
        attractions.forEach {
            map.addMarker(
                MarkerOptions()
                    .title(it.name)
                    .position(LatLng(it.lat, it.lng))
            )?.let {
                attractionMarkers.add(it)
            }
        }
        setMapListener(map)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.showInfoWindow()
        attractions.find { it.name == marker.title}?.let {
            showMarkerInfo(it)
        }
        return true
    }

    private fun setMapListener(mMap: GoogleMap) {
        mMap.setOnMarkerClickListener(this)

        mMap.setOnCameraMoveListener { }
    }

    @SuppressLint("MissingPermission")
    private fun showMarkerInfo(data: Attraction) {
        //切換地圖Camera
        gmap.animateCamera(LatLng(data.lat, data.lng), 17f)

        val dialogBinding = DialogMarkerBinding.inflate(layoutInflater)
        val rootView = dialogBinding.root
        val dialog = AlertDialog.Builder(this)
            .setView(rootView)
            .create()
        // 設置對話框窗口背景為透明
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogBinding.apply {
            tvTitle.text = data.name
            btnDetail.setOnClickListener {
                //顯示景點詳細資訊Dialog
                dialog.dismiss()
                val dialogBinding2 = DialogAttractionDetailBinding.inflate(layoutInflater)
                val rootView2 = dialogBinding2.root
                val dialog2 = AlertDialog.Builder(this@MainActivity)
                    .setView(rootView2)
                    .create()
                dialog2.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialogBinding2.apply {
                    Picasso.get()
                        .load(data.imageUrl)
                        .into(dialogBinding2.imgAttraction)
                    tvTitle.text = data.name
                    tvAddress.text = data.address
                    tvIntro.text = data.introduction
                    btnClose.setOnClickListener { dialog2.dismiss() }
                }
                dialog2.show()
            }
            btnNavigate.setOnClickListener {
                val locationResult: Task<Location> = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        // 處理定位資訊
                        GoogleMapUtil.startGoogleMapRoutePlan(this@MainActivity, LatLng(location.latitude, location.longitude), LatLng(data.lat, data.lng))
                    } else {
                        Toast.makeText(this@MainActivity, "無法取得定位資訊", Toast.LENGTH_SHORT).show()
                    }
                }
                //dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun loadCSVFile() {
        try {
            val inputStream: InputStream = assets.open("taichung_tourism.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var row: String?
            var i = 0

            // 讀取CSV文件的每一行
            while (reader.readLine().also { row = it } != null) {
                // 在這裡處理每一行的資料，例如拆分成欄位，處理資料等
                val columns = row!!.split(",")
                // 這裡示範將每一行的第一個欄位和第二個欄位輸出至 Log
                attractions.add(Attraction(columns[0], columns[1].toDouble(), columns[2].toDouble(), columns[3], columns[4], columns[5]))
                spinnerItemList.add(columns[0])
                i++
            }
            spinnerAdapter.notifyDataSetChanged()
            reader.close()
        } catch (e: IOException) {
            Log.e("loadCSVFile Exception", "$e")
        }
    }

    private fun loadMap() {
        val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)
    }
}