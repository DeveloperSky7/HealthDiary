package org.seokhwan.weight_record


import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.seokhwan.weight_record.databinding.FragmentInfomationBinding
import java.lang.NumberFormatException
import java.util.*

class InfomationFragment : Fragment() {
    lateinit var binding:FragmentInfomationBinding

    private val TAG = "Firestore"
    val db = FirebaseFirestore.getInstance()

    private var userHeight = ""
    private var userWeight = ""
    private var userBirth = ""
    private var userGender = ""
    private var age = 0

    var check = false

    var resumeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentInfomationBinding.inflate(inflater,container,false)
        return binding.root
    }
    //구성 : firestore 유저 -> uid -> 내부 개인 정보 문서란 정보 받아와서 BMI, BMR
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"InfoFrag onViewCreated")

        CoroutineScope(Dispatchers.IO).launch {
            loadingInfoDb()

            delay(1000)
            while (check == true){
                Log.d(TAG,"InfoFrag recent (코루틴 내부) 메서드 실행 check -> $check")
                recentWeightDb() // 최신 무게 업데이트 하는 메서드
                check = false
            }
        }

//        updateDbChecking()
    }

    override fun onResume() {
        super.onResume()

        resumeCount += 1
        Log.d(TAG,"InfoFrag onResume resumeCount -> $resumeCount")

        if (resumeCount != 1){
            recentWeightDb() // 최신 무게 업데이트 하는 메서드
        }

    }
