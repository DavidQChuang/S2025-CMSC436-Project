package com.example.groupproject.view

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groupproject.R
import com.example.groupproject.controller.FinanceController
import com.example.groupproject.model.FinanceModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class BudgetActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener  {
    private lateinit var model: FinanceModel
    private lateinit var controller: FinanceController

    private lateinit var budgetLeftText: TextView
    private lateinit var budgetSeekBar: SeekBar
    private lateinit var saveBudgetButton: Button

    private lateinit var budgetUsedText: TextView // Used: $${budgetUsed} of $${budgetMax}
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var budgetRemainingText: TextView

    private lateinit var adView: AdView

    private var budgetUsed: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // Store UI elements
        getUIElements()

        // Set up model & controller
        model = FinanceModel(applicationContext)
        controller = FinanceController(model, this)

        // Set up seek bar listener
        // TODO: retrieve value from model -- Agus
        controller.loadTransactions { txns ->
            budgetUsed = txns.sumOf { 
                it.amount.toInt() 
            }
            runOnUiThread {
                // re-draw the SeekBar/UI with the real “used” amount
                updateBudgetDisplay(budgetSeekBar.progress.toFloat())
            } 
        }

        updateBudgetDisplay(model.budgetGoal.toFloat())
        budgetSeekBar.setOnSeekBarChangeListener(this)

        // Set up save budget button
        // TODO: Save value of budget seek bar to model (see below) -- Agus
        //        saveBudgetButton.setOnClickListener(this)

        controller.loadBudget { limit ->
            budgetSeekBar.progress = limit
            updateBudgetDisplay(limit.toFloat())
        }
        
        saveBudgetButton.setOnClickListener {
            val newLimit = budgetSeekBar.progress
            controller.saveBudget(newLimit) {
                updateBudgetDisplay(newLimit.toFloat())
            }
        }



        // Set up ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    ////////////////////////////////
    // Controller
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            updateBudgetDisplay(progress.toFloat())
        }
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    ////////////////////////////////
    // View API for controller use
    private fun updateBudgetDisplay(progress: Float) {
        val budgetMax = progress.toInt()

        budgetLeftText.text = "$${budgetMax}"
        budgetUsedText.text =  "Used: $${budgetUsed} of $${budgetMax}"

        budgetProgressBar.max = budgetMax.toInt()
        budgetProgressBar.progress = budgetUsed

        budgetRemainingText.text = "Remaining: $${budgetMax - budgetUsed}"
    }

    ///////////////////////////////////
    // Misc
    private fun getUIElements() {
        // Card 1: Current / adjustment
        budgetLeftText = findViewById(R.id.budgetLeftText)
        budgetSeekBar =  findViewById(R.id.budgetSeekBar)
        saveBudgetButton = findViewById(R.id.saveBudgetButton)

        // Card 2: breakdown
        budgetUsedText = findViewById(R.id.budgetUsedText)
        budgetProgressBar =  findViewById(R.id.budgetProgressBar)
        budgetRemainingText = findViewById(R.id.budgetRemainingText)

        // Ad view
        adView = findViewById<AdView>(R.id.adView)
    }
}