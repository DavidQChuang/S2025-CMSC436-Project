package com.example.groupproject.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.database.FirebaseDatabase

class FinanceModel (context: Context) {
    private val db = FirebaseDatabase.getInstance().reference
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

    /////////////////////////////
    // Local persistent data
    var symbol: String
        get() = prefs.getString("symbol", "$") ?: "$" // default: $
        set(symbol) = prefs.edit { putString("symbol", symbol) }

    var savingsGoal: Int
        get() = prefs.getInt("savings_goal", 0) // default: 0
        set(goal) = prefs.edit { putInt("savings_goal", goal) }

    var budgetGoal: Int
        get() = prefs.getInt("budget", 0) // default: 0
        set(limit) = prefs.edit { putInt("budget", limit) }

    /////////////////////////////
    // Remote persistent data
    // Save transaction to Firebase
    fun saveTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        Log.i("FinanceModel", "Saving transaction.")
        val txnRef = db.child("transactions").push()
        val txnWithId = transaction.copy(id = txnRef.key ?: "")
        txnRef.setValue(txnWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { error ->
                onComplete(false)
                Log.e("FinanceModel", "Firebase failed to add a transaction.")
            }
    }

    // Get transactions from Firebase
    fun getTransactions(callback: (List<Transaction>?) -> Unit) {
        Log.i("FinanceModel", "Getting transactions.")
        db.child("transactions")
            .get()
            .addOnSuccessListener { snapshot ->
                val txns = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                callback(txns)
            }
            .addOnFailureListener {
                callback(null)
                Log.e("FinanceModel", "Firebase failed to load transactions.")
            }
    }
}