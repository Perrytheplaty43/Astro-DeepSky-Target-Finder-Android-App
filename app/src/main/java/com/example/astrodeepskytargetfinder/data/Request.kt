package com.example.astrodeepskytargetfinder.data

import android.util.Log
import java.net.URL

class Request(private val url: String) {
     fun run(): String {
        val listJson = URL(url).readText()
        return listJson
    }
}