package com.example.groupproject.view

import com.example.groupproject.R
import com.example.groupproject.controller.FinanceController
import com.example.groupproject.model.FinanceModel

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    // mvc
    private lateinit var model: FinanceModel
    private lateinit var controller: FinanceController

    // ui
    private lateinit var budgetLeftText: TextView  // $2000
    private lateinit var budgetUsedText: TextView  // $1500 of $2000
    private lateinit var budgetProgressBar: ProgressBar

    private lateinit var totalSpentText: TextView  // $500

    private lateinit var savingsText: TextView  // $750 of $2000
    private lateinit var savingsProgressBar: ProgressBar

    private lateinit var adView: AdView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get UI elements
        getUIElements()

        // Set up model & controller
        model = FinanceModel(applicationContext)
        controller = FinanceController(model, this)

        // TODO: Set up UI elements in MainActivity using info from Model

        // Set up navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> startActivity(Intent(this, TransactionsActivity::class.java))
                R.id.nav_budget -> startActivity(Intent(this, BudgetActivity::class.java))
            }
            true
        }

        // Set up ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    ////////////////////////////////
    // View API for controller use
    public fun setBudget(budgetUsed: Int, budgetMax: Int) {
        budgetLeftText.text = "$${budgetMax}"
        budgetUsedText.text = "$${budgetUsed} of $${budgetMax}"

        budgetProgressBar.progress = budgetUsed
        budgetProgressBar.max = budgetMax
    }

    public fun setSpending(totalSpent: Int) {
        totalSpentText.text = "$${totalSpent}"
    }

    public fun setSavings(savings: Int, savingsGoal: Int) {
        savingsText.text = "$${savings} of $${savingsGoal}"

        savingsProgressBar.progress = savings
        savingsProgressBar.max = savingsGoal
    }

    ///////////////////////////////////
    // Misc
    private fun getUIElements() {
        // Budget
        budgetLeftText = findViewById<TextView>(R.id.budgetLeftTextMain)
        budgetUsedText =  findViewById<TextView>(R.id.budgetUsedTextMain)
        budgetProgressBar = findViewById<ProgressBar>(R.id.budgetProgressBarMain)

        // Spending
        totalSpentText = findViewById<TextView>(R.id.totalSpentText)

        // Savings
        savingsText = findViewById<TextView>(R.id.savingsText)
        savingsProgressBar = findViewById<ProgressBar>(R.id.savingsProgressBar)

        // Nav and ad view
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        adView = findViewById<AdView>(R.id.adView)
    }
}