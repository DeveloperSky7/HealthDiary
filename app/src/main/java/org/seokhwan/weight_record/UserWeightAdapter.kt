package org.seokhwan.weight_record

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.seokhwan.weight_record.databinding.UserWeightDataViewBinding

class UserWeightAdapter:RecyclerView.Adapter<Holder>() {
    var weightList = mutableListOf<UserWeightData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = UserWeightDataViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)

    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val weight = weightList.get(position)
        holder.setWeight(weight)
    }

    override fun getItemCount(): Int {
        return weightList.size
    }
}
class Holder(val binding: UserWeightDataViewBinding) :RecyclerView.ViewHolder(binding.root){
    fun setWeight(weight:UserWeightData){
        binding.userWeightDataTextview.text = weight.weightData
    }
}