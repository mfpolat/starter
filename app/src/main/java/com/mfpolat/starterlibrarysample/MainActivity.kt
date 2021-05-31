package com.mfpolat.starterlibrarysample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mfpolat.starter.base.BaseListener

class MainActivity : AppCompatActivity() ,BaseListener{


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        

    }

    override fun listener(event: Any) {
        super.listener(event)
    }
}
