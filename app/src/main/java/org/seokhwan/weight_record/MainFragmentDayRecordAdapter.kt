package org.seokhwan.weight_record

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.seokhwan.weight_record.databinding.FragmentMainRecyclerItemBinding
import java.util.*

class MainFragmentDayRecordAdapter:RecyclerView.Adapter<MainFragmentHolder>() {
    var items = mutableListOf<MainFragmentDayRecord>()

    private val TAG = "Firestore"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainFragmentHolder {
        val binding = FragmentMainRecyclerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MainFragmentHolder(binding)
    }

    override fun onBindViewHolder(holder: MainFragmentHolder, position: Int) {
        val dayRecord = items.get(position)
        holder.setList(dayRecord)

        holder.binding.root.setOnClickListener {
            Log.d(TAG,"foodmenu adapter 클릭 => ${items.get(position)}")
            val intent = Intent(it.context,WeightRecordCustomDialog::class.java)

            intent.putExtra("date",dayRecord.date)
            intent.putExtra("weight",dayRecord.weight)
            intent.putExtra("excercise",dayRecord.exercise)
            intent.putExtra("drink",dayRecord.drinking)
            intent.putExtra("memo",dayRecord.memo)

            it.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }
}
class MainFragmentHolder(val binding:FragmentMainRecyclerItemBinding):RecyclerView.ViewHolder(binding.root){

    fun setList(dayRecord:MainFragmentDayRecord){
        binding.fragmentMainRecyclerDate.text = dayRecord.date
        binding.fragmentMainRecyclerWeight.text = "${dayRecord.weight}kg"
        binding.fragmentMainRecyclerExercise.isChecked = dayRecord.exercise
        binding.fragmentMainRecyclerDrinking.isChecked = dayRecord.drinking
        binding.fragmentMainRecyclerMemo.text = dayRecord.memo

    }
}