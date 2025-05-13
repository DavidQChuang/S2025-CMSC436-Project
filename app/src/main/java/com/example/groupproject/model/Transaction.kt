package com.example.groupproject.model

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Float,
    val category: String,
    val date: Date,
    val description: String
) {
    constructor() : this("", 0f, "", Date(), "") { }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "amount" to amount,
            "category" to category,
            "date" to date,
            "description" to description
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Transaction {
            return Transaction(
                id = map["id"] as? String ?: "",
                amount = map["amount"] as Float,
                category = map["category"] as String,
                date = map["date"] as Date,
                description = map["description"] as String
            )
        }
    }
}