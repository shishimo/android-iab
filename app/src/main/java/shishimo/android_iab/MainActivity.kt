package shishimo.android_iab

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient

class MainActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener, SkuDetailsResponseListener {

    private val TAG: String = "SHISHIMO01"

    lateinit private var statusTextView : TextView
    lateinit private var priceTextView : TextView
    lateinit private var buyButton : Button

    lateinit private var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.text_view_purchase_status)
        priceTextView = findViewById(R.id.text_view_price)
        buyButton = findViewById(R.id.button_buy)

        buyButton.setOnClickListener {
            Log.d(TAG, "push a buy button")
        }

        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        billingClient.startConnection(this)

    }

    // アプリ終了時に呼ばれる
    override fun onDestroy() {
        billingClient.endConnection()
        super.onDestroy()
    }

    // BillingClientStateListener

    // セットアップの終了時に呼ばれる
    override fun onBillingSetupFinished(@BillingResponse resultCode: Int) {
        if (resultCode == BillingClient.BillingResponse.OK) {
            Log.d(TAG, "Billing Setup OK: $resultCode")

            val isSubscriptionSupported = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
            if (isSubscriptionSupported == BillingResponse.OK) {
                getPurchases()
            } else {
                Log.d(TAG, "Billing Setup OK: $resultCode")
            }
        } else {
            Log.d(TAG, "Billing Setup Error: $resultCode")
        }
    }

    // 切断時に呼ばれる
    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "BillingServiceDisconnected")
    }

    // PurchasesUpdatedListener

    override fun onPurchasesUpdated(@BillingResponse responseCode: Int, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated")
        if (responseCode == BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d(TAG, "Purchase: $purchase")
            }
        } else if (responseCode == BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user canceling the purchase flow.
            Log.d(TAG, "Canceled: $responseCode")
        } else {
            // Handle any other error codes.
            Log.d(TAG, "Error: $responseCode")
        }
    }

    // SkuDetailsResponseListener

    override fun onSkuDetailsResponse(@BillingResponse responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
        Log.d(TAG, "onSkuDetailsResponse: $responseCode")
        if (responseCode == BillingResponse.OK) {
            if (skuDetailsList != null) {
                Log.d(TAG, "SKUList is ${skuDetailsList}")
                for (detail in skuDetailsList) {
                    Log.d(TAG, "${detail.sku}")
                }
            } else {
                Log.d(TAG, "SKUList is null")
            }
        }
    }

    // Private

    // 購入しているか確認する。
    fun getPurchases() {
        if (billingClient.isReady) {
            val skuList: List<String> = listOf("shishimo.android_iab.item.01")
            var params: SkuDetailsParams.Builder = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build(), this)
        }
    }

}
