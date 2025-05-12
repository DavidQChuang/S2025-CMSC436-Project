package com.example.groupproject.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.database.FirebaseDatabase

class FinanceModel (context: Context) {
    private val db = FirebaseDatabase.getInstance().reference
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

    /////////////////////////////
    // Local persistent data
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
        val txnRef = db.child("transactions").push()
        val txnWithId = transaction.copy(id = txnRef.key ?: "")
        txnRef.setValue(txnWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Get transactions from Firebase
    fun getTransactions(callback: (List<Transaction>) -> Unit) {
        db.child("transactions")
            .get()
            .addOnSuccessListener { snapshot ->
                val txns = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                callback(txns)
            }
    }
}