package com.anwesh.uiprojects.linkedsquareblockview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.squarerotblockview.SquareRotBlockView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquareRotBlockView.create(this)
    }
}