//    //리사이클러뷰 업데이트시 확인하는 메서드
//    fun updateDbChecking(){
//        Log.d(TAG,"InfoFrag updateDbChecking 메서드 실행중")
//        val userId = getMyId()
//        val updateDb = db.collection("DailyWeight").document(userId).collection("dayWeight")
//        updateDb.addSnapshotListener { snapshot, e ->
//            if (e != null){
//                Log.d(TAG,"foodmenu update 확인중 에러 -> $e")
//                return@addSnapshotListener
//            }
//
//            for (doc in snapshot!!.documentChanges){
//
//            }
//            Log.d(TAG,"InfoFrag update 확인")
//            recentWeightDb()
//
//        }
//
//    }
    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(context?.contentResolver,Settings.Secure.ANDROID_ID)
        return uid
    }
    // 최신 무게 업데이트 하는 메서드
    fun recentWeightDb() {
        Log.d(TAG,"InfoFrag recentWeightDb 실행 , userWeight -> $userWeight")
        val userId = getMyId()
        var countingNum = 0
        val weight = mutableListOf<String>()

        val dayWeightDb = db.collection("DailyWeight").document(userId).collection("dayWeight")
        dayWeightDb.get().addOnSuccessListener { result ->
            for (document in result){
                if (document["weight"] as String != "-"){
                    weight.add(document["weight"] as String)
                    countingNum += 1
                }
                Log.d(TAG,"InfoFrag recentWeightDb 실행, for문 끝 -> weight : $weight / $countingNum")
            }

            if (countingNum != 0){
                val lastWeight = weight.last()
                binding.fragmentInfomationWeight.text = "${lastWeight}kg"
                calculate(lastWeight) //최신 몸무게 기준 bmi, bmr 계산하기
                Log.d(TAG,"InfoFrag calculate 실행전 lastWeight -> $lastWeight")
            } else{
                weight.add(userWeight)
                val lastWeight = weight.last()
                binding.fragmentInfomationWeight.text = "${lastWeight}kg"
                calculate(lastWeight) //최신 몸무게 기준 bmi, bmr 계산하기
                Log.d(TAG,"InfoFrag calculate 실행전 lastWeight -> $lastWeight")

            }

        }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
        }

    }
    //유저란 데이터 불러오기.
    fun loadingInfoDb() {
        Log.d(TAG,"InfoFrag loadingInfoDb 실행")
        val userId = getMyId()

        val userDb = db.collection("User").document(userId)
        userDb.get()
            .addOnSuccessListener { document ->
                if (document.data != null){
                    Log.d(TAG,"InfoFrag loadingInfoDb 데이터 읽는중")
                    userHeight = document["userHeight"]as String
                    binding.fragmentInfomationHeight.text = "${userHeight}cm"
                    userWeight = document["userWeight"]as String
                    binding.fragmentInfomationWeight.text = "${userWeight}kg"
                    userGender = document["userGender"] as String
                    binding.fragmentInfomationGender.text = userGender
                    userBirth = document["userBirth"] as String

                    val birthYear = userBirth.substring(0,4).toInt()
                    val birthMonth = userBirth.substring(5,7).toInt()
                    val birthDay = userBirth.substring(8).toInt()

                    val calendar = Calendar.getInstance()
                    val curYear = calendar.get(Calendar.YEAR)
                    val curMonth = calendar.get(Calendar.MONTH)+1
                    val curDay = calendar.get(Calendar.DAY_OF_MONTH)

                    age = (curYear - birthYear)
                    if ((birthMonth*100)+birthDay > (curMonth*100)+curDay){
                        age -1
                    }
                    binding.fragmentInfomationAge.text = "${age}세(만)"

                    val height = document["userHeight"] as String
                    val heightDouble:Double = height.toDouble()
                    val weight = document["userWeight"] as String
                    val weightDouble:Double = weight.toDouble()

                    val bmi = (weightDouble / ((heightDouble /10.0) * (heightDouble/10.0)) * 100)
                    var bmiInfo = ""
                    if (bmi < 18.5){
                        bmiInfo = "저체중"
                    } else if (bmi > 23.0) {
                        bmiInfo = "과체중"
                    } else if (bmi >= 25) {
                        bmiInfo = "경도 비만"
                    } else if (bmi >= 30 ) {
                        bmiInfo = "중등도 비만"
                    } else {
                        bmiInfo = "정상"
                    }
                    binding.fragmentInfomationBmi.text = "${String.format("%.2f",bmi)}\n$bmiInfo"

                    var bmr = 2000.0

                    if (userGender == "남성"){
                        bmr = (66.47 + (13.75 * weightDouble)) + (5 * heightDouble) - (6.76 * age.toDouble())
                    } else if(userGender == "여성"){
                        bmr = (655.1 + (9.56 * weightDouble)) + (1.85 * heightDouble) - (4.68 * age.toDouble())
                    }

                    binding.fragmentInfomationBmr.text = "${String.format("%.2f",bmr)}"

                    check = true

                    Log.d(TAG,"InfoFrag loadingInfoDb 데이터 읽기 완료")

                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }

    }
    //최신 몸무게 기준 bmi, bmr 계산하기
    fun calculate(weight:String){
        var heightDouble= 0.0
        var weightDouble= 0.0
        try {
            heightDouble = userHeight.toDouble()
            weightDouble = weight.toDouble()
        }catch (e:NumberFormatException){
            weightDouble= userWeight.toDouble()
        }

        Log.d(TAG,"InfoFrag calculate -> userHeight : $userHeight , weight : $weight")

        val bmi = (weightDouble / ((heightDouble /10.0) * (heightDouble/10.0)) * 100)
        var bmiInfo = ""
        if (bmi < 18.5){
            bmiInfo = "저체중"
        } else if (bmi > 23.0) {
            bmiInfo = "과체중"
        } else if (bmi >= 25) {
            bmiInfo = "경도 비만"
        } else if (bmi >= 30 ) {
            bmiInfo = "중등도 비만"
        } else {
            bmiInfo = "정상"
        }
        binding.fragmentInfomationBmi.text = "${String.format("%.2f",bmi)}\n$bmiInfo"

        var bmr = 2000.0

        if (userGender == "남성"){
            bmr = (66.47 + (13.75 * weightDouble)) + (5 * heightDouble) - (6.76 * age.toDouble())
        } else if(userGender == "여성"){
            bmr = (655.1 + (9.56 * weightDouble)) + (1.85 * heightDouble) - (4.68 * age.toDouble())
        }

        binding.fragmentInfomationBmr.text = "${String.format("%.2f",bmr)}"

        Log.d(TAG,"InfoFrag calculate() 메서드 계산 완료")

    }
    //요일 한국어로 바꾸기
    fun getKorDayOfWeek(dayOfWeek:Int):String{
        var korDayOfWeek = ""
        when(dayOfWeek){
            1->{
                korDayOfWeek = "일요일"
            }
            2->{
                korDayOfWeek = "월요일"
            }
            3->{
                korDayOfWeek = "화요일"
            }
            4->{
                korDayOfWeek = "수요일"
            }
            5->{
                korDayOfWeek = "목요일"
            }
            6->{
                korDayOfWeek = "금요일"
            }
            7->{
                korDayOfWeek = "토요일"
            }
        }
        return korDayOfWeek
    }

}


/* greenrobot 라이브러리 구동 코드
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
        Log.d(TAG,"Frag > onStart")
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
        Log.d(TAG,"Frag > onStop")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLog(event: CallEvent){
        Log.d(TAG,"${event.id}")
    }
 */