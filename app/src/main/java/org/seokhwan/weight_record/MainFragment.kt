package org.seokhwan.weight_record

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import org.seokhwan.weight_record.databinding.FragmentMainBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

data class MainFragmentDayRecord
    (var date:String, var weight:String, var exercise:Boolean, var drinking:Boolean,var memo:String )

class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding

    private val TAG = "Firestore"
    val dailyWeightDb = FirebaseFirestore.getInstance()
    val targetDb = FirebaseFirestore.getInstance()
    val dateDb = FirebaseFirestore.getInstance()

    val dayWeight = mutableListOf<MainFragmentDayRecord>()
    val fragmentAdapter = MainFragmentDayRecordAdapter()

    var check = false
    var resumeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG,"MainFragment onViewCreated")

        binding.fragmentMainLoadingProgress.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            checkWeightDb() //db 확인 후 없으면 초기화 진행 메서드
            delay(1000)
            while (check == true){
                Log.d(TAG,"MainFragment loading 메서드 실행, check -> $check")
                dayWeight.clear()
                loadingWeightDb() //리사이클러뷰 데이터 불러오는 메서드
                loadingTargetDb() //목표란 데이터 불러오기
                check = false
            }
        }

//        CoroutineScope(Dispatchers.Main).launch {
//            val check = launch {
//                checkWeightDb()
//            }.join()
//
//            withContext(Dispatchers.IO){
//                Log.d(TAG,"MainFragment loading 메서드 실행, check -> $check")
//                dayWeight.clear()
//                loadingWeightDb()
//                loadingTargetDb()
//
//            }
//        }

        binding.fragmentMainRecyclerview.adapter = fragmentAdapter

        val manager = LinearLayoutManager(context)
        manager.reverseLayout = true
        manager.stackFromEnd = true

        binding.fragmentMainRecyclerview.layoutManager = manager

