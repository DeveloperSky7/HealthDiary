package org.seokhwan.weight_record

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.seokhwan.weight_record.databinding.ActivityUserGenderBinding

class UserGender : AppCompatActivity() {

    val binding by lazy { ActivityUserGenderBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var maleSelect = false
        var femaleSelect = false

        binding.userGenderMale.setOnClickListener {
            if (maleSelect == false) {
                binding.userGenderMale.bringToFront()
                binding.userGenderMale.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.gender_button_background_pressed))
                binding.userGenderMale.setTextColor(ContextCompat.getColor(this,R.color.white))
                maleSelect = true

            } else {
                binding.userGenderMale.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.gender_button_background))
                binding.userGenderMale.setTextColor(ContextCompat.getColor(this,R.color.black))
                maleSelect = false
            }
        }

        binding.userGenderFemale.setOnClickListener {
            if (femaleSelect == false) {
                binding.userGenderFemale.bringToFront()
                binding.userGenderFemale.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.gender_button_background_pressed))
                binding.userGenderFemale.setTextColor(ContextCompat.getColor(this,R.color.white))
                femaleSelect = true

            } else {
                binding.userGenderFemale.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.gender_button_background))
                binding.userGenderFemale.setTextColor(ContextCompat.getColor(this,R.color.black))
                femaleSelect = false
            }
        }

        binding.genderNextbtn.setOnClickListener {
            val intent = Intent()
            if (maleSelect == true && femaleSelect == false) {
                intent.putExtra("gender","남성")
                setResult(121,intent)
                finish()
            } else if(femaleSelect == true && maleSelect == false) {
                intent.putExtra("gender","여성")
                setResult(121,intent)
                finish()
            } else if(maleSelect == true && femaleSelect == true){
                Toast.makeText(this,"한가지만 선택해주세요.",Toast.LENGTH_SHORT).show()
            }

        }
    }
}