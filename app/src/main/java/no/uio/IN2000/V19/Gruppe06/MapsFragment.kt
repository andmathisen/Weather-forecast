package no.uio.IN2000.V19.Gruppe06



import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location

import android.os.Build
import android.os.Bundle
import android.os.Looper

import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment

import android.support.v4.content.ContextCompat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


import java.util.ArrayList
import kotlin.random.Random


class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker? = null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    private var ruter: ArrayList<Model> = ArrayList()
    lateinit var koordinater: ArrayList<String>
    private lateinit var punkter: ArrayList<LatLng>
    lateinit var ruteNavn:String
    var bundle: Bundle? = null
    var dist: Double = 0.0


    companion object {
        fun newInstance(): MapsFragment = MapsFragment()

        private const val MY_PERMISSION_CODE: Int = 1000
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


            return inflater.inflate(R.layout.fragment_maps, container, false)
        }



    fun checkDuplicate(name:String):Boolean{
        var bol = true
        for (model in ruter){
            if(model.title == name){
                bol = false
            }
        }
        return bol
    }

        // Haversine formula
        fun calculateDistance(punkter:ArrayList<LatLng>):Double{
            var dist:Double = 0.0
            var n = 0
            val R = 6372.8
            var a:Double = 0.0
            var c:Double = 0.0
            var dLat:Double = 0.0
            var dLon:Double = 0.0
            var originLat:Double = 0.0
            var destLat:Double = 0.0
            while(n<(punkter.size-1)){
                if(n == punkter.size-1){
                    break
                }
                dLat = Math.toRadians(punkter[n+1].latitude - punkter[n].latitude)
                dLon = Math.toRadians(punkter[n+1].longitude - punkter[n].longitude)
                originLat = Math.toRadians(punkter[n].latitude)
                destLat = Math.toRadians(punkter[n+1].latitude)


                a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLon / 2), 2.toDouble()) * Math.cos(originLat) * Math.cos(destLat)

                c = 2 * Math.asin(Math.sqrt(a))

                dist = dist + (R * c)



                n++
            }

            return dist
        }

    private fun generateBitmapDescriptorFromRes(context: Context, resId: Int, tint: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, resId)
        drawable!!.setBounds(
            0,
            0,
            drawable.intrinsicWidth,
            drawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        drawable.setTint(tint)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {

                buildLocationRequest()
                buildLocationCallback()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        punkter = ArrayList()
        bundle = arguments
        if (bundle != null) {

            var i = 0
            var latitude: Double
            var longitude: Double
            ruteNavn = bundle!!.getString("name")
            koordinater = bundle!!.getStringArrayList("koordinater")
            while (i < koordinater.size) {
                latitude = koordinater[i].toDouble()
                longitude = koordinater[i + 1].toDouble()
                Log.d(TAG, latitude.toString())
                Log.d(TAG, longitude.toString())
                punkter.add(LatLng(latitude, longitude))
                i += 2
            }


            dist = calculateDistance(punkter)

            if (checkDuplicate(ruteNavn)) {
                ruter.add(Model(ruteNavn, punkter, dist))
            }
        }


        for(model in ruter){//Skal tegne alle ruter på kartet, men fungerer foreløbig ikke.
            val tint:Int = Random.nextInt(0,255)
            for(point in model.punkter) {
                val markerOptions = MarkerOptions()
                    .position(point)  // Setting latitude and longitude of the marker position
                    .icon(generateBitmapDescriptorFromRes(activity!!.applicationContext, R.drawable.ic_veipunkt,tint))//Custom marker
                    .flat(true)
                    .anchor(0.5F, 0.5F)


                val polylineOptions = PolylineOptions()
                    .color(Color.WHITE)
                    .jointType(0)
                    .width(8f)

                mMap.addMarker(markerOptions)
                mMap.addPolyline(polylineOptions)
            }
        }



    }

        private fun buildLocationCallback() {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    mLastLocation = p0!!.locations[p0.locations.size - 1] // get last location

                    if (mMarker != null) {
                        mMarker!!.remove()
                    }
                    latitude = mLastLocation.latitude
                    longitude = mLastLocation.longitude
                    val latLng = LatLng(latitude, longitude)
                    val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title("Din posisjon")


                    mMarker = mMap.addMarker(markerOptions)

                    // Move camera

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f))

                }
            }
        }

        private fun buildLocationRequest() {
            locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 5000
            locationRequest.fastestInterval = 3000
            locationRequest.smallestDisplacement = 10f

        }

        private fun checkLocationPermission(): Boolean {

            if (ContextCompat.checkSelfPermission(
                    this.activity!!,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this.activity!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {

                    ActivityCompat.requestPermissions(
                        this.activity!!, arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ), MY_PERMISSION_CODE
                    )
                } else
                    ActivityCompat.requestPermissions(
                        this.activity!!, arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ), MY_PERMISSION_CODE
                    )
                return false

            } else
                return true
        }

        // Override OnRequestPermissionResult
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            when (requestCode) {
                MY_PERMISSION_CODE -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(
                                activity!!.applicationContext,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            if (checkLocationPermission()) {
                                buildLocationRequest()
                                buildLocationCallback()

                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
                                fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback,
                                    Looper.myLooper()
                                )
                                mMap.isMyLocationEnabled = true
                            }
                        }
                    } else {
                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }

        override fun onStop() {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            super.onStop()
            Log.d(TAG,"Map on Stop")

        }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"Map resumed")
    }

        override fun onMapReady(googleMap: GoogleMap?) {
            mMap = googleMap!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        activity!!.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                }
            } else {
                mMap.isMyLocationEnabled = true
            }

            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            mMap.setPadding(0, 0, 0, 80)
            mMap.uiSettings.isZoomControlsEnabled = true


        }


    }
