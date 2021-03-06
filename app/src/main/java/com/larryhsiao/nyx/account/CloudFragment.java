package com.larryhsiao.nyx.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.larryhsiao.nyx.R;
import com.larryhsiao.nyx.base.JotFragment;
import com.larryhsiao.nyx.sync.SyncsFragment;
import com.larryhsiao.nyx.sync.SyncService;

import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED;
import static com.android.billingclient.api.BillingClient.SkuType.SUBS;
import static com.android.billingclient.api.Purchase.PurchaseState.PURCHASED;

/**
 * Cloud syc page.
 */
public class CloudFragment extends JotFragment
    implements PurchasesUpdatedListener, BillingClientStateListener {
    private BillingClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.page_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        client = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(this)
            .build();
        client.startConnection(this);
    }

    @Override
    public void onPurchasesUpdated(BillingResult res, @Nullable List<Purchase> purchases) {
        if (res.getResponseCode() == OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (res.getResponseCode() == USER_CANCELED) {
            // @todo #190 Handle an error caused by a user cancelling the purchase flow.
        } else {
            // @todo #191 Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams param =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                client.acknowledgePurchase(param, res -> onSubscribed());
            }
        }
    }

    private void onSubscribed() {
        SyncService.enqueue(getContext());
        toSyncPage();
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == OK) {
            checkIfSubscribed();
        }
    }

    private void checkIfSubscribed() {
        List<String> skuList = new ArrayList<>();
        skuList.add("premium");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(SUBS);
        // @todo #195 Find out if the cached query state is reliable.
        List<Purchase> purchases = client.queryPurchases(SUBS).getPurchasesList();
        if (purchases != null) {
            checkPurchases(purchases);
        }
        // @todo #194 Record the subscribe at firebase for checking availability.
    }

    private void checkPurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == PURCHASED
                && "premium".equals(purchase.getSku())) {
                toSyncPage();
                return;
            }
        }
        toPurchase();
    }

    private void toSyncPage() {
        getChildFragmentManager().beginTransaction()
            .replace(R.id.account_pageContainer, new SyncsFragment())
            .commit();
    }

    private void toPurchase() {
        getChildFragmentManager().beginTransaction()
            .replace(R.id.account_pageContainer, new PurchaseFragment())
            .commit();
    }

    @Override
    public void onBillingServiceDisconnected() {

    }
}