//        updateDbChecking() //리사이클러뷰 업데이트시 확인하는 메서드
        // 목표 생성시 다이얼로그 호출
        binding.fragmentMainTargetAdd.setOnClickListener {
            val intent = Intent(context,MainFragmentDialog::class.java)
            startActivity(intent)
        }
        //목표 삭제 버튼 클릭시 데이터 삭제 코드
        binding.fragmentMainTargetDelete.setOnClickListener {
            val userId = getMyId()

            binding.run {
                fragmentMainTargetStartDate.text = "-"
                fragmentMainTargetFinishDate.text = "-"
                fragmentMainTargetStartWeight.text = "-"
                fragmentMainTargetFinishWeight.text = "-"
                fragmentMainTargetChangeRateDate.progress = 0
                fragmentMainTargetChangeRateWeight.progress = 0
                fragmentMainTargetChangeRateTextDate.text = "0%"
                fragmentMainTargetChangeRateTextWeight.text = "0%"
            }

            val targetData = targetDb.collection("TargetWeight").document(userId)
            targetData.delete().addOnSuccessListener {
                Log.d(TAG,"MainFragment 목표 데이터 삭제")
            }.addOnFailureListener { exception ->
                Log.d(TAG,"exception -> $exception")
            }
        }

    }
    //재실행시 실행 매서드
    override fun onResume() {
        super.onResume()

        binding.fragmentMainLoadingProgress.visibility = View.VISIBLE

        resumeCount += 1
        //초기 화면 구성시 동작 x , 이후 재실행시 아래 메서드 실행
        if (resumeCount != 1){
            dayWeight.clear()
            Log.d(TAG,"MainFragment list 클리어")
            loadingWeightDb() //리사이클러뷰 데이터 불러오는 메서드
            loadingTargetDb() // 목표란 데이터 불러오기
        }
    }

    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
        return uid
    }

    // 목표란 데이터 불러오기
    fun loadingTargetDb(){
        Log.d(TAG,"MainFragment loadingTargetDb 메서드 실행")
        val userId = getMyId()

        val targetData = targetDb.collection("TargetWeight").document(userId)
        targetData.get().addOnSuccessListener { document ->
            if (document.data != null){
                val startWeight = document["startWeight"] as String
                binding.fragmentMainTargetStartWeight.setText("${startWeight}kg")
                val endWeight = document["endWeight"] as String
                binding.fragmentMainTargetFinishWeight.setText("${endWeight}kg")
                val startDate = document["startDate"] as String
                val start = StringBuilder()
                start.append(startDate)
                start.insert(2,"-")
                binding.fragmentMainTargetStartDate.setText("$start")
                val endDate = document["endDate"] as String
                val end = StringBuilder()
                end.append(endDate)
                end.insert(2,"-")
                binding.fragmentMainTargetFinishDate.setText("$end")

                val endFullDate = document["endFullDate"] as String

                val targetWeight = document["targetWeight"] as String
                val targetDate = document["targetDate"] as String

                calculateTargetWeight(endWeight,targetWeight,startWeight) // 목표 대비 현재 무게 계산
                calculateTargetDate(endFullDate,targetDate) // 목표 대비 현재 날짜 계산

            }else {
                Log.d(TAG,"MainFragment loadingTargetDb 데이터 없음")

            }
        }.addOnFailureListener { exception ->
            Log.d(TAG,"exception : $exception")
        }
        Log.d(TAG,"MainFragment loadingTargetDb 메서드 완료")
    }

    // 목표 대비 현재 날짜 계산
    fun calculateTargetDate(endFullDate:String, targetDate:String){
        Log.d(TAG,"MainFragment calculateTargetDate 메서드 실행, endWeight: $endFullDate, targetWeight: $targetDate")
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
        val endDate = simpleDateFormat.parse(endFullDate).time
        val targetDay = targetDate.toFloat()

        val todayDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time.time

        val calculate = (endDate-todayDate)/(24*60*60*1000)
        if (calculate.toFloat() == targetDay){

            binding.fragmentMainTargetChangeRateDate.progress = 0
            binding.fragmentMainTargetChangeRateTextDate.text = "0%"

        } else{
            val calculateDate = ((targetDay-calculate.toFloat())/(targetDay))*100
            val progress = calculateDate.roundToInt()
            if (100 <= progress){
                binding.fragmentMainTargetChangeRateDate.progress = 100
                binding.fragmentMainTargetChangeRateTextDate.text = "100%"
            } else {
                binding.fragmentMainTargetChangeRateDate.progress = progress
                binding.fragmentMainTargetChangeRateTextDate.text = "${progress}%"
            }

            Log.d(TAG,"MainFragment calculateTargetDate calculate: $calculate, progress: $progress")
        }


    }

    // 목표 대비 현재 무게 계산
    fun calculateTargetWeight(endWeight:String, targetWeight:String,startWeight:String){
        Log.d(TAG,"MainFragment calculateTargetWeight 메서드 실행, endWeight: $endWeight, targetWeight: $targetWeight")
        val endKg = endWeight.toFloat()
        val targetKg = targetWeight.toFloat()

        var counting = 0

        val targetWeightList = mutableListOf<String>()
        val _targetWeightList = mutableListOf<String>()

        val userId = getMyId()
        val targetWeightDb = targetDb.collection("DailyWeight").document(userId).collection("dayWeight")
        targetWeightDb.get().addOnSuccessListener { result ->
            for (document in result){
                if(document["weight"] as String != "-"){
                    targetWeightList.add(document["weight"] as String)
                    counting += 1
                }else{
                    _targetWeightList.add(startWeight)
                }
            }

            if (counting == 0){
                val curKg = (_targetWeightList.last()).toFloat()
                val calProgress =
                    ((targetKg - (curKg - endKg))/targetKg)*100

                val progress = calProgress.roundToInt()
                binding.fragmentMainTargetChangeRateWeight.progress = progress
                binding.fragmentMainTargetChangeRateTextWeight.text = "${progress}%"

            } else {
                val curKg = (targetWeightList.last()).toFloat()
                val calProgress =
                    ((targetKg - (curKg - endKg))/targetKg)*100
                val progress = calProgress.roundToInt()

                if (progress <= 0){
                    binding.fragmentMainTargetChangeRateWeight.progress = 0
                    binding.fragmentMainTargetChangeRateTextWeight.text = "0%"
                } else if (100<= progress){
                    binding.fragmentMainTargetChangeRateWeight.progress = 100
                    binding.fragmentMainTargetChangeRateTextWeight.text = "100%"
                } else {
                    binding.fragmentMainTargetChangeRateWeight.progress = progress
                    binding.fragmentMainTargetChangeRateTextWeight.text = "${progress}%"
                }



            }

        }.addOnFailureListener { e ->
            Log.d(TAG,"exception : $e")
        }
    }

