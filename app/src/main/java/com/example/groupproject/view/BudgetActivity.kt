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

    private lateinit var savingsText: TextView
    private lateinit var savingsProgressBar: ProgressBar

    private lateinit var savingsLeftText: TextView
    private lateinit var savingsSeekBar: SeekBar
    private lateinit var saveSavingsButton: Button

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

        // Initialize elements from model
        controller.loadTransactions { transactions ->
            budgetUsed = transactions?.sumOf { it.amount.toInt() } ?: 0

            // Load budget limit
            controller.loadBudget { budgetLimit ->
                runOnUiThread {
                    budgetSeekBar.progress = budgetLimit
                    updateBudgetDisplay(budgetLimit.toFloat())
                }
            }

            // Load savings goal
            controller.loadSavings { savingsGoal ->
                runOnUiThread {
                    savingsSeekBar.progress = savingsGoal
                    updateSavingsDisplay(savingsGoal.toFloat())
                }
            }
        }

        // Setup budget seek bar
        budgetSeekBar.setOnSeekBarChangeListener(this)
        saveBudgetButton.setOnClickListener {
            val newBudgetLimit = budgetSeekBar.progress
            controller.saveBudget(newBudgetLimit) {
                updateBudgetDisplay(newBudgetLimit.toFloat())
            }
        }

        // Setup savings seek bar
        savingsSeekBar.setOnSeekBarChangeListener(this)
        saveSavingsButton.setOnClickListener {
            val newSavingsGoal = savingsSeekBar.progress
            controller.saveSavings(newSavingsGoal) {
                updateSavingsDisplay(newSavingsGoal.toFloat())
            }
        }

        // Set up ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    ////////////////////////////////
    // Controller
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar?.id) {
            R.id.budgetSeekBar -> if (fromUser) updateBudgetDisplay(progress.toFloat())
            R.id.savingsSeekBar -> if (fromUser) updateSavingsDisplay(progress.toFloat())
        }
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    ////////////////////////////////
    // View API for controller use
    private fun updateBudgetDisplay(budgetMax: Float) {
        val max = budgetMax.toInt()
        val remaining = max - budgetUsed

        val s = model.symbol

        budgetLeftText.text = "$s$max"
        budgetUsedText.text = "Used: $s$budgetUsed of $s$max"
        budgetProgressBar.max = max
        budgetProgressBar.progress = budgetUsed
        budgetRemainingText.text = "Remaining: $s$remaining"

        // Update savings display since it depends on budget
        updateSavingsDisplay(savingsSeekBar.progress.toFloat())
    }

    private fun updateSavingsDisplay(savingsMax: Float) {
        val max = savingsMax.toInt()
        val currentSavings = budgetProgressBar.max - budgetUsed
        val progress = currentSavings.coerceAtMost(max)

        val s = model.symbol

        savingsLeftText.text = "$s$max"
        savingsText.text = "$s$currentSavings of $s$max"
        savingsProgressBar.max = max
        savingsProgressBar.progress = progress
    }

    ///////////////////////////////////
    // Misc
    private fun getUIElements() {
        // Card 1: breakdown
        budgetUsedText = findViewById(R.id.budgetUsedText)
        budgetProgressBar =  findViewById(R.id.budgetProgressBar)
        budgetRemainingText = findViewById(R.id.budgetRemainingText)

        savingsText = findViewById(R.id.savingsText2)
        savingsProgressBar = findViewById(R.id.savingsProgressBar2)

        // Card 2: Budget adjustment
        budgetLeftText = findViewById(R.id.budgetLeftText)
        budgetSeekBar =  findViewById(R.id.budgetSeekBar)
        saveBudgetButton = findViewById(R.id.saveBudgetButton)

        // Card 3: Savings adjustment
        savingsLeftText = findViewById(R.id.savingsLeftText)
        savingsSeekBar =  findViewById(R.id.savingsSeekBar)
        saveSavingsButton = findViewById(R.id.saveSavingsButton)

        // Ad view
        adView = findViewById<AdView>(R.id.adView)
    }
}