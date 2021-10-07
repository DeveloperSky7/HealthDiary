package org.seokhwan.weight_record

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import org.seokhwan.weight_record.databinding.ActivityMainFragmentDialogBinding
import java.time.LocalDate
import java.util.*

class MainFragmentDialog : AppCompatActivity() {

    val binding by lazy { ActivityMainFragmentDialogBinding.inflate(layoutInflater) }

    private val TAG = "Firestore"

    val targetDb = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userId = getMyId()
        val targetData = targetDb.collection("TargetWeight").document(userId)

        val calendar = Calendar.getInstance()
        val curMonth = (calendar.get(Calendar.MONTH)+1)
        val curDay = calendar.get(Calendar.DAY_OF_MONTH)
        val curYear = calendar.get(Calendar.YEAR)
        val startDate = "${String.format("%02d",curMonth)}${String.format("%02d",curDay)}"

        binding.fragmentMainDialogSave.setOnClickListener {
            val startWeight:String?
            val endWeight:String?

            if (binding.fragmentMainDialogNowWeight.text.toString() != "" &&
                binding.fragmentMainDialogTargetWeight.text.toString() != "" &&
                binding.fragmentMainDialogTargetDay.text.toString() != "") {

                startWeight = binding.fragmentMainDialogNowWeight.text.toString()
                endWeight = binding.fragmentMainDialogTargetWeight.text.toString()
                val day = binding.fragmentMainDialogTargetDay.text.toString()

                val targetDate = day.toInt()
                val targetWeight = (startWeight.toFloat())-(endWeight.toFloat())

                calendar.add(Calendar.DATE, +targetDate)
                val targetMonth = (calendar.get(Calendar.MONTH)+1)
                val targetDay = calendar.get(Calendar.DAY_OF_MONTH)
                val targetYear = calendar.get(Calendar.YEAR)
                val endDate = "${String.format("%02d",targetMonth)}${String.format("%02d",targetDay)}"
                val endFullDate = "$targetYear${String.format("%02d",targetMonth)}${String.format("%02d",targetDay)}"

                val data = hashMapOf("startWeight" to startWeight,"endWeight" to endWeight,"targetDate" to targetDate.toString(),
                    "startDate" to startDate,"endDate" to endDate,"targetWeight" to targetWeight.toString(),"endFullDate" to endFullDate)
                Log.d(TAG,"MainFragmentDialog curMonth: $curMonth, curDay: $curDay")

                targetData.set(data)
                finish()


            } else {
                Toast.makeText(this,"목표 데이터 설정 완료 해주세요.",Toast.LENGTH_LONG).show()
            }
        }

        binding.fragmentMainDialogCancel.setOnClickListener {
            finish()
        }

    }

    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        return uid
    }
}