package org.seokhwan.weight_record

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.seokhwan.weight_record.databinding.ActivityUserBirthBinding
import org.seokhwan.weight_record.databinding.ActivityUserGenderBinding
import org.seokhwan.weight_record.databinding.ActivityUserLoginBinding
import org.seokhwan.weight_record.databinding.ActivityUserWeightBinding
import java.util.*

class UserLogin : AppCompatActivity() {

    val binding by lazy { ActivityUserLoginBinding.inflate(layoutInflater) }

    lateinit var loginResultListener: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val birthIntent = Intent(this, UserBirth::class.java)
        val genderIntent = Intent(this, UserGender::class.java)
//        val weightIntent = Intent(this,UserWeight::class.java)

        loginResultListener =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    120 -> {
                        val birth = it.data?.getStringExtra("birth")
                        binding.userLoginBirth.text = birth
                    }
                    121 -> {
                        val gender = it.data?.getStringExtra("gender")
                        binding.userLoginGender.text = gender
                    }
////                122 -> {
////                    val weight = it.data?.getStringExtra("weight")
////                    binding.userLoginWeight.text = "${weight}"
//                }
                }
            }

        binding.userLoginBirth.setOnClickListener {
            loginResultListener.launch(birthIntent)
        }
        binding.userLoginGender.setOnClickListener {
            loginResultListener.launch(genderIntent)
        }
//        binding.userLoginWeight.setOnClickListener {
//            loginResultListener.launch(weightIntent)
//        }

        binding.userLoginNextbtn.setOnClickListener {
            val temp = "-"

            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = (calendar.get(Calendar.MONTH) + 1)
            val chartDate = String.format("%02d", month).plus(String.format("%02d", day))

            if (intent.getStringExtra("MainCount") == "초기") {
                if (binding.userLoginBirth.text.toString() != temp &&
                    binding.userLoginGender.text.toString() != temp &&
                    binding.userLoginWeight.text.toString() != temp &&
                    binding.userLoginHeight.text.toString() != temp
                ) {
                    val finishLoginIntent = Intent()
                    finishLoginIntent.putExtra("loginBirth", "${binding.userLoginBirth.text}")
                    finishLoginIntent.putExtra("loginGender", "${binding.userLoginGender.text}")
                    finishLoginIntent.putExtra("loginWeight", "${binding.userLoginWeight.text}")
                    finishLoginIntent.putExtra("loginHeight", "${binding.userLoginHeight.text}")
                    finishLoginIntent.putExtra("chartDate", chartDate)
                    setResult(110, finishLoginIntent)
                    finish()
                } else {
                    Toast.makeText(this, "정보 입력 해주세요.", Toast.LENGTH_SHORT).show()
                }

            } else if (intent.getStringExtra("MainCount") == "수정") {
                if (binding.userLoginBirth.text.toString() != temp &&
                    binding.userLoginGender.text.toString() != temp &&
                    binding.userLoginWeight.text.toString() != temp &&
                    binding.userLoginHeight.text.toString() != temp
                ) {
                    val finishLoginIntent = Intent()
                    finishLoginIntent.putExtra("loginBirth", "${binding.userLoginBirth.text}")
                    finishLoginIntent.putExtra("loginGender", "${binding.userLoginGender.text}")
                    finishLoginIntent.putExtra("loginWeight", "${binding.userLoginWeight.text}")
                    finishLoginIntent.putExtra("loginHeight", "${binding.userLoginHeight.text}")
                    finishLoginIntent.putExtra("chartDate", chartDate)
                    setResult(111, finishLoginIntent)
                    finish()
                }


            }


        }
    }
}