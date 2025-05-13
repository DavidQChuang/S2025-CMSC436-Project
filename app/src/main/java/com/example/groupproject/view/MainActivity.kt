package com.example.groupproject.view

import android.Manifest
import com.example.groupproject.R
import com.example.groupproject.controller.FinanceController
import com.example.groupproject.model.FinanceModel

import android.util.Log
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.max


class MainActivity : AppCompatActivity() {
    // mvc
    private lateinit var model: FinanceModel
    private lateinit var controller: FinanceController

    // ui
    private lateinit var budgetLeftText: TextView  // $2000
    private lateinit var budgetUsedText: TextView  // $1500 of $2000
    private lateinit var budgetRemainingTextMain: TextView  // Remaining: $500
    private lateinit var budgetProgressBar: ProgressBar

    private lateinit var totalSpentText: TextView  // $500

    private lateinit var savingsText: TextView  // $750 of $2000
    private lateinit var savingsProgressBar: ProgressBar

    private lateinit var adView: AdView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var viewTransactionsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get UI elements
        getUIElements()

        // Set up model & controller
        model = FinanceModel(applicationContext)
        controller = FinanceController(model, this)

        setBudget(0, model.budgetGoal)
        setSpending(0)
        setSavings(model.budgetGoal, model.savingsGoal)

        // Set up navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> startActivity(Intent(this, TransactionsActivity::class.java))
                R.id.nav_budget -> startActivity(Intent(this, BudgetActivity::class.java))
            }
            true
        }

        viewTransactionsButton.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }

        // Set up currency spinner
        val currencySpinner: Spinner = findViewById(R.id.currency_spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.currency_options,
            android.R.layout.simple_spinner_item
        )
        currencySpinner.adapter = adapter
        // Load currency options from strings.xml
        controller.loadSymbol { symbol ->
            currencySpinner.setSelection(adapter.getPosition(symbol))
        }

        // Setup geolocate button
        val context = this
        findViewById<ImageButton>(R.id.geolocateButton).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.i("MainActivity", "Geolocating.")
                controller.checkLocationPermissions()

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(applicationContext, "No permission for currency geolocation.", Toast.LENGTH_SHORT).show()
                    Log.i("MainActivity", "Geolocating failed.")
                    return
                }

                controller.getCurrencySymbol { currency ->
                    var currencyPos = adapter.getPosition(currency.symbol)
                    Log.i("MainActivity", "Setting currency to ${currency.symbol}.")

                    if(currencyPos == -1){
                        adapter.add(currency.symbol)
                        currencyPos = adapter.getPosition(currency.symbol)
                    }

                    currencySpinner.setSelection(currencyPos)
                }
            }
        })

        // Handle currency selection event
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Log.i("MainActivity", "Selecting currency.")

                val selectedCurrency = parent.getItemAtPosition(position).toString()

                controller.saveSymbol(selectedCurrency, {})
                resetUI()
                Toast.makeText(applicationContext, "Setting currency to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Set up ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onResume(){
        super.onResume()

        // Set up UI elements in MainActivity using info from Model
        controller.loadBudget { budgetMax ->
            controller.loadTransactions { txns ->
                var totalSpent = -1

                if(txns != null) {
                    totalSpent = txns.sumOf { it.amount.toInt() }
                }

                runOnUiThread {
                    setBudget(totalSpent, budgetMax)
                    setSpending(totalSpent)
                    setSavings(budgetMax-totalSpent, model.savingsGoal)
                }
            }
        }
    }

    ////////////////////////////////
    // View API for controller use
    public fun resetUI() {
        var budgetMax = budgetProgressBar.max
        var totalSpent = budgetMax - budgetProgressBar.progress

        setBudget(totalSpent, budgetMax)
        setSpending(totalSpent)
        setSavings(budgetMax-totalSpent, model.savingsGoal)
    }

    public fun setBudget(budgetUsed: Int, budgetMax: Int) {
        Log.i("MainActivity", "Set budget: $${budgetUsed} of $${budgetMax}")
        val s = model.symbol

        budgetLeftText.text = "$s$budgetMax"
        budgetUsedText.text = "$s$budgetUsed of $s$budgetMax"
        budgetRemainingTextMain.text = "Remaining: $s${budgetMax-budgetUsed}"

        budgetProgressBar.max = max(budgetMax,1)
        budgetProgressBar.progress = budgetMax - budgetUsed
        if(budgetMax <= 0) {
            budgetProgressBar.progress = 1
        }
    }

    public fun setSpending(totalSpent: Int) {
        val s = model.symbol

        Log.i("MainActivity", "Set spending: $s${totalSpent}")
        totalSpentText.text = "$s${totalSpent}"
    }

    public fun setSavings(savings: Int, savingsGoal: Int) {
        Log.i("MainActivity", "Set savings: \$${savings} of \$${savingsGoal}")

        val s = model.symbol

        savingsText.text = "$s${savings} of $s${savingsGoal}"

        savingsProgressBar.max = max(savingsGoal,1)
        savingsProgressBar.progress = savings
        if(savingsGoal <= 0) {
            budgetProgressBar.progress = 1
        }
    }

    ///////////////////////////////////
    // Misc
    private fun getUIElements() {
        // Budget
        budgetLeftText = findViewById<TextView>(R.id.budgetLeftTextMain)
        budgetUsedText =  findViewById<TextView>(R.id.budgetUsedTextMain)
        budgetRemainingTextMain = findViewById(R.id.budgetRemainingTextMain)
        budgetProgressBar = findViewById<ProgressBar>(R.id.budgetProgressBarMain)

        // Spending
        totalSpentText = findViewById<TextView>(R.id.totalSpentText)

        // Savings
        savingsText = findViewById<TextView>(R.id.savingsText)
        savingsProgressBar = findViewById<ProgressBar>(R.id.savingsProgressBar)

        // Nav and ad view
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        adView = findViewById<AdView>(R.id.adView)
        viewTransactionsButton = findViewById(R.id.viewTransactionsButton)
    }
}