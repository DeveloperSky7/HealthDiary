package org.seokhwan.weight_record

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import org.seokhwan.weight_record.databinding.FragmentFoodmenuRecordRecyclerBinding

class FoodmenuRecordAdapter:RecyclerView.Adapter<FoodmenuHolder>() {
    var list = listOf<Foodmenu>()

    private val TAG = "Firestore"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodmenuHolder {
        val binding = FragmentFoodmenuRecordRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FoodmenuHolder(binding)

    }

    override fun onBindViewHolder(holder: FoodmenuHolder, position: Int) {
        val foodmenu = list.get(position)
        holder.setFoodmenuRecording(foodmenu)

        holder.binding.root.setOnClickListener {
            Log.d(TAG,"foodmenu adapter 클릭 => ${list.get(position)}")
            val intent = Intent(it.context,FoodmenuRecordCustomDialog::class.java)

            intent.putExtra("date",foodmenu.date)
            intent.putExtra("breakfast",foodmenu.breakFast)
            intent.putExtra("lunch",foodmenu.lunch)
            intent.putExtra("dinner",foodmenu.dinner)
            intent.putExtra("snack",foodmenu.snack)

            it.context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}

class FoodmenuHolder(val binding: FragmentFoodmenuRecordRecyclerBinding):RecyclerView.ViewHolder(binding.root){

    fun setFoodmenuRecording(foodmenu: Foodmenu){
        binding.foodmenuRecordRecyclerDate.text = foodmenu.date
        binding.foodmenuRecordRecyclerBreakfast.text = foodmenu.breakFast
        binding.foodmenuRecordRecyclerLunch.text = foodmenu.lunch
        binding.foodmenuRecordRecyclerDinner.text = foodmenu.dinner
        binding.foodmenuRecordRecyclerSnack.text = foodmenu.snack
    }

    init {

    }

}