//    리사이클러뷰 업데이트시 확인하는 메서드
//    fun updateDbChecking(){
//        Log.d(TAG,"Mainfragment updateDbChecking 메서드 실행중")
//        val userId = getMyId()
//        val updateDb = dailyWeightDb.collection("DailyWeight").document(userId).collection("dayWeight")
//        updateDb.addSnapshotListener { snapshot, e ->
//            if (e != null){
//                Log.d(TAG,"Mainfragment update 확인중 에러 -> $e")
//                return@addSnapshotListener
//            }
//
//            for (doc in snapshot!!.documentChanges){
//
//            }
//            Log.d(TAG,"Mainfragment update 확인")
//        }
//    }

    //리사이클러뷰 데이터 불러오는 메서드
    fun loadingWeightDb() {
        Log.d(TAG,"MainFragment dayWeightDb 읽기 시작")
        val userId = getMyId()

        val dayWeightDb = dailyWeightDb.collection("DailyWeight").document(userId).collection("dayWeight")
        dayWeightDb.get().addOnSuccessListener { result ->
            for (document in result){
                val day = MainFragmentDayRecord(document["date"] as String,document["weight"] as String, document["excercise"] as Boolean,
                document["drink"] as Boolean, document["memo"] as String)
                dayWeight.add(day)

                fragmentAdapter.items = dayWeight
            }
            binding.fragmentMainLoadingProgress.visibility = View.GONE
            fragmentAdapter.notifyDataSetChanged()
            Log.d(TAG,"MainFragment dayWeightDb 읽기 완료, 리사이클러뷰 업데이트")
        }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }
    }

