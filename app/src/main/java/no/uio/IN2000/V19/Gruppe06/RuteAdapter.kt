package no.uio.IN2000.V19.Gruppe06


import android.content.Context

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask

import android.support.v7.widget.CardView

import android.util.Log

import android.widget.LinearLayout

import kotlin.collections.ArrayList


class RuteAdapter( val punkter: ArrayList<LatLng>,val punktData:ArrayList<RuteFragment.weatherdata>, val punktOceanData:ArrayList<RuteFragment.OceanF>,var tid:String,val fart:Double,context: Context):RecyclerView.Adapter<RuteAdapter.ViewHolder>() {



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val punkt = punktData[position]
        val punktOc = punktOceanData[position]

        if(punkt != null && punktOc!=null) {

            val dist: Double
            val nytid: Double

            if (position != 0) { // Alle punkter bortsett fra 1. vil få ny tid basert på distansen mellom det gjeldene punktet og forrige, og farten
                dist = getDist(punkter[position], punkter[position - 1])
                nytid = dist / fart

                tid = newTime(nytid)
            }


            var locationForecast: RuteFragment.Location = punkt.product.time[0].location
            var oceanForecast: RuteFragment.OceanF.MoxForecast? = if(punktOc.moxForecast != null) punktOc.moxForecast[0] else null
            var secData: RuteFragment.Time = punkt.product.time[1]


            if(punktOc.moxForecast != null) {

                for (forecast in punktOc.moxForecast) {

                    if ((forecast.metnoOceanForecast.moxValidTime.gmlTimePeriod.gmlBegin == tid) && (forecast.metnoOceanForecast.moxValidTime.gmlTimePeriod.gmlEnd == tid)) {
                        oceanForecast = forecast
                        break
                    }
                }
            }


            for (time in punkt.product.time) { // Finner starttid som matcher det brukeren har valgt
                if ((time.from == tid) && time.location.symbol != null) {
                    secData = time
                }
                if ((time.from == tid) && (time.to == tid)) {
                    locationForecast = time.location
                    break
                }
            }



            holder.temp.text = locationForecast.temperature.value + "°C"
            holder.wind.text = locationForecast.windSpeed.mps + "mps(" + locationForecast.windSpeed.name + ")"


            holder.waveHeight.text =
                if (oceanForecast?.metnoOceanForecast!!.moxSignificantTotalWaveHeight != null) oceanForecast.metnoOceanForecast.moxSignificantTotalWaveHeight.content + "m" else " -----"


            val symbolNr = secData.location.symbol.number

            DownloadImageTask(holder.symbol)
                .execute("https://in2000-apiproxy.ifi.uio.no/weatherapi/weathericon/1.1/?symbol=" + symbolNr + "&content_type=image/png")



            holder.itemView.setOnClickListener { v ->
                // Utvider itemviewet "on click"

                val synlig = !punktData[position].visible

                if (synlig) {
                    holder.subdata.visibility = View.VISIBLE
                    val params = holder.cardview.layoutParams
                    params.height = dpToPx(130)
                    holder.cardview.layoutParams = params
                    holder.vannTempTitle.text = "Vanntemp:"
                    holder.tåkeTitle.text = "Tåke:"
                    holder.nedbørTitle.text = "Nedbør:"
                    holder.vindretningTitle.text = "Vindretning:"
                    holder.nedbør.text =
                        if (secData.location.precipitation != null) secData.location.precipitation.value + "mm" else "---"
                    holder.tåke.text = if (locationForecast.fog != null) locationForecast.fog.percent + "%" else "---"
                    holder.vannTemp.text =
                        if (oceanForecast?.metnoOceanForecast!!.moxseaTemperature != null) oceanForecast.metnoOceanForecast.moxseaTemperature.content + "°C" else "---"
                    holder.vindretning.text =
                        if (locationForecast.windDirection != null) locationForecast.windDirection.name else "---"
                } else {
                    holder.subdata.visibility = View.GONE
                    val params = holder.cardview.layoutParams
                    params.height = dpToPx(75)
                    holder.cardview.layoutParams = params
                }


                punktData[position].visible = synlig

            }
        }


    }



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.punkt_element, p0, false)
        return ViewHolder(cellForRow)

    }

    fun getDist(punkt1:LatLng,punkt2:LatLng):Double{ // Returnerer distansen mellom to koordinater
        var dist = 0.0
        val R = 6372.8

        val dLat = Math.toRadians(punkt2.latitude - punkt1.latitude)
        val dLon = Math.toRadians(punkt2.longitude - punkt1.longitude)
        val originLat = Math.toRadians(punkt1.latitude)
        val destLat = Math.toRadians(punkt2.latitude)
        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLon / 2), 2.toDouble()) * Math.cos(originLat) * Math.cos(destLat)
        val c = 2 * Math.asin(Math.sqrt(a))

        dist = (R * c)



        return dist

    }


    fun addItem(wdata: RuteFragment.weatherdata) {
        punktData.add(wdata)
    }

    fun addOceanItem(odata: RuteFragment.OceanF) {
        punktOceanData.add(odata)
    }

    fun getLocationData():ArrayList<RuteFragment.weatherdata>{
        return punktData
    }
    fun getOceanData():ArrayList<RuteFragment.OceanF>{
        return punktOceanData
    }

    fun dpToPx(dp: Int): Int { // Metode som gir riktig dp(density independent pixels) utifra størrelsen på skjermen

        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }


    fun newTime(nytid: Double):String{//Metode for å få riktig tidspunkt man når et punkt utifra fart * distanse

        var n = nytid

        var retTid:String = tid

        if(nytid < 0.5){
            return tid
        }
        while(n>0){

            retTid = tid

            var h = retTid.substring(11,13).toInt() + 1
            if(h>24){
                h = 1
                val hString = "0" + h.toString()
                val dag = retTid.substring(8,10).toInt() + 1
                val maaned = retTid.substring(5,7).toInt()
                if((dag > 31) || (dag>30 && maaned%2==0 && maaned < 7) || (dag>30 && maaned%2!=0 && maaned>8)){
                    maaned + 1
                }
                var maanedString = maaned.toString()
                var dagString = dag.toString()
                if(maaned<10){
                    maanedString = "0$maaned"
                }
                if(dag < 10 ){
                    dagString = "0$dag"
                }

                retTid = retTid.substring(0,4) + "-" + maanedString + "-" + dagString + "T" + hString + retTid.substring(13,20)

            }
            else {
                var hString = h.toString()

                if (h < 10) {
                    hString = "0" + h.toString()

                }

                retTid = retTid.substring(0, 11) + hString + retTid.substring(13, 20)

            }
            tid = retTid

            n -= 1
        }

        return retTid
    }





    override fun getItemCount(): Int {
        return punktData.size
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {



        val temp = itemView.findViewById(R.id.temp) as TextView
        val waveHeight = itemView.findViewById(R.id.waveHeight) as TextView
        val wind = itemView.findViewById(R.id.wind) as TextView
        val symbol = itemView.findViewById<ImageView>(R.id.symbol)
        val subdata = itemView.findViewById(R.id.sub_data) as LinearLayout
        val vannTemp = itemView.findViewById(R.id.vannTemp) as TextView
        val nedbør = itemView.findViewById(R.id.nedbør) as TextView
        val vindretning = itemView.findViewById(R.id.vindretning) as TextView
        val tåke = itemView.findViewById(R.id.tåke) as TextView
        val cardview = itemView.findViewById(R.id.cardview) as CardView
        val vannTempTitle = itemView.findViewById(R.id.vtempTitle) as TextView
        val tåkeTitle = itemView.findViewById(R.id.tåkeTitle) as TextView
        val nedbørTitle = itemView.findViewById(R.id.nedbørTitle) as TextView
        val vindretningTitle = itemView.findViewById(R.id.vindretningTitle) as TextView


    }

}

private class DownloadImageTask(internal var bmImage: ImageView) : AsyncTask<String, Void, Bitmap>() { // Metode for å hente riktig vær symbol

    override fun doInBackground(vararg urls: String): Bitmap? {
        val urldisplay = urls[0]
        var symbol: Bitmap? = null
        try {
            val input = java.net.URL(urldisplay).openStream()
            symbol = BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }

        return symbol
    }

    override fun onPostExecute(result: Bitmap) {
        bmImage.setImageBitmap(result)
    }
}
