package com.larryhsiao.nyx.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.larryhsiao.nyx.R;
import com.larryhsiao.nyx.base.JotFragment;

import java.util.Collections;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.android.billingclient.api.BillingClient.SkuType.SUBS;

/**
 * Fragment for indicates user to subscribe the cloud functions.
 */
public class PurchaseFragment extends JotFragment implements
    BillingClientStateListener,
    PurchasesUpdatedListener
{
    private BillingClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(this)
            .build();
        client.startConnection(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.block_purchase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.blockPurchase_functionName))
            .setText(R.string.Cloud_syncs);
        ((TextView) view.findViewById(R.id.blockPurchase_description))
            .setText(R.string.Subscribe_to_enable_the_cloud_syncing_and_all_the_smart_functionns);

        Button purchaseBtn = view.findViewById(R.id.blockPurchase_purchaseButton);
        purchaseBtn.setText(R.string.Subscribe);
        purchaseBtn.setOnClickListener(it -> querySku());
    }

    private void querySku() {
        client.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setSkusList(Collections.singletonList("premium"))
                .setType(SUBS)
                .build(),
            this::handleAvailableSku
        );
    }

    private void handleAvailableSku(BillingResult billingResult, List<SkuDetails> list) {
        if (billingResult.getResponseCode() == OK) {
            for (SkuDetails skuDetails : list) {
                if ("premium".equals(skuDetails.getSku())) {
                    launchPurchase(skuDetails);
                    return;
                }
            }
        }
        Toast.makeText(getContext(), R.string.appError_unknown, LENGTH_SHORT).show();
    }

    private void launchPurchase(SkuDetails sku) {
        client.launchBillingFlow(
            getActivity(),
            BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()
        );
    }

    @Override
    public void onBillingSetupFinished(BillingResult res) {
        if (res.getResponseCode() == OK) {
            client.querySkuDetailsAsync(
                SkuDetailsParams.newBuilder()
                    .setSkusList(Collections.singletonList("premium"))
                    .setType(SUBS)
                    .build(),
                this::updatePrice
            );
        }
    }

    private void updatePrice(BillingResult res, List<SkuDetails> list) {
        Button purchaseBtn = getView().findViewById(R.id.blockPurchase_purchaseButton);
        if (res.getResponseCode() == OK) {
            for (SkuDetails skuDetails : list) {
                if ("premium".equals(skuDetails.getSku())) {
                    purchaseBtn.setText(getString(
                        R.string.Subscribe____month,
                        skuDetails.getPrice())
                    );
                    return;
                }
            }
        }
        purchaseBtn.setEnabled(false);
        purchaseBtn.setText(R.string.error);
        purchaseBtn.setOnClickListener(null);
    }

    @Override
    public void onBillingServiceDisconnected() {
    }

    @Override
    public void onPurchasesUpdated(BillingResult res, @Nullable List<Purchase> list) {
        Fragment parent = getParentFragment();
        if (parent instanceof PurchasesUpdatedListener) {
            ((PurchasesUpdatedListener) parent).onPurchasesUpdated(res, list);
        }
    }
}
