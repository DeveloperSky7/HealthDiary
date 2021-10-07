package org.seokhwan.weight_record

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.seokhwan.weight_record.databinding.FragmentFoodmenuRecordBinding
import java.text.SimpleDateFormat
import java.util.*


data class Foodmenu(var date:String, var breakFast:String,var lunch:String,var dinner:String,var snack:String)

class FoodmenuRecordFragment : Fragment() {
    lateinit var binding : FragmentFoodmenuRecordBinding

    private val TAG = "Firestore"
    val dailyMenuDb = FirebaseFirestore.getInstance()
    val dateDb = FirebaseFirestore.getInstance()

    val foodmenu = mutableListOf<Foodmenu>()
    val foodmenuAdapter = FoodmenuRecordAdapter()

    var check = false
    var resumeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFoodmenuRecordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG,"FoodmenuFragment onViewCreated")

        binding.foodmenuRecordLoadingProgress.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            checkDb() //db 확인 후 없으면 초기화 진행 메서드
            delay(1000)
            while (check == true){
                Log.d(TAG,"FoodmenuFragment loading 메서드 실행")
                foodmenu.clear()
                loadingDb() //리사이클러뷰 데이터 불러오는 메서드
                check = false
            }
        }

        binding.foodmenuRecordRecycler.adapter = foodmenuAdapter
        //역순 표기.
        val manager = LinearLayoutManager(context)
        manager.reverseLayout = true
        manager.stackFromEnd = true

        binding.foodmenuRecordRecycler.layoutManager = manager

        updateDbChecking() //리사이클러뷰 업데이트시 확인하는 메서드

    }
    //재실행 될시 실행 메서드
    override fun onResume() {
        super.onResume()

        resumeCount += 1
        Log.d(TAG,"FoodmenuFragment onresume resumeCount = $resumeCount")

        if (resumeCount != 1){
            foodmenu.clear()
            Log.d(TAG,"FoodmenuFragment list 클리어")
            loadingDb()
        }

    }

    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
        return uid
    }

    //리사이클러뷰 업데이트시 확인하는 메서드
    fun updateDbChecking(){
        Log.d(TAG,"FoodmenuFragment updateDbChecking 메서드 실행중")
        val userId = getMyId()
        val updateDb = dailyMenuDb.collection("DailyMenu").document(userId).collection("dayFood")
        updateDb.addSnapshotListener { snapshot, e ->
            if (e != null){
                Log.d(TAG,"FoodmenuFragment update 확인중 에러 -> $e")
                return@addSnapshotListener
            }

            for (doc in snapshot!!.documentChanges){

            }
            Log.d(TAG,"FoodmenuFragment update 확인")
        }

    }

