package org.seokhwan.weight_record

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.seokhwan.weight_record.databinding.ActivityWeightRecordCustomDialogBinding

class WeightRecordCustomDialog : AppCompatActivity() {

    val binding by lazy {ActivityWeightRecordCustomDialogBinding.inflate(layoutInflater)}

    val dailyMenuDb = FirebaseFirestore.getInstance()

    private val TAG = "Firestore"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userId = getMyId()

        val date = intent.getStringExtra("date")
        val weight = intent.getStringExtra("weight")
        val excercise = intent.getBooleanExtra("excercise",false)
        val drink = intent.getBooleanExtra("drink",false)
        val memo = intent.getStringExtra("memo")

        binding.run {
            fragmentWeightRecordDialogTitle.text = date
            fragmentWeightRecordDialogWeight.setText("$weight")
            fragmentWeightRecordDialogExercise.isChecked = excercise
            fragmentWeightRecordDialogDrink.isChecked = drink
            fragmentWeightRecordDialogMemo.setText("$memo")

        }

        val dailyWeightUpdateDb = dailyMenuDb.collection("DailyWeight").document(userId).collection("dayWeight").document("$date")

        binding.fragmentWeightRecordDialogSave.setOnClickListener {
            Log.d(TAG,"weightRecordDialog => 저장 버튼 클릭됨")
            val updateWeight: String?
            val updateMemo:String?

            if (binding.fragmentWeightRecordDialogWeight.text.toString() != ""){
                updateWeight= binding.fragmentWeightRecordDialogWeight.text.toString()
            } else {
                updateWeight = "-"
            }

            if (binding.fragmentWeightRecordDialogMemo.text.toString() != ""){
                updateMemo = binding.fragmentWeightRecordDialogMemo.text.toString()
            }else {
                updateMemo = "-"
            }

            val updateExcercise = binding.fragmentWeightRecordDialogExercise.isChecked
            val updateDrink = binding.fragmentWeightRecordDialogDrink.isChecked

            dailyWeightUpdateDb.update(mapOf("date" to date,"weight" to updateWeight,
            "excercise" to updateExcercise, "drink" to updateDrink, "memo" to updateMemo))
            Log.d(TAG,"weightRecordDialog => 업데이트 완료")

            finish()
        }
        binding.fragmentWeightRecordDialogCancel.setOnClickListener {
            Log.d(TAG,"weightRecordDialog => 취소 버튼 클릭됨")

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