package com.example.groupproject.view

import android.app.AlertDialog
import com.example.groupproject.R
import com.example.groupproject.controller.FinanceController
import com.example.groupproject.model.*

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.LayoutInflater;
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar

class TransactionsActivity : AppCompatActivity() {
    private lateinit var model: FinanceModel
    private lateinit var controller: FinanceController

    private lateinit var adapter: TransactionAdapter

    private lateinit var emptyState: TextView
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var addTransaction: FloatingActionButton
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        // Get UI elements and set up adapter & recycler
        getUIElements()
        setupRecyclerView()

        // Set up model & controller
        model = FinanceModel(applicationContext)
        controller = FinanceController(model, this)

        // Set up the transaction list RecyclerView (adapter + divider) -- Agus
        setupRecyclerView()

        // Setup plus button
        addTransaction.setOnClickListener {
            showAddTransactionDialog()
        }

        // Set up transactions view
        loadTransactions()

        // Set up ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    public fun loadTransactions() {// -- Agus
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        controller.loadTransactions { transactions ->
            // if transactions failed to load, set emptyState text accordingly
            if(transactions == null) {
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                emptyState.text = getString(R.string.empty_state_error)
            }
            else {
                emptyState.text = getString(R.string.empty_state)

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (transactions.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        transactionsRecyclerView.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        transactionsRecyclerView.visibility = View.VISIBLE
                        adapter.addTransactions(transactions)
                        transactionsRecyclerView.adapter = adapter
                    }
                }
            }
        }
    }

    ///////////////////////////////////
    // Misc

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(model, mutableListOf(
            Transaction("asdf", 100f, "Food", Calendar.getInstance().time, "stuff")
        )) { transaction ->
            showTransactionDetails(transaction)
        }

        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionsRecyclerView.adapter = adapter
        transactionsRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    // Show an alert with the details of the clicked item
    private fun showTransactionDetails(transaction: Transaction) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val s = model.symbol

        AlertDialog.Builder(this)
            .setTitle("Transaction Details")
            .setMessage(
                "Amount: $s${transaction.amount}\n" +
                        "Category: ${transaction.category}\n" +
                        "Date: ${dateFormat.format(transaction.date)}\n" +
                        "Description: ${transaction.description}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)

        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val descriptionText = dialogView.findViewById<EditText>(R.id.descriptionText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Transaction")
            .setPositiveButton("Cancel", null)
            .create()

        // TODO: Create a transaction and save it to Firebase when the button is clicked,
        //  then update transactions view -- Agus
        val addTransactionButton = dialogView.findViewById<FloatingActionButton>(R.id.addTransactionDialog)
        addTransactionButton.setOnClickListener {
            Log.i("TransactionsActivity", "Add transaction button presssed")

            val amount = amountEditText.text.toString().toFloatOrNull() ?: 0f
            val category = categorySpinner.selectedItem.toString()
            val cal = Calendar.getInstance()
            cal.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val date = cal.time
            val description = descriptionText.text.toString()

            val txn = Transaction("", amount, category, date, description)
            controller.addTransaction(txn) { success ->
                runOnUiThread {
                    if (success) {
                        loadTransactions()
                        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun getUIElements() {
        // Budget
        emptyState = findViewById(R.id.emptyState)
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        addTransaction = findViewById(R.id.addTransaction)

        // Ad view
        adView = findViewById<AdView>(R.id.adView)
    }
}