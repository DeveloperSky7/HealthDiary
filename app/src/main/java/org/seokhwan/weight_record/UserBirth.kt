package org.seokhwan.weight_record

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CalendarView
import android.widget.DatePicker
import org.seokhwan.weight_record.databinding.ActivityUserBirthBinding
import java.util.*

class UserBirth : AppCompatActivity() {

    val binding by lazy {ActivityUserBirthBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()
        binding.birhPicker.init(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH), object : CalendarView.OnDateChangeListener,
            DatePicker.OnDateChangedListener{
            override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {

            }

            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int
            ) {
                binding.birhPickerText.text = "${binding.birhPicker.year}-${String.format("%02d",binding.birhPicker.month+1)}-${String.format("%02d",binding.birhPicker.dayOfMonth)}"
            }
        })

        binding.birthNextbtn.setOnClickListener {
            val userBirth = binding.birhPickerText.text.toString()
            val intent = Intent()
            intent.putExtra("birth",userBirth)
            setResult(120,intent)
            finish()
        }



    }
}