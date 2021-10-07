package org.seokhwan.weight_record

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.firebase.firestore.FirebaseFirestore
import org.seokhwan.weight_record.databinding.FragmentWeightRecordBinding
import java.lang.StringBuilder
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class WeightRecordFragment : Fragment() {

    lateinit var binding:FragmentWeightRecordBinding

    private val TAG = "Firestore"
    val weightFragmentDb = FirebaseFirestore.getInstance()
    val initChartDb = FirebaseFirestore.getInstance()

    val entries = ArrayList<Entry>()

    val dateList = mutableListOf<String>()

    var resumeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWeightRecordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG,"WeightRecordFragment oncreateview ")

        chartDataLoading()
    }


    override fun onResume() {
        super.onResume()

        resumeCount += 1
        Log.d(TAG,"WeightRecordFragment resumeCount -> $resumeCount ")

        if (resumeCount != 1){
            entries.clear()
            dateList.clear()
            chartDataLoading() // 차트 데이터 로딩
        }

    }

    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
        return uid
    }
    //db data 없을 경우 차트 데이터 초기 유저 데이터에서 가져오기
    private fun chartInit(){
        Log.d(TAG,"WeightRecordFragment 차트 데이터 db 없음, 초기 데이터 가져오기")
        val userId = getMyId()
        var num = 0
        dateList.clear()

        val chartInitDb = initChartDb.collection("User").document(userId)
        chartInitDb.get().addOnSuccessListener { document ->
            if (document.data != null){
                val weight = document["userWeight"] as String
                val date = document["chartInitDate"] as String
                val dateString = StringBuilder()
                dateString.append(date)
                dateString.insert(2,"-")
                dateList.add(dateString.toString())
                entries.add(Entry(num.toFloat(),weight.toFloat()))
                setChart(entries)

            }


        }.addOnFailureListener { exception ->
            Log.d(TAG,"exception : $exception") }

    }
    //db data 로딩 후 차트 데이터 지정
    private fun chartDataLoading(){
        Log.d(TAG,"WeightRecordFragment chartDataLoading 메서드 실행")
        var num = 0
        val userId = getMyId()
        dateList.clear()

        val chartDataLoadingDb = weightFragmentDb.collection("DailyWeight").document(userId).collection("dayWeight")
        chartDataLoadingDb.get().addOnSuccessListener { result ->
            for (document in result){
                if (document["weight"] as String != "-"){
                    val weight = document["weight"] as String
                    val date = document["chartDate"] as String
                    val dateString = StringBuilder()
                    dateString.append(date)
                    dateString.insert(2,"-")
                    dateList.add(dateString.toString())
                    entries.add(Entry(num.toFloat(),weight.toFloat()))
                    num += 1
                }
            }
            Log.d(TAG,"WeightRecordFragment chartDataLoading num: $num , entry : ${entries}")
            if (num != 0){
                setChart(entries)
            } else{
                chartInit()
            }

        }
            .addOnFailureListener { exception ->
                Log.d(TAG,"exception : $exception")
            }
    }
    //라인 차트 설정
    private fun setChart(entries:MutableList<Entry>){
        Log.d(TAG,"WeightRecordFragment setChart 메서드 실행, entry -> $entries")
        //그래프에 들어갈 데이터 준비

        //데이터 그래프 설정
        val dataSet = LineDataSet(entries,"체중 변화").apply {
            setDrawCircleHole(true)
            setCircleColor(resources.getColor(R.color.design_default_color_primary_dark)) // 데이터 중점 원형 색
            color = resources.getColor(R.color.design_default_color_primary_dark) // 라인색 지정
            circleRadius = 3f // 데이터 중점 원형 크기
            circleHoleRadius = 3f
            valueTextSize = 12f // 값 글자 크기
            lineWidth = 2f // 라인 두께
            fillAlpha = 0 //라인색 투명도
            fillColor = resources.getColor(R.color.purple_200) // 라인색 지정
            setDrawValues(true) //값을 그리기
            valueFormatter = MyValueFormatter()
        }

        //x,y 범례등 차트 외관 설정
        binding.fragmentWeightRecordLinechart.apply {
            val lineData = LineData(dataSet)
            data = lineData

            axisRight.isEnabled = false // y축 오른쪽 데이터 비활성화
            setPinchZoom(true) // 확대 설정
            isDoubleTapToZoomEnabled = true // 더블탭 확대 설정
            setVisibleXRangeMaximum(7f) // x축 최대 항목
            description.isEnabled = false // 라인차트 안의 텍스트(x축) 미사용
            description.text = "날짜" // 라인차트 안의 텍스트(x축)
            description.textSize = 10f // 라인차트 안의 텍스트 사이즈(x축)
            setBackgroundColor(resources.getColor(R.color.white)) // 배경색
            setExtraOffsets(8f,16f,8f,16f) // 차트 padding 설정

            //범례 관련
            legend.apply {
                isEnabled = false
                textSize = 15f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP // 수직 조정 : 위로
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // 수평 조정 : 가운데
                orientation = Legend.LegendOrientation.HORIZONTAL // 범례와 차트 설정: 수평
                setDrawInside(false) //차트안에 그리기 허용
            }
            // x축 왼쪽
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM //x축 데이터 아래로
                setDrawGridLines(true) // 그리드
                setDrawAxisLine(true) // 축라인
                axisLineColor = Color.BLACK
                setDrawLabels(true) // 라벨
                textSize = 10f
                textColor = Color.BLACK
                granularity = 1f //x축 데이터 표시 간격
                isGranularityEnabled = true // y축 간격 제한하는 세분화 기능
                valueFormatter = MyValueFormatter()


            }
            // y축 왼쪽
            axisLeft.apply {
                setDrawAxisLine(true) // 축라인
                setDrawGridLines(true) //그리드
                axisLineColor = Color.BLACK
                setDrawLabels(true) // 라벨
                textSize = 10f
                textColor = Color.BLACK
//                granularity = 1f //y축 데이터 표시 간격
                axisMinimum = 0f //y축 데이터 최소 표시값
                axisMaximum = 140f
                isGranularityEnabled = true // y축 간격 제한하는 세분화 기능
            }
        }

        binding.fragmentWeightRecordLinechart.invalidate()
    }

    inner class MyValueFormatter : ValueFormatter() {

        private val format = DecimalFormat("###,##0.0")

        override fun getPointLabel(entry: Entry?): String {
            return format.format(entry?.y)
        }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return dateList.getOrNull(value.toInt()) ?: value.toString()
    }

    }

}


