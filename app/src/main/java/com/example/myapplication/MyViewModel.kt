package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel() : ViewModel() {

    private val gameEngine = GameEngine()

    fun getGameEngine() : GameEngine {
        return gameEngine
    }

}