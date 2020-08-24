package no.uio.IN2000.V19.Gruppe06


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView



//deklarerer en arraylist med elementer, som extender recyclerView.adapter.
class MainAdapter(val ruter: ArrayList<Model>, context: Context?) : RecyclerView.Adapter<MainAdapter.CustomViewHolder>() {



    var onItemClick: ((Model) -> Unit)? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.rute_element, p0, false)
        return CustomViewHolder(cellForRow)
    }


    override fun getItemCount(): Int {
        return ruter.size
    }

    fun removeAt(position: Int) {
        ruter.removeAt(position)
        notifyItemRemoved(position)
    }




    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val element: Model = ruter[position] //fra parameteret "p1"
        holder.textViewRouteName.text = element.title
        holder.textViewRouteLength.text = String.format("%.1f",element.dist) + "km/" + String.format("%.1f",(element.dist*0.539956803)) + "nm"



    }


    inner class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var customItemClickListener: CustomItemClickListener? = null

        val textViewRouteName = itemView.findViewById(R.id.titleTv) as TextView
        val textViewRouteLength = itemView.findViewById(R.id.distTv) as TextView



        init {
            v.setOnClickListener{
                onItemClick?.invoke(ruter[adapterPosition])
            }
        }




        override fun onClick(v: View?) {
            this.customItemClickListener!!.onCustomItemClickListener(v!!, adapterPosition)
        }

    }
}