/*    // 매일 업데이트 메서드
    fun updateDb() {
        Log.d(TAG,"foodmenu updateDb 실행중")
        val userId = getMyId()
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = (calendar.get(Calendar.MONTH)+1)
        val year = calendar.get(Calendar.YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val korDay = getKorDayOfWeek(dayOfWeek)

        val curDate = "$year-${String.format("%02d",month)}-${String.format("%02d",day)}\n($korDay)"
        val daily = hashMapOf("date" to curDate,"breakfast" to "-",
            "lunch" to "-","dinner" to "-","snack" to "-")

        val dailyDb = dailyMenuDb.collection("DailyMenu").document(userId).collection("dayFood").document(curDate)
        dailyDb.get().addOnSuccessListener { document->
            if (document.data == null){
                dailyDb.set(daily, SetOptions.merge())
                Log.d(TAG,"foodmenu updateDb 당일 data 없음, 추가 완료")
            } else {
                Log.d(TAG,"foodmenu updateDb 당일 data 있음")
            }
        }
            .addOnFailureListener {exception ->
                Log.d(TAG,"exception : $exception")
            }
    } */

    //리사이클러뷰 데이터 불러오는 메서드
    fun loadingDb(){
        val userId = getMyId()

        val dayFoodDb = dailyMenuDb.collection("DailyMenu").document(userId).collection("dayFood")
        dayFoodDb.get()
            .addOnSuccessListener { result ->
                Log.d(TAG,"FoodmenuFragment dayFoodDb 읽기 시작")
                for (document in result){
                    val menu = Foodmenu(document["date"] as String, document["breakfast"] as String,
                        document["lunch"] as String, document["dinner"] as String, document["snack"] as String)

                    foodmenu.add(menu)
                    foodmenuAdapter.list = foodmenu
                }
                Log.d(TAG,"FoodmenuFragment dayFoodDb 읽기 완료, 리사이클러뷰 업데이트")
                binding.foodmenuRecordLoadingProgress.visibility = View.GONE
                foodmenuAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }
    }

    // 데이터 없는날짜까지 확인, 날짜 만큼 초기화 매서드로 전달
    fun addFoodmenuData(){
        Log.d(TAG,"FoodmenuFragment addFoodmenuData 실행")
        val userId = getMyId()

        val dateList = mutableListOf<String>()

        val checkDb = dateDb.collection("DailyMenu").document(userId).collection("dayFood")
        checkDb.get().addOnSuccessListener { result ->
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
    fun addInitData(noDateDay:Int) {
        Log.d(TAG, "FoodmenuFragment addInitData 매서드 실행, 데이터 없는 일수 -> $noDateDay")
        val userId = getMyId()
        val calendar = Calendar.getInstance()
        var num = 0

        if (noDateDay == 0) {
            Log.d(TAG, "FoodmenuFragment addInitData 당일 데이터 있음, 추가 필요 없음")
        } else {
            for (i in 1..noDateDay) {
                calendar.add(Calendar.DATE,-num)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = (calendar.get(Calendar.MONTH)+1)
                val year = calendar.get(Calendar.YEAR)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val korDay = getKorDayOfWeek(dayOfWeek)

                val fullDate = "$year${String.format("%02d",month)}${String.format("%02d",day)}"
                val curDate = "$year-${String.format("%02d",month)}-${String.format("%02d",day)}\n($korDay)"

                val daily = hashMapOf("date" to curDate,"breakfast" to "-",
                    "lunch" to "-","dinner" to "-","snack" to "-","fullDate" to fullDate)

                val addDataDb = dateDb.collection("DailyMenu").document(userId).collection("dayFood").document(curDate)
                addDataDb.set(daily)

                num = 1
            }
        }

        Log.d(TAG,"FoodmenuFragment addInitData 메서드 종료")
    }


    //db 확인 후 없으면 초기화 진행 메서드
    fun checkDb(){
        val userId = getMyId()

        val checkDb = dailyMenuDb.collection("DailyMenu").document(userId)
        checkDb.get()
            .addOnSuccessListener { document ->
                if (document.data != null){
                    Log.d(TAG,"FoodmenuFragment checkDb : 데이터 확인됨, 초기화 필요없음")
                    addFoodmenuData()
                    check = true
                } else if(document.data == null){
                    Log.d(TAG,"FoodmenuFragment checkDb : 데이터 없음, 초기화 필요")
                    foodmenuInitHash()
                    check = true
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }
    }

    //uid 검색 후 없을시 초기화 하는 코드
    fun foodmenuInitHash(){
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
            val curDate = "$year-${String.format("%02d",month)}-${String.format("%02d",day)}\n($korDay)"

            val daily = hashMapOf("date" to curDate,"breakfast" to "-",
            "lunch" to "-","dinner" to "-","snack" to "-","fullDate" to fullDate)

            val foodmenuDb = dailyMenuDb.collection("DailyMenu").document(userId).collection("dayFood").document(curDate)
            foodmenuDb.set(daily)

            num = 1
        }

        val initDocu = hashMapOf("data" to "true")
        val foodmenuDocuDb = dailyMenuDb.collection("DailyMenu").document(userId)
        foodmenuDocuDb.set(initDocu)

        Log.d(TAG,"FoodmenuFragment 초기화 완료")

    }

    //한글로 요일 표기 메서드
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
