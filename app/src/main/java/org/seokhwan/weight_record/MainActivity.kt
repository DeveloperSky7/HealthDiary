package org.seokhwan.weight_record

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import org.seokhwan.weight_record.databinding.ActivityMainBinding


data class User
    (var uid:String? , var userBirth: String?, var userWeight: String?, var userHeight: String? ,
     var userGender: String?,  var data: Boolean = false)

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    lateinit var resultListener: ActivityResultLauncher<Intent> // 인텐트 받아오는 변수 설정

    lateinit var mAdView : AdView

    private val TAG = "Firestore"
    val db = FirebaseFirestore.getInstance()

    private var userId = ""
    private var userBirth = ""
    private var userWeight = ""
    private var userHeight = ""
    private var userGender = ""

    var backKeyPressedTime : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.mainActionToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        userId = getMyId()

        checkDb() //db 확인 후 초기 정보 없으면 입력창 전환 메서드

        startForResult() // 로그인 정보 받아오는 메서드

        // 뷰페이저 탭, 생성등 만드는곳
        val fragmentList = listOf(MainFragment(),WeightRecordFragment(),FoodmenuRecordFragment(),InfomationFragment())

        val fragmentAdapter = FragmentAdapter(this)
        fragmentAdapter.fragmentList = fragmentList

        binding.fragmentViewpager.adapter = fragmentAdapter

        val tabTitle = listOf<String>("메인","체중 변화","식단 기록","내 정보")
        TabLayoutMediator(binding.fragmentTablayout,binding.fragmentViewpager){
                tab,position -> tab.text = tabTitle[position]
        }.attach()
        Log.d(TAG,"MainActivity 뷰페이져, 탭 생성 성공")


        // 광고
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    //뒤로가기 두번 누르면 종료
    override fun onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressedTime + 2500){
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show()
            return
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2500){
            Log.d(TAG,"종료하겠습니다.")
            finishAffinity()
        }
    }
    //툴바 구현
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    //툴바 메뉴란 아이템 선택 리스너
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.main_toolbar_userInfo -> {
                Log.d(TAG,"MainActivity 팝업창 유저정보수정 클릭")
                correctionInfo()
                true
            }
            R.id.main_toolbar_removeAD -> {
                Log.d(TAG,"MainActivity 광고 제거 클릭")
                Toast.makeText(this,"업데이트 예정 입니다.",Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(this.contentResolver,Settings.Secure.ANDROID_ID)
        return uid
    }

    //db 확인 후 초기 정보 없으면 입력창 전환 메서드
    fun checkDb(){
        userId = getMyId()

        val userLoginIntent = Intent(this,UserLogin::class.java)
        userLoginIntent.putExtra("MainCount","초기")
        // DB 확인 후 문서 있으면 메인화면, 없으면 로그인정보창
        val userDb = db.collection("User").document(userId)
        userDb.get()
            .addOnSuccessListener { document ->
                if (document.data != null){
                    Log.d(TAG,"MainActivity 로그인 기록 확인.")
                } else {
                    Log.d(TAG,"MainActivity 데이터 없음 INTENT 이동")
                    resultListener.launch(userLoginIntent)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }

    }

    //초기 정보 수정
    fun correctionInfo(){
        userId = getMyId()
        Log.d(TAG,"MainActivity 초기 유저 정보 수정 실행중")

        val userLoginIntent = Intent(this,UserLogin::class.java)
        userLoginIntent.putExtra("MainCount","수정")

        resultListener.launch(userLoginIntent)

    }

    fun showToast(data:String){
        Toast.makeText(this,data,Toast.LENGTH_SHORT).show()
    }

    fun startForResult(){ // 로그인 정보 받아오는 메서드
        resultListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            when(it.resultCode) {
                110 -> {
                    Log.d(TAG,"MainActivity 유저 정보 초기 정보 데이타베이스 저장중")
                    val inputBirth = it.data?.getStringExtra("loginBirth")
                    val inputGender = it.data?.getStringExtra("loginGender")
                    val inputWeight = it.data?.getStringExtra("loginWeight")
                    val inputHeight = it.data?.getStringExtra("loginHeight")
                    val chartDate = it.data?.getStringExtra("chartDate")
                    userId = getMyId()
                    userBirth = inputBirth.toString()
                    userWeight = "${inputWeight.toString()}"
                    userHeight = "${inputHeight.toString()}"
                    userGender = inputGender.toString()

                    val data = hashMapOf("userId" to userId, "userBirth" to userBirth,
                        "userWeight" to userWeight, "userHeight" to userHeight,
                        "userGender" to userGender, "userData" to true, "chartInitDate" to chartDate)

                    db.collection("User").document(userId).set(data)
                }
                111 -> {
                    Log.d(TAG,"MainActivity 유저 정보 수정자료 데이타베이스 저장중")
                    val inputBirth = it.data?.getStringExtra("loginBirth")
                    val inputGender = it.data?.getStringExtra("loginGender")
                    val inputWeight = it.data?.getStringExtra("loginWeight")
                    val inputHeight = it.data?.getStringExtra("loginHeight")
                    val chartDate = it.data?.getStringExtra("chartDate")
                    userId = getMyId()
                    userBirth = inputBirth.toString()
                    userWeight = "${inputWeight.toString()}"
                    userHeight = "${inputHeight.toString()}"
                    userGender = inputGender.toString()

                    val data = hashMapOf("userId" to userId, "userBirth" to userBirth,
                        "userWeight" to userWeight, "userHeight" to userHeight,
                        "userGender" to userGender, "userData" to true, "chartInitDate" to chartDate)

                    db.collection("User").document(userId).set(data)
                }
            }
        }

    }
}