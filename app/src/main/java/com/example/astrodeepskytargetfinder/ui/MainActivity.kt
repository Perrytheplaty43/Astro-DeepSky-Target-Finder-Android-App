package com.example.astrodeepskytargetfinder.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.astrodeepskytargetfinder.R
import com.example.astrodeepskytargetfinder.data.Request
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "CutPasteId")
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    @SuppressLint("CutPasteId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val lat = findViewById<EditText>(R.id.editTextNumberDecimal)
        val long = findViewById<EditText>(R.id.editTextNumberDecimal2)
        val loading = findViewById<ProgressBar>(R.id.progressBar)
        fun getLastKnownLocation() {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location->
                    Log.d("LOCATION", location.latitude.toString() + ", " + location.longitude.toString())
                    lat.setText(location.latitude.toString().dropLast(5))
                    long.setText(location.longitude.toString().dropLast(5))
                    loading.visibility = View.GONE
                }

        }
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    getLastKnownLocation()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Location")
                        .setMessage("Location permission are needed inorder for this feature to work.") // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                                // Continue with delete operation
                            } // A null listener allows the button to dismiss the dialog and take no further action.
                        .show()
                }
            }
        val that = this
        val firstImage = findViewById<ImageView>(R.id.imageViewFirst)
        val secondImage = findViewById<ImageView>(R.id.imageViewSecond)
        val thirdImage = findViewById<ImageView>(R.id.imageViewThird)
        val fourthImage = findViewById<ImageView>(R.id.imageViewFourth)
        val fifthImage = findViewById<ImageView>(R.id.imageViewFifth)

        val editText = findViewById<EditText>(R.id.editTextNumberTol)

        val btnClick = findViewById<Button>(R.id.submit)
        val btnClickGeo = findViewById<Button>(R.id.geo)
        val btnClickDate = findViewById<Button>(R.id.button2)

        var date = ""
        btnClickDate.setOnClickListener {
            val dateDia =
                MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            dateDia.show(supportFragmentManager, "tag")

            dateDia.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                val month = calendar.get(Calendar.MONTH) + 1
                val monthString = if (month in 1..9) {
                    "0$month"
                } else {
                    month.toString()
                }

                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val dayString = if (day in 1..9) {
                    "0$day"
                } else {
                    day.toString()
                }
                date = calendar.get(Calendar.YEAR).toString() + "-" + monthString + "-" + dayString
            }
        }

        btnClickGeo.setOnClickListener {
            loading.visibility = View.VISIBLE
            fun getLastKnownLocation() {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location->
                        Log.d("LOCATION", location.latitude.toString() + ", " + location.longitude.toString())
                        lat.setText(location.latitude.toString().dropLast(5))
                        long.setText(location.longitude.toString().dropLast(5))
                        loading.visibility = View.GONE
                    }

            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            getLastKnownLocation()
            loading.visibility = View.GONE
        }

        btnClick.setOnClickListener {
            loading.visibility = View.VISIBLE
            Log.d("DATE:", date)
            if (editText.text.toString().toInt() < 42) {
                editText.setText("42")
            } else if (editText.text.toString().toInt() > 88) {
                editText.setText("88")
            }
            val magTol = findViewById<EditText>(R.id.editTextNumber)

            val gx = findViewById<CheckBox>(R.id.checkBox)
            val oc = findViewById<CheckBox>(R.id.checkBox2)
            val gb = findViewById<CheckBox>(R.id.checkBox3)
            val nb = findViewById<CheckBox>(R.id.checkBox4)
            val pl = findViewById<CheckBox>(R.id.checkBox5)
            var type = ""
            if (gx.isChecked) type += "Gx,"
            if (oc.isChecked) type += "OC,"
            if (gb.isChecked) type += "Gb,"
            if (nb.isChecked) type += "Nb,"
            if (pl.isChecked) type += "Pl,"
            type += "CpN,Ast,Kt,TS,DS,SS,Q,U,D,PD"

            Log.d("TYPE:", type)
            val url = "https://athesto.ddns.net/astro?lat=" + lat.text.toString() + "&long=" + long.text.toString() + "&tol=" + editText.text.toString() + "&tolMag=" + magTol.text.toString() + "&type=" + type + "&date=" + date
            Log.d("URL:", url)
            doAsync {
                val dataJ = Request(url).run().split("[")
                Log.d("Res1", dataJ.toString())
                val DATA = dataJ.toString()
                if (dataJ.toString() == "[, \"null\",, ]]]") {
                    uiThread {
                        val firstConst = findViewById<TextView>(R.id.textView2)
                        firstConst.text = getString(R.string.noTargets)

                        val secondConst = findViewById<TextView>(R.id.textView22)
                        secondConst.text = getString(R.string.none, "2nd")

                        val thirdConst = findViewById<TextView>(R.id.textView23)
                        thirdConst.text = getString(R.string.none, "3rd")

                        val fourthConst = findViewById<TextView>(R.id.textView24)
                        fourthConst.text = getString(R.string.none, "4th")

                        val fifthConst = findViewById<TextView>(R.id.textView25)
                        fifthConst.text = getString(R.string.none, "5th")

                        Glide.clear(firstImage)
                        Glide.clear(secondImage)
                        Glide.clear(thirdImage)
                        Glide.clear(fourthImage)
                        Glide.clear(fifthImage)

                        val secondConst6 = findViewById<TextView>(R.id.textView62)
                        secondConst6.text = ""

                        val thirdConst6 = findViewById<TextView>(R.id.textView63)
                        thirdConst6.text = ""

                        val fourthConst6 = findViewById<TextView>(R.id.textView64)
                        fourthConst6.text = ""

                        val fifthConst6 = findViewById<TextView>(R.id.textView65)
                        fifthConst6.text = ""

                        val firstConst6 = findViewById<TextView>(R.id.textView6)
                        firstConst6.text = ""
                        loading.visibility = View.GONE
                        return@uiThread
                    }
                    return@doAsync
                }
                val regex = Regex("[\\\\\"\\]]")
                val subData = arrayListOf(regex.replace(dataJ[3], ""))
                for (i in 3 until dataJ.size - 1) {
                    val result = regex.replace(dataJ[i], "")
                    subData.add(result)
                }
                val final = arrayListOf(subData[0].split(","))
                if (subData.size > 1) {
                    for (i in 1 until subData.size) {
                        final.add(subData[i].split(","))
                    }
                }
                final.removeAt(1)
                //The var "final" is the final 2d array
                Log.d("Res", final.toString())

                uiThread {
                    var mag0 = final[0][2]
                    if (mag0 == "") {
                        mag0 = "N/A"
                    }

                    var mag1 = final[1][2]
                    if (mag1 == "") {
                        mag1 = "N/A"
                    }

                    var mag2 = final[2][2]
                    if (mag2 == "") {
                        mag2 = "N/A"
                    }

                    var mag3 = final[3][2]
                    if (mag3 == "") {
                        mag3 = "N/A"
                    }

                    var mag4 = final[4][2]
                    if (mag4 == "") {
                        mag4 = "N/A"
                    }
                    when {
                        final.size >= 5 -> {
                            val firstTitle = findViewById<TextView>(R.id.textView2)
                            if (final[0][0].contains("I")) {
                                firstTitle.text =
                                    getString(R.string.first2, final[0][0].drop(1), mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[0][0].drop(1) + ".jpg")
                                    .into(firstImage)
                            } else {
                                firstTitle.text = getString(R.string.first3, final[0][0], mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[0][0] + ".jpg")
                                    .into(firstImage)
                            }
                            val firstConst = findViewById<TextView>(R.id.textView6)
                            firstConst.text = getString(R.string.first4, final[0][4], final[0][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val secondTitle = findViewById<TextView>(R.id.textView22)
                            if (final[1][0].contains("I")) {
                                secondTitle.text =
                                    getString(R.string.second2, final[1][0].drop(1), mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[1][0].drop(1) + ".jpg")
                                    .into(secondImage)
                            } else {
                                secondTitle.text = getString(R.string.second3, final[1][0], mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[1][0] + ".jpg")
                                    .into(secondImage)
                            }
                            val secondConst = findViewById<TextView>(R.id.textView62)
                            secondConst.text = getString(R.string.second4, final[1][4], final[1][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val thirdTitle = findViewById<TextView>(R.id.textView23)
                            if (final[2][0].contains("I")) {
                                thirdTitle.text =
                                    getString(R.string.third2, final[2][0].drop(1), mag2)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[2][0].drop(1) + ".jpg")
                                    .into(thirdImage)
                            } else {
                                thirdTitle.text = getString(R.string.third3, final[2][0], mag2)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[2][0] + ".jpg")
                                    .into(thirdImage)
                            }
                            val thirdConst = findViewById<TextView>(R.id.textView63)
                            thirdConst.text = getString(R.string.third4, final[2][4], final[2][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val fourthTitle = findViewById<TextView>(R.id.textView24)
                            if (final[3][0].contains("I")) {
                                fourthTitle.text =
                                    getString(R.string.fourth2, final[3][0].drop(1), mag3)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[3][0].drop(1) + ".jpg")
                                    .into(fourthImage)
                            } else {
                                fourthTitle.text = getString(R.string.fourth3, final[3][0], mag3)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[3][0] + ".jpg")
                                    .into(fourthImage)
                            }
                            val fourthConst = findViewById<TextView>(R.id.textView64)
                            fourthConst.text = getString(R.string.fourth4, final[3][4], final[3][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val fifthTitle = findViewById<TextView>(R.id.textView25)
                            if (final[4][0].contains("I")) {
                                fifthTitle.text =
                                    getString(R.string.fifth2, final[4][0].drop(1), mag4)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[4][0].drop(1) + ".jpg")
                                    .into(fifthImage)
                            } else {
                                fifthTitle.text = getString(R.string.fifth3, final[4][0], mag4)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[4][0] + ".jpg")
                                    .into(fifthImage)
                            }
                            val fifthConst = findViewById<TextView>(R.id.textView65)
                            fifthConst.text = getString(R.string.fifth4, final[4][4], final[4][3])
                        }
                        final.size == 4 -> {
                            val firstTitle = findViewById<TextView>(R.id.textView2)
                            if (final[0][0].contains("I")) {
                                firstTitle.text =
                                    getString(R.string.first2, final[0][0].drop(1), mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[0][0].drop(1) + ".jpg")
                                    .into(firstImage)
                            } else {
                                firstTitle.text = getString(R.string.first3, final[0][0], mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[0][0] + ".jpg")
                                    .into(firstImage)
                            }
                            val firstConst = findViewById<TextView>(R.id.textView6)
                            firstConst.text = getString(R.string.first4, final[0][4], final[0][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val secondTitle = findViewById<TextView>(R.id.textView22)
                            if (final[1][0].contains("I")) {
                                secondTitle.text =
                                    getString(R.string.second2, final[1][0].drop(1), mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[1][0].drop(1) + ".jpg")
                                    .into(secondImage)
                            } else {
                                secondTitle.text = getString(R.string.second3, final[1][0], mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[1][0] + ".jpg")
                                    .into(secondImage)
                            }
                            val secondConst = findViewById<TextView>(R.id.textView62)
                            secondConst.text = getString(R.string.second4, final[1][4], final[1][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val thirdTitle = findViewById<TextView>(R.id.textView23)
                            if (final[2][0].contains("I")) {
                                thirdTitle.text =
                                    getString(R.string.third2, final[2][0].drop(1), mag2)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[2][0].drop(1) + ".jpg")
                                    .into(thirdImage)
                            } else {
                                thirdTitle.text = getString(R.string.third3, final[2][0], mag2)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[2][0] + ".jpg")
                                    .into(thirdImage)
                            }
                            val thirdConst = findViewById<TextView>(R.id.textView63)
                            thirdConst.text = getString(R.string.third4, final[2][4], final[2][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val fourthTitle = findViewById<TextView>(R.id.textView24)
                            if (final[3][0].contains("I")) {
                                fourthTitle.text =
                                    getString(R.string.fourth2, final[3][0].drop(1), mag3)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[3][0].drop(1) + ".jpg")
                                    .into(fourthImage)
                            } else {
                                fourthTitle.text = getString(R.string.fourth3, final[3][0], mag3)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[3][0] + ".jpg")
                                    .into(fourthImage)
                            }
                            val fourthConst = findViewById<TextView>(R.id.textView64)
                            fourthConst.text = getString(R.string.fourth4, final[3][4], final[3][3])

                            val fifthConst = findViewById<TextView>(R.id.textView25)
                            fifthConst.text = getString(R.string.none, "5th")

                            Glide.clear(fifthImage)

                            val fifthConst6 = findViewById<TextView>(R.id.textView65)
                            fifthConst6.text = ""
                        }
                        final.size == 3 -> {
                            val firstTitle = findViewById<TextView>(R.id.textView2)
                            if (final[0][0].contains("I")) {
                                firstTitle.text =
                                    getString(R.string.first2, final[0][0].drop(1), mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[0][0].drop(1) + ".jpg")
                                    .into(firstImage)
                            } else {
                                firstTitle.text = getString(R.string.first3, final[0][0], mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[0][0] + ".jpg")
                                    .into(firstImage)
                            }
                            val firstConst = findViewById<TextView>(R.id.textView6)
                            firstConst.text = getString(R.string.first4, final[0][4], final[0][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val secondTitle = findViewById<TextView>(R.id.textView22)
                            if (final[1][0].contains("I")) {
                                secondTitle.text =
                                    getString(R.string.second2, final[1][0].drop(1), mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[1][0].drop(1) + ".jpg")
                                    .into(secondImage)
                            } else {
                                secondTitle.text = getString(R.string.second3, final[1][0], mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[1][0] + ".jpg")
                                    .into(secondImage)
                            }
                            val secondConst = findViewById<TextView>(R.id.textView62)
                            secondConst.text = getString(R.string.second4, final[1][4], final[1][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val thirdTitle = findViewById<TextView>(R.id.textView23)
                            if (final[2][0].contains("I")) {
                                thirdTitle.text =
                                    getString(R.string.third2, final[2][0].drop(1), mag2)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[2][0].drop(1) + ".jpg")
                                    .into(thirdImage)
                            } else {
                                thirdTitle.text = getString(R.string.third3, final[2][0], mag2)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[2][0] + ".jpg")
                                    .into(thirdImage)
                            }
                            val thirdConst = findViewById<TextView>(R.id.textView63)
                            thirdConst.text = getString(R.string.third4, final[2][4], final[2][3])
                            val fourthConst = findViewById<TextView>(R.id.textView24)
                            fourthConst.text = getString(R.string.none, "4th")

                            val fifthConst = findViewById<TextView>(R.id.textView25)
                            fifthConst.text = getString(R.string.none, "5th")

                            Glide.clear(fourthImage)
                            Glide.clear(fifthImage)

                            val fourthConst6 = findViewById<TextView>(R.id.textView64)
                            fourthConst6.text = ""

                            val fifthConst6 = findViewById<TextView>(R.id.textView65)
                            fifthConst6.text = ""
                        }
                        final.size == 2 -> {
                            val firstTitle = findViewById<TextView>(R.id.textView2)
                            if (final[0][0].contains("I")) {
                                firstTitle.text =
                                    getString(R.string.first2, final[0][0].drop(1), mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[0][0].drop(1) + ".jpg")
                                    .into(firstImage)
                            } else {
                                firstTitle.text = getString(R.string.first3, final[0][0], mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[0][0] + ".jpg")
                                    .into(firstImage)
                            }
                            val firstConst = findViewById<TextView>(R.id.textView6)
                            firstConst.text = getString(R.string.first4, final[0][4], final[0][3])

                            //-------------------------------------------------------------------------------------------------------------------------
                            val secondTitle = findViewById<TextView>(R.id.textView22)
                            if (final[1][0].contains("I")) {
                                secondTitle.text =
                                    getString(R.string.second2, final[1][0].drop(1), mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[1][0].drop(1) + ".jpg")
                                    .into(secondImage)
                            } else {
                                secondTitle.text = getString(R.string.second3, final[1][0], mag1)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[1][0] + ".jpg")
                                    .into(secondImage)
                            }
                            val secondConst = findViewById<TextView>(R.id.textView62)
                            secondConst.text = getString(R.string.second4, final[1][4], final[1][3])

                            val thirdConst = findViewById<TextView>(R.id.textView23)
                            thirdConst.text = getString(R.string.none, "3rd")

                            val fourthConst = findViewById<TextView>(R.id.textView24)
                            fourthConst.text = getString(R.string.none, "4th")

                            val fifthConst = findViewById<TextView>(R.id.textView25)
                            fifthConst.text = getString(R.string.none, "5th")

                            Glide.clear(thirdImage)
                            Glide.clear(fourthImage)
                            Glide.clear(fifthImage)

                            val thirdConst6 = findViewById<TextView>(R.id.textView63)
                            thirdConst6.text = ""

                            val fourthConst6 = findViewById<TextView>(R.id.textView64)
                            fourthConst6.text = ""

                            val fifthConst6 = findViewById<TextView>(R.id.textView65)
                            fifthConst6.text = ""
                        }
                        final.size == 1 -> {
                            val firstTitle = findViewById<TextView>(R.id.textView2)
                            if (final[0][0].contains("I")) {
                                firstTitle.text =
                                    getString(R.string.first2, final[0][0].drop(1), mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/IC" + final[0][0].drop(1) + ".jpg")
                                    .into(firstImage)
                            } else {
                                firstTitle.text = getString(R.string.first3, final[0][0], mag0)
                                Glide.with(that)
                                    .load("https://athesto.ddns.net/Images/NGC" + final[0][0] + ".jpg")
                                    .into(firstImage)
                            }
                            val firstConst = findViewById<TextView>(R.id.textView6)
                            firstConst.text = getString(R.string.first4, final[0][4], final[0][3])

                            val secondConst = findViewById<TextView>(R.id.textView22)
                            secondConst.text = getString(R.string.none, "2nd")

                            val thirdConst = findViewById<TextView>(R.id.textView23)
                            thirdConst.text = getString(R.string.none, "3rd")

                            val fourthConst = findViewById<TextView>(R.id.textView24)
                            fourthConst.text = getString(R.string.none, "4th")

                            val fifthConst = findViewById<TextView>(R.id.textView25)
                            fifthConst.text = getString(R.string.none, "5th")

                            Glide.clear(secondImage)
                            Glide.clear(thirdImage)
                            Glide.clear(fourthImage)
                            Glide.clear(fifthImage)

                            val secondConst6 = findViewById<TextView>(R.id.textView62)
                            secondConst6.text = ""

                            val thirdConst6 = findViewById<TextView>(R.id.textView63)
                            thirdConst6.text = ""

                            val fourthConst6 = findViewById<TextView>(R.id.textView64)
                            fourthConst6.text = ""

                            val fifthConst6 = findViewById<TextView>(R.id.textView65)
                            fifthConst6.text = ""
                        }
                        else -> {
                            val firstConst = findViewById<TextView>(R.id.textView6)
                            firstConst.text = ""

                            val secondConst = findViewById<TextView>(R.id.textView22)
                            secondConst.text = getString(R.string.none, "2nd")

                            val thirdConst = findViewById<TextView>(R.id.textView23)
                            thirdConst.text = getString(R.string.none, "3rd")

                            val fourthConst = findViewById<TextView>(R.id.textView24)
                            fourthConst.text = getString(R.string.none, "4th")

                            val fifthConst = findViewById<TextView>(R.id.textView25)
                            fifthConst.text = getString(R.string.none, "5th")
                            Glide.clear(firstImage)
                            Glide.clear(secondImage)
                            Glide.clear(thirdImage)
                            Glide.clear(fourthImage)
                            Glide.clear(fifthImage)

                            val secondConst6 = findViewById<TextView>(R.id.textView62)
                            secondConst6.text = ""

                            val thirdConst6 = findViewById<TextView>(R.id.textView63)
                            thirdConst6.text = ""

                            val fourthConst6 = findViewById<TextView>(R.id.textView64)
                            fourthConst6.text = ""

                            val fifthConst6 = findViewById<TextView>(R.id.textView65)
                            fifthConst6.text = ""
                        }
                    }
                    loading.visibility = View.GONE
                }
            }

        }
    }
}
