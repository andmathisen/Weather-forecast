package no.uio.IN2000.V19.Gruppe06



import android.content.ContentValues.TAG
import android.content.Context

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

import android.view.*
import android.widget.Toast
import android.support.v7.widget.Toolbar
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas

import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.EditText
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException




class NyRuteFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap//
    private lateinit var toolbar: Toolbar

    private lateinit var points: ArrayList<LatLng>
    private lateinit var markers: ArrayList<Marker>
    private lateinit var polylines: ArrayList<Polyline>

    private lateinit var locationCallback: LocationCallback//

    private var latitude: Double = 0.toDouble()//
    private var longitude: Double = 0.toDouble()//

    private lateinit var mLastLocation: Location//
    private var mMarker: Marker? = null//
    private lateinit var locationRequest: LocationRequest//
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient//









    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_ny_rute, container, false)
        setHasOptionsMenu(true)
        toolbar = v.findViewById(R.id.toolbar)
        toolbar.setTitle("Lag rute")
        toolbar.inflateMenu(R.menu.menu_items)
        toolbar.setOnMenuItemClickListener(toolbarListener)




        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)




    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_items, menu)

        super.onCreateOptionsMenu(menu, inflater)

}


    fun clearMarkers(){
        points.clear()

        for(marker in markers){
            marker.remove()
        }
        markers.clear()

        for (polyline in polylines){
            polyline.remove()
        }
        polylines.clear()
    }

    override fun onPause() {
        super.onPause()

    }

    private val toolbarListener: Toolbar.OnMenuItemClickListener =
        Toolbar.OnMenuItemClickListener { item ->
            val builder: AlertDialog.Builder
            val input:EditText
            var name : String


            when(item.itemId){
                R.id.back -> {
                    if(markers.size != 0){
                        points.removeAt(points.size-1)

                        markers[markers.size-1].remove()
                        markers.removeAt(markers.size-1)

                        polylines[polylines.size-1].remove()
                        polylines.removeAt(polylines.size-1)

                        //polylineOptions.points.removeAt(points.size-1)

                    }

                }

                R.id.clear ->{
                    clearMarkers()

                }
                R.id.done ->{

                    builder = AlertDialog.Builder( ContextThemeWrapper(activity!!, R.style.AlertDialog_theme))
                    builder.setTitle("Rutenavn")


                    input = EditText(activity!!)
                    input.inputType = InputType.TYPE_CLASS_TEXT
                    builder.setView(input)

                    builder.setPositiveButton(
                        "OK"
                    ) { dialog, which -> name = input.text.toString()

                        if(name.isNullOrBlank()){
                            Toast.makeText(activity,"Mangler navn!",Toast.LENGTH_LONG).show()

                        } else{


                            val ma:MainActivity = activity as MainActivity
                            ma.sendInfoMap(name,points)
                            ma.sendInfo(name,points)


                            clearMarkers()

                        }

                    }

                    builder.setNegativeButton(
                        "Cancel"
                    ) { dialog, which -> dialog.cancel() }

                    builder.show()
                }


            }

            true
        }




    companion object {
        fun newInstance(): NyRuteFragment = NyRuteFragment()

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

    private fun generateBitmapDescriptorFromRes(context: Context, resId: Int): BitmapDescriptor {
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

        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    activity!!.applicationContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                buildLocationRequest()
                buildLocationCallback()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.isMyLocationEnabled = true
        }

        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        //mMap.setPadding(0, 0, 0, 80)
        mMap.uiSettings.isZoomControlsEnabled = true



        points = ArrayList()
        markers = ArrayList()
        polylines = ArrayList()
        var marker:Marker
        var polyline:Polyline

        // Setting OnClick event listener for the Google Map
        mMap.setOnMapClickListener { point ->

            val client = OkHttpClient()


            val url = "https://maps.googleapis.com/maps/api/elevation/json?locations="+ point.latitude+ "," + point.longitude + "&key=AIzaSyC1M9BVBWwCm2sz_MwgRNorj-W5YHHY3SA"

            val request1 = Request.Builder()
                .url(url)
                .build()

            client.newCall(request1).enqueue(object: Callback {
                override fun onResponse(call: Call?, response: Response?) {
                    val body = response?.body()?.string()


                    val gson = GsonBuilder().create()

                   val res = gson.fromJson(body, results::class.java)



                    activity!!.runOnUiThread {
                        if(res.results[0].elevation<=0) {

                            // Instantiating the class MarkerOptions to plot marker on the map
                            val markerOptions = MarkerOptions()
                                .position(point)  // Setting latitude and longitude of the marker position
                                .icon(
                                    generateBitmapDescriptorFromRes(
                                        activity!!.applicationContext,
                                        R.drawable.ic_veipunkt
                                    )
                                )//Custom marker
                                .flat(true)
                                .anchor(0.5F, 0.5F)


                            val polylineOptions = PolylineOptions()
                                .color(Color.WHITE) // Setting the color of the polyline
                                .jointType(0)
                                .width(8f)// Setting the width of the polyline

                            // Adding the taped point to the ArrayList
                            points.add(point)

                            // Setting points of polyline
                            polylineOptions.addAll(points)


                            // Adding the polyline to the map
                            polyline = mMap.addPolyline(polylineOptions)
                            polylines.add(polyline)

                            // Adding the marker to the map
                            marker = mMap.addMarker(markerOptions)
                            markers.add(marker)
                        }

                        else{
                            Toast.makeText(context,"Punkt må være på havet!",Toast.LENGTH_LONG).show()
                        }
                    }


                }

                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to execute request.")
                }
            })





        }
    }



    data class results(
    val results: List<Result>,
    val status: String
) {
    data class Result(
        val elevation: Double,
        val location: Location,
        val resolution: Double
    ) {
        data class Location(
            val lat: Double,
            val lng: Double
        )
    }
}

}
