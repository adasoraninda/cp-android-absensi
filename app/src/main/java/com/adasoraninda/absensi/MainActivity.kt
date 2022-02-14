package com.adasoraninda.absensi

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.skyfishjy.library.RippleBackground
import java.lang.Math.toRadians
import java.util.*
import kotlin.math.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var textScanning: TextView? = null
    private var buttonCheckIn: CircleButtonView? = null
    private var rippleEffect: RippleBackground? = null

    private val realTimeDb by lazy { FirebaseRealTimeDb.getInstance() }

    private var fusedLocation: FusedLocationProviderClient? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrElse(Manifest.permission.ACCESS_FINE_LOCATION) { false } &&
                    permissions.getOrElse(Manifest.permission.ACCESS_COARSE_LOCATION) { false } -> {
                Log.d(TAG, "Permissions granted")

                if (!checkLocationEnabled()) return@registerForActivityResult

                getLastLocation()
            }
            else -> {
                Log.e(TAG, "permission not granted")
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        buttonCheckIn?.setOnClickListener {
            if (!checkLocationPermission()) {
                requestLocationPermissions()
            } else {
                if (!checkLocationEnabled()) return@setOnClickListener
                getLastLocation()
            }
        }

    }

    private fun initView() {
        textScanning = findViewById(R.id.text_scanning)
        buttonCheckIn = findViewById(R.id.button_check_in)
        rippleEffect = findViewById(R.id.ripple_effect)
    }

    private fun checkLocationPermission(): Boolean {
        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return coarseLocation && fineLocation
    }

    private fun checkLocationEnabled(): Boolean {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return false
        }

        return true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    private fun getLastLocation() {
        startRippleAnimation()
        if (checkLocationPermission() && checkLocationEnabled()) {
            fusedLocation?.lastLocation?.addOnSuccessListener { location ->
                val currentLat = location.latitude
                val currentLong = location.longitude

                val destinations = getDestinations()
                if (destinations.isEmpty()) throw RuntimeException("Destination not found")

                Thread {
                    val distance = calculateDistance(
                        currentLat,
                        currentLong,
                        destinations[0].latitude,
                        destinations[0].longitude
                    ) * 1000 // to km

                    if (distance < 10.0) {
                        Handler(mainLooper).post {
                            val clickListener = { di: DialogInterface, name: String? ->
                                val user = User(name, Utils.getCurrentDate())
                                sendUserToDatabase(user)
                                di.cancel()
                            }

                            val formDialog = FormDialog(clickListener)
                            formDialog.isCancelable = false
                            formDialog.show(supportFragmentManager, FormDialog.TAG)
                        }
                    } else {
                        Handler(mainLooper).post {
                            Log.e(TAG, "distance to far")
                            Toast.makeText(this, "Failed Absent", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }?.addOnFailureListener {
                Log.e(TAG, it.message.toString())
                Toast.makeText(this, "Failed Absent", Toast.LENGTH_LONG).show()
                stopRippleAnimation()
            }
        }
    }

    private fun sendUserToDatabase(user: User) {
        Handler(mainLooper).post {
            realTimeDb.sendUserToDatabase(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Send user to db success")
                    Toast.makeText(this, "Check in Success", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.e(TAG, it.message.toString())
                    Toast.makeText(this, "Check in Failed", Toast.LENGTH_SHORT).show()
                }.addOnCompleteListener {
                    stopRippleAnimation()
                }
        }
    }

    private fun getDestinations(): List<Address> {
        val destination = "Institut Teknologi Indonesia"
        val geocode = Geocoder(this, Locale.getDefault())
        return geocode.getFromLocationName(destination, 100)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6372.8 // in kilometers

        val radiansLat1 = toRadians(lat1)
        val radiansLat2 = toRadians(lat2)
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        return 2 * r * asin(
            sqrt(
                sin(dLat / 2).pow(2.0)
                        + sin(dLon / 2).pow(2.0)
                        * cos(radiansLat1)
                        * cos(radiansLat2)
            )
        )
    }

    private fun startRippleAnimation() {
        val rippleBackground = rippleEffect ?: return

        if (!rippleBackground.isRippleAnimationRunning) {
            rippleBackground.startRippleAnimation()
        }
    }

    private fun stopRippleAnimation() {
        val rippleBackground = rippleEffect ?: return

        if (rippleBackground.isRippleAnimationRunning) {
            rippleBackground.stopRippleAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        buttonCheckIn?.setOnClickListener(null)
        stopRippleAnimation()
    }

}