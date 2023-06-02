package ru.mirea.tsybulko.lesson8

import android.Manifest
import android.R
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CompositeIcon
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.mirea.tsybulko.lesson8.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), UserLocationObjectListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        // check permissions

        if (!checkPermissions()) {
            requestPermissions()
        }

        userLocationLayer = loadUserLocationLayer()
/*

        mapView.map.move(
            CameraPosition(
                Point(55.751574, 37.573856),
                11.0f,
                0.0f,
                0.0f
            ),
            Animation(Animation.Type.SMOOTH, 0.0f),
            null
        )
 */
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            200
        )
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    private fun loadUserLocationLayer(): UserLocationLayer {
        MapKitFactory.getInstance().run {
            resetLocationManagerToDefault()
            return createUserLocationLayer(mapView.mapWindow).apply {
                isVisible = true
                isHeadingEnabled = true
                setObjectListener(this@MainActivity)
            }
        }
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )

        // При определении направления движения устанавливается следующая иконка
        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                this, R.drawable.arrow_up_float
            )
        )

        // При получении координат местоположения устанавливается следующая иконка
        val pinIcon: CompositeIcon = userLocationView.pin.useCompositeIcon()
        pinIcon.setIcon(
            "pin",
            ImageProvider.fromResource(this, R.drawable.btn_radio),
            IconStyle().setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(1f)
                .setScale(0.5f)
        )
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }
}