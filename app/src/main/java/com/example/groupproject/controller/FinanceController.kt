package com.example.groupproject.controller

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.groupproject.model.FinanceModel
import com.example.groupproject.model.Transaction
import com.google.android.gms.location.LocationServices
import java.util.Currency
import java.util.Locale

class FinanceController(private val model: FinanceModel,private val context: Activity) {
    // Load the current symbol from SharedPreferences
    fun loadSymbol(onLoaded: (String) -> Unit) {
        onLoaded(model.symbol)
    }

    // Load the current symbol from SharedPreferences
    fun saveSymbol(symbol: String, onComplete: () -> Unit) {
        model.symbol = symbol
        onComplete()
    }

    // Load all transactions from Firebase
    fun loadTransactions(onLoaded: (List<Transaction>?) -> Unit) {
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

    // Load the current savingsGoal from SharedPreferences
    fun loadSavings(onLoaded: (Int) -> Unit) {
        onLoaded(model.savingsGoal)
    }

    // Save a new savingsGoal
    fun saveSavings(limit: Int, onComplete: () -> Unit) {
        model.savingsGoal = limit
        onComplete()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrencySymbol(callback: (Currency) -> Unit) {
        Log.i("FinanceController", "Getting location.")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            Log.i("FinanceController", "Checking if location is non-null.")
            if (location != null) {
                Log.i("FinanceController", "Location was non-null.")
                val latitude = location.latitude
                val longitude = location.longitude

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val countryCode = addresses[0].countryCode
                    val currency = Currency.getInstance(Locale("", countryCode))
                    Log.d("Currency", "Local Currency: ${currency.symbol}")

                    callback(currency)
                }
            }else{
                callback(Currency.getAvailableCurrencies().first())
            }
        }.addOnFailureListener {
            Log.e("LocationError", "Failed to get location: ${it.message}")
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    public fun checkLocationPermissions() {
        when {
            PermissionHelper.hasLocationPermissions(context) -> { }
            PermissionHelper.shouldShowPermissionRationale(context) -> {
                // Show explanation why permission is needed
                showPermissionRationale()
            }
            else -> {
                // Request the permissions
                PermissionHelper.requestLocationPermissions(
                    context,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(context)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location permissions to determine your local currency. Please grant the permissions.")
            .setPositiveButton("OK") { _, _ ->
                PermissionHelper.requestLocationPermissions(
                    context,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    context,
                    "Using default currency settings",
                    Toast.LENGTH_LONG
                ).show()
            }
            .create()
            .show()
    }
    object PermissionHelper {
        // Check if location permissions are granted
        fun hasLocationPermissions(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        // Request location permissions
        fun requestLocationPermissions(activity: Activity, requestCode: Int) {
            val permissions = arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }

        // Check if permission rationale should be shown
        fun shouldShowPermissionRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
}
