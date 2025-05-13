package com.example.groupproject.controller

import android.content.Context
import com.example.groupproject.model.FinanceModel
import com.example.groupproject.model.Transaction

class FinanceController(private val model: FinanceModel,private val context: Context) {

    // Load all transactions from Firebase
    fun loadTransactions(onLoaded: (List<Transaction>) -> Unit) {
        model.getTransactions { txns ->
            onLoaded(txns)
        }
    }

    // Save a new transaction to Firebase
    fun addTransaction(txn: Transaction, onComplete: (Boolean) -> Unit) {
        model.saveTransaction(txn) { success ->
            onComplete(success)
        }
    }

    // Load the current budgetGoal from SharedPreferences
    fun loadBudget(onLoaded: (Int) -> Unit) {
        onLoaded(model.budgetGoal)
    }

    // Save a new budgetGoal
    fun saveBudget(limit: Int, onComplete: () -> Unit) {
        model.budgetGoal = limit
        onComplete()
    }
}
