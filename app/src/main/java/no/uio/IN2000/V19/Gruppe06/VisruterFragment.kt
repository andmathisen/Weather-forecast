package no.uio.IN2000.V19.Gruppe06

import android.content.ContentValues.TAG
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.visruter_fragment.*
import java.util.*




class VisruterFragment : Fragment() {


    private var ruter:ArrayList<Model> = ArrayList()
    private lateinit var koordinater:ArrayList<String>
    private lateinit var punkter:ArrayList<LatLng>
    lateinit var name:String

    var bundle: Bundle? = null
    var dist:Double = 0.0



    companion object {
        fun newInstance() = VisruterFragment()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?{


        return inflater.inflate(R.layout.visruter_fragment,container,false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)


        recView.layoutManager = LinearLayoutManager(activity!!.applicationContext, LinearLayout.VERTICAL,false)

        punkter = ArrayList()

        bundle = arguments
        if(bundle!=null){


            var i = 0
            var latitude : Double
            var longitude : Double
            name = bundle!!.getString("name")
            koordinater = bundle!!.getStringArrayList("koordinater")
            while (i<koordinater.size){
                latitude = koordinater[i].toDouble()
                longitude = koordinater[i+1].toDouble()
                Log.d(TAG,latitude.toString())
                Log.d(TAG,longitude.toString())
                punkter.add(LatLng(latitude,longitude))
                i += 2
            }

            dist = calculateDistance(punkter)
            if(checkDuplicate(name)){
                ruter.add(Model(name,punkter,dist))
            }




        }

        if(ruter.size==0){
            val ingen_ruter= view.findViewById(R.id.ingenRuter) as TextView
            ingen_ruter.visibility = View.VISIBLE
        }


        var mainadapter = MainAdapter(ruter, context)
        recView.adapter  = mainadapter

        val swipeHandler = object : SwipeToDeleteCallback(this.context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recView.adapter as MainAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                mainadapter = adapter

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recView)

        mainadapter.onItemClick = {rute ->

            val ma: MainActivity = activity as MainActivity
            ma.startRuteFragment(rute)

        }



    }


    // Haversine formel for kalkulasjon av distanse mellom punkter
    private fun calculateDistance(punkter:ArrayList<LatLng>):Double{
        var dist = 0.0
        var n = 0
        val R = 6372.8
        while(n<(punkter.size-1)){
            if(n == punkter.size-1){
                break
            }
            val dLat = Math.toRadians(punkter[n+1].latitude - punkter[n].latitude)
            val dLon = Math.toRadians(punkter[n+1].longitude - punkter[n].longitude)
            val originLat = Math.toRadians(punkter[n].latitude)
            val destLat = Math.toRadians(punkter[n+1].latitude)
            val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLon / 2), 2.toDouble()) * Math.cos(originLat) * Math.cos(destLat)
            val c = 2 * Math.asin(Math.sqrt(a))

            dist += (R * c)


            n++
        }

        return dist
    }




}


