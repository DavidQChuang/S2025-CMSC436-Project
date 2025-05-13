package com.example.groupproject.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.groupproject.R
import com.example.groupproject.model.FinanceModel
import com.example.groupproject.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val model: FinanceModel,
    private var transactions: MutableList<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountText: TextView = itemView.findViewById(R.id.tvAmount)
        private val categoryText: TextView = itemView.findViewById(R.id.tvCategory)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        private val iconImage: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(transaction: Transaction) {
            amountText.text = String.format("${model.symbol}%.2f", transaction.amount)
            categoryText.text = transaction.category
            dateText.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(transaction.date)

//            val iconRes = when (transaction.category.toLowerCase(Locale.ROOT)) {
//                "food" -> R.drawable.ic_food
//                "transport" -> R.drawable.ic_transport
//                "shopping" -> R.drawable.ic_shopping
//                "entertainment" -> R.drawable.ic_entertainment
//                "bills" -> R.drawable.ic_bills
//                else -> R.drawable.ic_other
//            }
//            iconImage.setImageResource(iconRes)

            itemView.setOnClickListener { onItemClick(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    public fun addTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions.sortedByDescending { it.date }.toMutableList()
        notifyDataSetChanged()
    }
}