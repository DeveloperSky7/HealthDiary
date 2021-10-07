package org.seokhwan.weight_record

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.seokhwan.weight_record.databinding.ActivityUserWeightBinding

data class UserWeightData(var weightData:String){}

class UserWeight : AppCompatActivity() {

    val binding by lazy { ActivityUserWeightBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val weightSet = setWeightList()
        val userWeightAdapter = UserWeightAdapter()
        userWeightAdapter.weightList = weightSet

        binding.weightRecyclerview.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                var weight = (recyclerView.layoutManager as LinearLayoutManager)
                    .findFirstVisibleItemPosition()
                binding.weightRecyclerview.smoothScrollToPosition(weight)
                binding.weightText.text = "${weight+33}"
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var weight = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                binding.weightText.text = "${weight+33}"
            }
        })

        binding.weightRecyclerview.adapter = userWeightAdapter

        binding.weightRecyclerview.layoutManager =
            LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.HORIZONTAL}

        binding.weightNextbtn.setOnClickListener {
            val intent = Intent()
            intent.putExtra("weight","${binding.weightText.text}")
            setResult(122,intent)
            finish()
        }

    }

    fun setWeightList():MutableList<UserWeightData>{
        val weightList = mutableListOf<UserWeightData>()
        for (num in 30..130){
            var weightItem = UserWeightData("$num")
            weightList.add(weightItem)
        }
        return weightList
    }
}