/*    // 매일 db 업데이트 메서드
    fun updateWeightDb(){
        Log.d(TAG,"MainFragment updateWeightDb 실행")
        val userId = getMyId()
        val calendar = Calendar.getInstance()

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = (calendar.get(Calendar.MONTH)+1)
        val year = calendar.get(Calendar.YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val korDay = getKorDayOfWeek(dayOfWeek)
        val fullDate = "$year${String.format("%02d",month)}${String.format("%02d",day)}"
        val monthDay = String.format("%02d",month).plus(String.format("%02d",day))

        val curDate = "$year-${String.format("%02d",month)}-${String.format("%02d",day)}\n($korDay)"

        val daily = hashMapOf("date" to curDate,"weight" to "-",
            "excercise" to false ,"drink" to false,"memo" to "-","fullDate" to fullDate,"monthDay" to monthDay)

        val weightDailyDb = dailyWeightDb.collection("DailyWeight").document(userId).collection("dayWeight").document(curDate)
        weightDailyDb.get().addOnSuccessListener { document ->
            if (document.data == null){
                weightDailyDb.set(daily, SetOptions.merge())
                Log.d(TAG,"MainFragment updateDb 당일 data 없음, 추가 완료")
            } else {
                Log.d(TAG,"MainFragment updateDb 당일 data 있음")
            }
        }.addOnFailureListener {exception ->
            Log.d(TAG,"exception : $exception")
        }
    } */

    // 데이터 없는날짜까지 확인, 날짜 만큼 초기화 매서드로 전달
    fun addWeightData(){
        Log.d(TAG,"MainFragment addWeightData 매서드 실행")
        val userId = getMyId()

        val dateList = mutableListOf<String>()

        val checkDate = dateDb.collection("DailyWeight").document(userId).collection("dayWeight")
        checkDate.get().addOnSuccessListener { result ->
            for (document in result){
                dateList.add(document["fullDate"] as String)
            }
            val lastFullDate = dateList.last()

            val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
            val lastDateFloat = simpleDateFormat.parse(lastFullDate).time

            val todayDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time.time

            val calculateDate = (todayDate-lastDateFloat)/(24*60*60*1000)
            val noDataDay = calculateDate.toInt()

            addInitData(noDataDay)

        }.addOnFailureListener { exception ->
            Log.d(TAG,"exception : $exception")

        }
    }
    // 날짜 없는날 만큼 초기화
    fun addInitData(noDateDay:Int){
        Log.d(TAG,"MainFragment addInitData 매서드 실행, 데이터 없는 일수 -> $noDateDay")
        val userId = getMyId()
        val calendar = Calendar.getInstance()
        var num = 0

        if (noDateDay == 0){
            Log.d(TAG,"MainFragment addInitData 당일 데이터 있음, 추가 필요 없음")
        } else {
            for (i in 1..noDateDay){
                calendar.add(Calendar.DATE,-num)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = (calendar.get(Calendar.MONTH)+1)
                val year = calendar.get(Calendar.YEAR)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val korDay = getKorDayOfWeek(dayOfWeek)

                val fullDate = "$year${String.format("%02d",month)}${String.format("%02d",day)}"
                val chartDate = String.format("%02d",month).plus(String.format("%02d",day))
                val curDate = "$year-${String.format("%02d",month)}-${String.format("%02d",day)}\n($korDay)"

                val daily = hashMapOf("date" to curDate,"weight" to "-",
                    "excercise" to false ,"drink" to false,"memo" to "-","fullDate" to fullDate,"chartDate" to chartDate)

                val addDateDb = dateDb.collection("DailyWeight").document(userId).collection("dayWeight").document(curDate)
                addDateDb.set(daily)

                num = 1
            }
        }

        Log.d(TAG,"MainFragment addInitData 메서드 종료")
    }

    //db 확인 후 없으면 초기화 진행 메서드
    fun checkWeightDb(){
        Log.d(TAG,"MainFragment checkDb 실행")
        val userId = getMyId()

        val checkDb = dailyWeightDb.collection("DailyWeight").document(userId)
        checkDb.get().addOnSuccessListener { document ->
            if (document.data == null){
                weightInitHash()
                Log.d(TAG,"MainFragment checkDb : 데이터 없음, 초기화 완료")
                check = true
            } else
                Log.d(TAG,"MainFragment checkDb : 데이터 확인됨, 초기화 필요없음")
                addWeightData() // 데이터 없는 일수 계산 후 초기화, 당일 데이터는 계산 안함
                check = true
        }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }

    }

    //uid 검색 후 없을시 초기화 하는 코드
    fun weightInitHash(){
        Log.d(TAG,"MainFragment weightInitHash 실행")
        val calendar = Calendar.getInstance()
        var num = 0
        val userId = getMyId()

        for (i in 1..30){
            calendar.add(Calendar.DATE,-num)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = (calendar.get(Calendar.MONTH)+1)
            val year = calendar.get(Calendar.YEAR)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val korDay = getKorDayOfWeek(dayOfWeek)

            val fullDate = "$year${String.format("%02d",month)}${String.format("%02d",day)}"
            val chartDate = String.format("%02d",month).plus(String.format("%02d",day))
            val curDate = "$year-${String.format("%02d",month)}-${String.format("%02d",day)}\n($korDay)"

            val daily = hashMapOf("date" to curDate,"weight" to "-",
                "excercise" to false ,"drink" to false,"memo" to "-","fullDate" to fullDate,"chartDate" to chartDate)

            val weightDb = dailyWeightDb.collection("DailyWeight").document(userId).collection("dayWeight").document(curDate)
            weightDb.set(daily)

            num = 1
        }
        val initDocu = hashMapOf("data" to true)
        val weightDocuDb = dailyWeightDb.collection("DailyWeight").document(userId)
        weightDocuDb.set(initDocu)

        Log.d(TAG,"MainFragment 초기화 완료")
    }

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