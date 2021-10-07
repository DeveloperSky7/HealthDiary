package org.seokhwan.weight_record

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.seokhwan.weight_record.databinding.FragmentFoodmenuRecordDialogBinding

class FoodmenuRecordCustomDialog : AppCompatActivity() {

    private val TAG = "Firestore"

    val dailyMenuDb = FirebaseFirestore.getInstance()

    val binding by lazy { FragmentFoodmenuRecordDialogBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userId = getMyId()

        val date = intent.getStringExtra("date")
        val breakfast = intent.getStringExtra("breakfast")
        val lunch = intent.getStringExtra("lunch")
        val dinner = intent.getStringExtra("dinner")
        val snack = intent.getStringExtra("snack")

        binding.run {
            fragmentFoodmenuRecordDialogTitle.text = date
            fragmentFoodmenuRecordDialogBreakfast.setText("$breakfast")
            fragmentFoodmenuRecordDialogLunch.setText("$lunch")
            fragmentFoodmenuRecordDialogDinner.setText("$dinner")
            fragmentFoodmenuRecordDialogSnack.setText("$snack")
        }

        val dailyMenuUpdateDb = dailyMenuDb.collection("DailyMenu").document(userId).collection("dayFood").document("$date")

        binding.fragmentFoodmenuRecordDialogSave.setOnClickListener {
            Log.d(TAG,"foodmenuDialog => 저장 버튼 클릭됨")
            //null 대비 값 입력
            val updateBreakfast:String?
            val updateLunch:String?
            val updateDinner:String?
            val updateSnack:String?

            if (binding.fragmentFoodmenuRecordDialogBreakfast.text.toString() != ""){
                updateBreakfast = binding.fragmentFoodmenuRecordDialogBreakfast.text.toString()
            } else {
                updateBreakfast = "-"
            }

            if (binding.fragmentFoodmenuRecordDialogLunch.text.toString() != ""){
                updateLunch = binding.fragmentFoodmenuRecordDialogLunch.text.toString()
            } else {
                updateLunch = "-"
            }

            if (binding.fragmentFoodmenuRecordDialogDinner.text.toString() != ""){
                updateDinner = binding.fragmentFoodmenuRecordDialogDinner.text.toString()
            } else {
                updateDinner = "-"
            }

            if (binding.fragmentFoodmenuRecordDialogSnack.text.toString() != ""){
                updateSnack = binding.fragmentFoodmenuRecordDialogSnack.text.toString()
            } else {
                updateSnack = "-"
            }

            dailyMenuUpdateDb.update(mapOf("breakfast" to updateBreakfast,"lunch" to updateLunch,
                "dinner" to updateDinner, "snack" to updateSnack))
            Log.d(TAG,"foodmenuDialog => 업데이트 완료")

            finish()
        }

        binding.fragmentFoodmenuRecordDialogCancel.setOnClickListener {
            Log.d(TAG,"foodmenuDialog => 취소 버튼 클릭됨")
            finish()
        }

    }

    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(this.contentResolver,Settings.Secure.ANDROID_ID)
        return uid
    }
}