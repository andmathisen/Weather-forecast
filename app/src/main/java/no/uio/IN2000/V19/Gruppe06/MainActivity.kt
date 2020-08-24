package no.uio.IN2000.V19.Gruppe06




import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng







class MainActivity : AppCompatActivity() {

    private var nyRuteFragment:Fragment = NyRuteFragment()
    private var mapsFragment:Fragment = MapsFragment()
    private var visruterFragment:Fragment = VisruterFragment()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        supportFragmentManager.inTransaction {
            add(R.id.fragment_container, visruterFragment,"vis rute")
            add(R.id.fragment_container, nyRuteFragment,"ny rute")
            add(R.id.fragment_container,mapsFragment,"map")
        }


        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navigation.setOnNavigationItemSelectedListener(navListener)

    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    fun sendInfo(name : String, punkter : ArrayList<LatLng>) {
        val koordinater : ArrayList<String> = ArrayList()
        for (latlng:LatLng in punkter){
            koordinater.add(latlng.latitude.toString())
            koordinater.add(latlng.longitude.toString())

        }
        val fm : FragmentManager = supportFragmentManager

        val ft : FragmentTransaction = fm.beginTransaction()

        val bundle = Bundle()
        bundle.putString("name",name)
        bundle.putStringArrayList("koordinater",koordinater)
        visruterFragment.arguments = bundle

        ft.replace(R.id.fragment_container, visruterFragment)
        ft.commit()
    }

    fun sendInfoMap(name : String, punkter : ArrayList<LatLng>) {
        val koordinater : ArrayList<String> = ArrayList()
        for (latlng:LatLng in punkter){
            koordinater.add(latlng.latitude.toString())
            koordinater.add(latlng.longitude.toString())

        }
        val fm : FragmentManager = supportFragmentManager

        val ft : FragmentTransaction = fm.beginTransaction()

        val bundle = Bundle()
        bundle.putString("name",name)
        bundle.putStringArrayList("koordinater",koordinater)
        mapsFragment.arguments = bundle
        ft.replace(R.id.fragment_container, mapsFragment)
        ft.commit()

    }

    fun startRuteFragment(rute:Model){
        val ruteFragment = RuteFragment()
        val koordinater : ArrayList<String> = ArrayList()
        for (latlng:LatLng in rute.punkter){
            koordinater.add(latlng.latitude.toString())
            koordinater.add(latlng.longitude.toString())

        }
        val fm : FragmentManager = supportFragmentManager

        val ft : FragmentTransaction = fm.beginTransaction()

        val bundle = Bundle()
        bundle.putString("name",rute.title)
        bundle.putStringArrayList("koordinater",koordinater)
        bundle.putDouble("dist",rute.dist)
        ruteFragment.arguments = bundle

        ft.replace(R.id.fragment_container, ruteFragment)
        ft.commit()

    }



    private val navListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {

            when(item.itemId){
                R.id.ny_rute -> {


                    val text = "Trykk og hold for å legge til veipunkt"
                    val duration = Toast.LENGTH_LONG

                    val toast = Toast.makeText(applicationContext, text, duration)

                    for (i in 0..1) {
                        toast.show()
                    }

                    supportFragmentManager.inTransaction {
                        replace(R.id.fragment_container, nyRuteFragment,"ny rute")


                    }
                    visruterFragment.arguments = null

                    return true

                }
                R.id.hjem ->{


                   supportFragmentManager.inTransaction {
                        replace(R.id.fragment_container, mapsFragment,"map")



                    }
                    visruterFragment.arguments = null



                    return true
                }
                R.id.mine_ruter ->{


                    supportFragmentManager.inTransaction {
                        replace(R.id.fragment_container, visruterFragment)

                    }



                    return true
                }

            }

            return true
        }
    }

}