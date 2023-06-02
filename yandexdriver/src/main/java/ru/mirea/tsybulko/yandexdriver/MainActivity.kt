package ru.mirea.tsybulko.yandexdriver

import android.Manifest
import android.R
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.*
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import ru.mirea.tsybulko.yandexdriver.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener {
    private lateinit var binding: ActivityMainBinding

    private var routeStartPosition: Point? = null

    companion object {
        val ROUTE_END_LOCATION: Point = Point(55.772151, 37.619540) // ra'men
        val COLORS = intArrayOf(-0x10000, -0xff0100, 0x00FFBBBB, -0xffff01)

        const val endLocationDescription = "The best ramen ever. From 12:00 to 00:00 every day"
    }

    private lateinit var mapView: MapView
    private var mapObjects: MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        MapKitFactory.initialize(this)
        DirectionsFactory.initialize(this)

        setContentView(binding.root)
        mapView = binding.mapView
        mapView.map.isRotateGesturesEnabled = false

        // check permissions
        if (!checkPermissions()) {
            requestPermissions()
        }

        // create object for driver route
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapView.map.mapObjects.addCollection()

        // get location
        locationManager = MapKitFactory.getInstance().createLocationManager()
        locationListener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                if (routeStartPosition == null) {
                    routeStartPosition = location.position

                    val screenCenter = Point(
                        (routeStartPosition!!.latitude + ROUTE_END_LOCATION.latitude) / 2,
                        (routeStartPosition!!.longitude + ROUTE_END_LOCATION.longitude) /
                                2
                    )
                    mapView.map.move(CameraPosition(screenCenter, 10.0f, 0.0f, 0.0f))
                    submitRequest()
                }
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {}
        }
    }

    private fun submitRequest() {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()

        // alternative routes count
        drivingOptions.routesCount = 4
        val requestPoints: ArrayList<RequestPoint> = ArrayList()

        // set route points
        requestPoints.apply {
            add(RequestPoint(routeStartPosition!!, RequestPointType.WAYPOINT, null))
            add(RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null))
        }
        // create server request
        drivingSession = drivingRouter!!.requestRoutes(
            requestPoints, drivingOptions,
            vehicleOptions, this
        )

        // create marker
        mapView.map.mapObjects.addPlacemark(
            ROUTE_END_LOCATION
        ).apply {
            addTapListener { mapObject, point ->
                Toast.makeText(application, endLocationDescription, Toast.LENGTH_SHORT).show()
                true
            }
        }
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

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        locationManager.unsubscribe(locationListener)
        mapView.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        locationManager.subscribeForLocationUpdates(
            0.0,
            1000,
            1.0,
            false,
            FilteringMode.OFF,
            locationListener
        )
    }

    override fun onDrivingRoutes(list: MutableList<DrivingRoute>) {
        var color: Int
        for (i in 0 until list.size) {
            // configure colors for routes
            color = COLORS[i]

            // add route to the map
            mapObjects!!.addPolyline(list[i].geometry).setStrokeColor(color)
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        var errorMessage = "Unknown error"
        if (error is RemoteError) {
            errorMessage = "Remote error"
        } else if (error is NetworkError) {
            errorMessage = "Network error"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}