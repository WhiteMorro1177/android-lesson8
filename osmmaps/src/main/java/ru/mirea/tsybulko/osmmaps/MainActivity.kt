package ru.mirea.tsybulko.osmmaps


import android.os.Bundle
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.preference.PreferenceManager
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

import ru.mirea.tsybulko.osmmaps.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.setZoomRounding(true)
        mapView.setMultiTouchControls(true)

        // receive permissions
        checkAndRequestPermissions(
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        )

        MyLocationNewOverlay(
            GpsMyLocationProvider(applicationContext), mapView
        ).apply {
            enableMyLocation()
            mapView.overlays.add(this)
            runOnFirstFix {
                try {
                    val latitude = this.myLocation.latitude
                    val longitude = this.myLocation.longitude
                    runOnUiThread {
                        val mapController: IMapController = mapView.controller
                        mapController.setZoom(1.0)
                        val point = GeoPoint(latitude, longitude)
                        mapController.setCenter(point)
                    }
                } catch (exc: Exception) {
                    Log.e("osm", "Error ${exc.message}")
                }
            }
        }

        CompassOverlay(
            applicationContext,
            InternalCompassOrientationProvider(applicationContext),
            mapView
        ).apply {
            enableCompass()
            mapView.overlays.add(this)
        }

        ScaleBarOverlay(mapView).apply {
            setCentred(true)
            setScaleBarOffset(applicationContext.resources.displayMetrics.widthPixels / 2, 10)
            mapView.overlays.add(this)
        }

        mapView.overlays.run {
            add(Marker(mapView).apply {
                position = GeoPoint(55.772151, 37.619540)
                icon = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.osm_ic_follow_me_on
                )
                title = "Ra'men"
                setOnMarkerClickListener { marker, mapView ->
                    Toast.makeText(
                        applicationContext, "Ra'men",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            })
            add(Marker(mapView).apply {
                position = GeoPoint(55.776497, 37.658118)
                icon = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.osm_ic_follow_me_on
                )
                title = "Kfc on Komsomolskaya"
                setOnMarkerClickListener { marker, mapView ->
                    Toast.makeText(
                        applicationContext, "Kfc on Komsomolskaya",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            })
            add(Marker(mapView).apply {
                position = GeoPoint(55.794229, 37.700772)
                icon = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.osm_ic_follow_me_on
                )
                title = "MIREA"
                setOnMarkerClickListener { marker, mapView ->
                    Toast.makeText(
                        applicationContext, "MIREA",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            })
        }
    }

    private fun checkAndRequestPermissions(permissionsToCheck: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            200
        )
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView.onResume()

    }

    override fun onPause() {
        super.onPause()
        Configuration.getInstance().save(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView.onPause()
    }
}