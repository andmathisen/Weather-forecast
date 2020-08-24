package no.uio.IN2000.V19.Gruppe06

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog

import android.content.Context
import android.graphics.Color

import android.graphics.drawable.ColorDrawable

import android.os.Bundle

import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_rute.*
import okhttp3.*
import java.io.IOException
import com.google.gson.GsonBuilder
import android.support.v7.widget.SimpleItemAnimator

import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.annotations.SerializedName

import java.util.*

import kotlin.collections.ArrayList


class RuteFragment : Fragment() {

    lateinit var koordinater: ArrayList<String>
    private lateinit var punkter: ArrayList<LatLng>
    lateinit var name:String
    var bundle: Bundle? = null
    var dist:Double = 0.0
    lateinit var data:ArrayList<weatherdata>
    lateinit var oceandata:ArrayList<OceanF>
    lateinit var ruteadapter:RuteAdapter

    lateinit var mDateSetListener:DatePickerDialog.OnDateSetListener
    lateinit var mTimeSetListener:TimePickerDialog.OnTimeSetListener
    val cal:Calendar = Calendar.getInstance()

    var year:Int = cal.get(Calendar.YEAR)
    var month:Int = cal.get(Calendar.MONTH)+1
    var day:Int = cal.get(Calendar.DAY_OF_MONTH)
    var hour:Int = cal.get(Calendar.HOUR_OF_DAY)
    var min:Int = cal.get(Calendar.MINUTE)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        return inflater.inflate(R.layout.fragment_rute, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(activity!!.applicationContext, LinearLayout.VERTICAL, false)
        punkter = ArrayList()
        data = ArrayList()
        oceandata = ArrayList()
        val datoTekst:TextView = view.findViewById(R.id.datoTekst)
        var måned:String = month.toString()
        if(month<10) måned = "0" + måned
        var dag:String = day.toString()
        if(day<10) dag = "0" + dag
        datoTekst.text = dag + "." + måned + "." + year.toString()
        val tidTekst:TextView = view.findViewById(R.id.tidTekst)
        var time:String = hour.toString()
        if(hour <10) time ="0" + time
        var minut:String = min.toString()
        if(min<10) minut = "0" + minut
        tidTekst.text =  time + ":" + minut

        val timeString = createTimeString(datoTekst.text.toString(),tidTekst.text.toString())

        bundle = arguments
        if (bundle != null) {

            var i = 0
            var latitude: Double
            var longitude: Double
            name = bundle!!.getString("name")
            koordinater = bundle!!.getStringArrayList("koordinater")
            dist = bundle!!.getDouble("dist")
            while (i < koordinater.size) {
                latitude = koordinater[i].toDouble()
                longitude = koordinater[i + 1].toDouble()
                punkter.add(LatLng(latitude, longitude))
                i += 2
            }

        }



        for (punkt in punkter) {
            getWeather(punkt.latitude, punkt.longitude)
        }

        ruteadapter = RuteAdapter(punkter,data, oceandata,timeString, 5.0,context!!)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        recyclerView.adapter = ruteadapter

        val nameText: TextView = view.findViewById(R.id.name)
        nameText.text = name
        val distText: TextView = view.findViewById(R.id.dist)
        distText.text = String.format("%.1f", dist) + " km/" + String.format("%.1f", (dist * 0.539956803)) + " nautisk mil"

        recyclerView.setHasFixedSize(true)


        val dato = view.findViewById(R.id.dato) as ImageButton

        dato.setOnClickListener{


             year = cal.get(Calendar.YEAR)
             month = cal.get(Calendar.MONTH)+1
             day = cal.get(Calendar.DAY_OF_MONTH)


            val dialog = DatePickerDialog(
                context,
                R.style.AlertDialog_theme,
                mDateSetListener,
                year,month,day)

            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val now = System.currentTimeMillis() - 1000


            dialog.datePicker.minDate = now
            dialog.datePicker.maxDate = now+(1000*60*60*24*5) //Setter max dato fordi det er så langt fram man kan hente oceanforecast data.

            dialog.show()
        }

        mDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val correctMonth = month + 1 // Måneder går fra 0 til 11 så man må addere med 1 for å få riktig
            var måned:String = correctMonth.toString()
            if(correctMonth<10) måned = "0" + måned
            var dag:String = dayOfMonth.toString()
            if(dayOfMonth<10) dag = "0" + dag

            datoTekst.text = dag + "." + måned + "." + year.toString()

             hour = cal.get(Calendar.HOUR_OF_DAY)
             min = cal.get(Calendar.MINUTE)
            val timeDialog = TimePickerDialog(context,
                R.style.AlertDialog_theme,
                mTimeSetListener,
                hour,min,true
            )

            timeDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            timeDialog.show()
        }

        mTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

            var time:String = hourOfDay.toString()
            if(hourOfDay <10) time ="0" + time
            var minut:String = minute.toString()
            if(minute<10) minut = "0" + minut
            tidTekst.text =  time + ":" + minut

            val p1 = ruteadapter.getLocationData()
            val p2 = ruteadapter.punktOceanData

            Log.d("Date",datoTekst.text.toString())
            Log.d("Tid",tidTekst.text.toString())
            recyclerView.adapter = RuteAdapter(punkter,p1, p2,createTimeString(datoTekst.text.toString(),tidTekst.text.toString()), 5.0,context!!)


        }

        val naa = view.findViewById(R.id.naa_knapp) as Button

        naa.setOnClickListener {
            year = cal.get(Calendar.YEAR)
            month = cal.get(Calendar.MONTH) + 1
            day = cal.get(Calendar.DAY_OF_MONTH)
            hour = cal.get(Calendar.HOUR_OF_DAY)
            min = cal.get(Calendar.MINUTE)

            var måned:String = month.toString()
            if(month<10) måned = "0" + måned
            var dag:String = day.toString()
            if(day<10) dag = "0" + dag

            var time:String = hour.toString()
            if(hour <10) time ="0" + time
            var minut:String = min.toString()
            if(min<10) minut = "0" + minut



            datoTekst.text = dag + "." + måned + "." + year.toString()
            tidTekst.text = time + ":" + minut

            val p1 = ruteadapter.getLocationData()
            val p2 = ruteadapter.getOceanData()

            recyclerView.adapter = RuteAdapter(punkter,p1, p2,createTimeString(datoTekst.text.toString(),tidTekst.text.toString()), 5.0,context!!)

        }

        val f = view.findViewById(R.id.fart) as TextView

       f.setOnClickListener {
           val p1 = ruteadapter.getLocationData()
           val p2 = ruteadapter.getOceanData()
           recyclerView.adapter = RuteAdapter(punkter,p1, p2,createTimeString(datoTekst.text.toString(),tidTekst.text.toString()), f.text.toString().toDouble(),context!!)
           hideKeyboard()
       }


    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun createTimeString(date:String,time:String):String{
        val min = time.substring(3,5).toInt()
        var hour = time.substring(0,2).toInt()
        var hourString:String = hour.toString()

        if(min>=30) hour += 1
        if(hour<10) hourString = "0$hour"
        Log.d("String Create", date.substring(6,10) + "-" + date.substring(3,5) + "-" + date.substring(0,2) + "T" + hourString + ":" + "00" +":"+"00" + "Z")
        return (date.substring(6,10) + "-" + date.substring(3,5) + "-" + date.substring(0,2) + "T" + hourString + ":" + "00" +":"+"00" + "Z") // Format string same way as in weather json
    }

    private fun createForecastUrl(lat: Double, lon: Double) : String
    {

        val url = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/1.9/.json?lat=" + lat + "&lon="+ lon
        return url
    }

    private fun createOceanUrl(lat: Double, lon: Double) : String
    {

        val url = "https://in2000-apiproxy.ifi.uio.no/weatherapi/oceanforecast/0.9/.json?lat=" + lat + "&lon="+lon
        return url
    }


    private fun getWeather(lat: Double, lon: Double){
        val getUrl1 = createForecastUrl(lat, lon)
        val getUrl2 = createOceanUrl(lat, lon)


        val client = OkHttpClient()

        val request1 = Request.Builder()
            .url(getUrl1)
            .build()

        val request2 = Request.Builder()
            .url(getUrl2)
            .build()


        client.newCall(request1).enqueue(object: Callback
        {
            override fun onResponse(call: Call?, response: Response?){
                val body = response?.body()?.string()


                val gson = GsonBuilder().create()

                val wdata = gson.fromJson(body,weatherdata::class.java)


                activity!!.runOnUiThread {
                    ruteadapter.addItem(wdata)

                    recyclerView.adapter = ruteadapter
                }

                response?.close()


            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request.")
            }


        })


        client.newCall(request2).enqueue(object: Callback
        {
            override fun onResponse(call: Call?, response: Response?){
                val body = response?.body()?.string()


                val gson = GsonBuilder().create()

                val odata = gson.fromJson(body,OceanF::class.java)



                activity!!.runOnUiThread {
                    ruteadapter.addOceanItem(odata)

                    recyclerView.adapter = ruteadapter
                }

                response?.close()


            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request.")

            }

        })




    }


    data class weatherdata(
        var visible:Boolean = false,
        val created: String,
        val meta: Meta,
        val product: Product
    )

    data class Meta(
        val model: List<Model>
    )

    data class Model(
        val from: String,
        val name: String,
        val nextrun: String,
        val runended: String,
        val termin: String,
        val to: String
    )

    data class Product(
        val cl: String,
        val time: List<Time>
    )

    data class Time(
        val datatype: String,
        val from: String,
        val location: Location,
        val to: String
    )

    data class Location(
        val mediumClouds:MediumClouds,
        val fog:Fog,
        val windProbability:WindProbability,
        val dewpointTemperature:DewPointTemperature,
        val temperatureProbability: TemperatureProbability,
        val windDirection:WindDirection,
        val cloudiness:Cloudiness,
        val windGust:WindGust,
        val pressure:Pressure,
        val lowClouds:LowClouds,
        val highClouds:HighClouds,
        val windSpeed:WindSpeed,
        val humidity:Humidity,
        val areaMaxWindSpeed:AreaMaxWindSpeed,
        val altitude: String,
        val latitude: String,
        val longitude: String,
        val temperature: temperature,
        val maxTemperature: MaxTemperature,
        val minTemperature: MinTemperature,
        val precipitation: Precipitation,
        val symbol: Symbol,
        val symbolProbability: SymbolProbability

    )

    data class HighClouds(
        val percent: String,
        val id: String
    )

    data class AreaMaxWindSpeed(
        val mps: String
    )

    data class Humidity(
        val value: String,
        val unit: String
    )

    data class temperature(
        val id: String,
        val value: String,
        val unit: String
    )

    data class WindGust(
        val mps: String,
        val id:String
    )

    data class WindSpeed(
        val id: String,
        val beaufort:String,
        val mps:String,
        val name:String
    )

    data class LowClouds(
        val id: String,
        val percent: String
    )

    data class Pressure(
        val value: String,
        val unit: String,
        val id: String
    )

    data class WindDirection(
        val id:String,
        val deg:String,
        val name:String
    )

    data class Cloudiness(
        val id:String,
        val percent:String
    )

    data class TemperatureProbability(
        val unit:String,
        val value:String
    )

    data class DewPointTemperature(
        val unit:String,
        val value:String,
        val id:String
    )

    data class WindProbability(
        val unit:String,
        val value:String
    )

    data class MediumClouds(
        val percent:String,
        val id:String
    )

    data class Fog(
        val percent:String,
        val id:String
    )

    data class Precipitation(
        val unit: String,
        val value: String
    )

    data class Symbol(
        val id: String,
        val number: String
    )

    data class MaxTemperature(
        val id: String,
        val unit: String,
        val value: String
    )

    data class SymbolProbability(
        val unit: String,
        val value: String
    )

    data class MinTemperature(
        val value: String,
        val id: String,
        val unit: String

    )

    data class OceanF(
        @SerializedName("gml:description")
        val gmlDescription: String,
        @SerializedName("gml:id")
        val gmlId: String,
        @SerializedName("mox:forecast")
        val moxForecast: List<MoxForecast>,
        @SerializedName("mox:forecastPoint")
        val moxForecastPoint: MoxForecastPoint,
        @SerializedName("mox:issueTime")
        val moxIssueTime: MoxIssueTime,
        @SerializedName("mox:nextIssueTime")
        val moxNextIssueTime: MoxNextIssueTime,
        @SerializedName("mox:observedProperty")
        val moxObservedProperty: MoxObservedProperty,
        @SerializedName("mox:procedure")
        val moxProcedure: MoxProcedure,
        @SerializedName("xmlns:gml")
        val xmlnsGml: String,
        @SerializedName("xmlns:metno")
        val xmlnsMetno: String,
        @SerializedName("xmlns:mox")
        val xmlnsMox: String,
        @SerializedName("xmlns:xlink")
        val xmlnsXlink: String,
        @SerializedName("xsi:schemaLocation")
        val xsiSchemaLocation: String
) {
    data class MoxObservedProperty(
        @SerializedName("xlink:href")
        val xlinkHref: String
    )

    data class MoxForecast(
        @SerializedName("metno:OceanForecast")
        val metnoOceanForecast: MetnoOceanForecast
    ) {
        data class MetnoOceanForecast(
            @SerializedName("gml:id")
            val gmlId: String,
            @SerializedName("mox:seaTemperature")
            val moxseaTemperature: MoxseaTemperature,
            @SerializedName("mox:meanTotalWaveDirection")
            val moxMeanTotalWaveDirection: MoxMeanTotalWaveDirection,
            @SerializedName("mox:seaBottomTopography")
            val moxSeaBottomTopography: MoxSeaBottomTopography,
            @SerializedName("mox:significantTotalWaveHeight")
            val moxSignificantTotalWaveHeight: MoxSignificantTotalWaveHeight,
            @SerializedName("mox:validTime")
            val moxValidTime: MoxValidTime
        ) {
            data class MoxSignificantTotalWaveHeight(
                val content: String,
                val uom: String
            )

            data class MoxSeaBottomTopography(
                val content: String,
                val uom: String
            )

            data class MoxseaTemperature(
                val content: String,
                val uom: String
            )

            data class MoxValidTime(
                @SerializedName("gml:TimePeriod")
                val gmlTimePeriod: GmlTimePeriod
            ) {
                data class GmlTimePeriod(
                    @SerializedName("gml:begin")
                    val gmlBegin: String,
                    @SerializedName("gml:end")
                    val gmlEnd: String,
                    @SerializedName("gml:id")
                    val gmlId: String
                )
            }

            data class MoxMeanTotalWaveDirection(
                val content: String,
                val uom: String
            )
        }
    }

    data class MoxProcedure(
        @SerializedName("xlink:href")
        val xlinkHref: String
    )

    data class MoxIssueTime(
        @SerializedName("gml:TimeInstant")
        val gmlTimeInstant: GmlTimeInstant
    ) {
        data class GmlTimeInstant(
            @SerializedName("gml:id")
            val gmlId: String,
            @SerializedName("gml:timePosition")
            val gmlTimePosition: String
        )
    }

    data class MoxForecastPoint(
        @SerializedName("gml:Point")
        val gmlPoint: GmlPoint
    ) {
        data class GmlPoint(
            @SerializedName("gml:id")
            val gmlId: String,
            @SerializedName("gml:pos")
            val gmlPos: String,
            val srsName: String
        )
    }

    data class MoxNextIssueTime(
        @SerializedName("gml:TimeInstant")
        val gmlTimeInstant: GmlTimeInstant
    ) {
        data class GmlTimeInstant(
            @SerializedName("gml:id")
            val gmlId: String,
            @SerializedName("gml:timePosition")
            val gmlTimePosition: String
        )
    }
}
}
