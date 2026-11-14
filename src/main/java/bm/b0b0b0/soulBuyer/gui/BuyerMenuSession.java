package bm.b0b0b0.soulBuyer.gui;

import bm.b0b0b0.soulBuyer.model.BuyerPayoutMode;

public record BuyerMenuSession(String categoryFilter, String sortMode, int page, BuyerPayoutMode payoutMode) {

    public static BuyerMenuSession empty() {
        return new BuyerMenuSession("", BuyerSortMode.PRICE_DESC, 0, BuyerPayoutMode.VAULT);
    }

    public static BuyerMenuSession withPayout(BuyerPayoutMode payoutMode) {
        BuyerPayoutMode mode = payoutMode == null ? BuyerPayoutMode.VAULT : payoutMode;
        return new BuyerMenuSession("", BuyerSortMode.PRICE_DESC, 0, mode);
    }

    public BuyerMenuSession withPageState(String filter, String sort, int pageIndex) {
        return new BuyerMenuSession(
                filter == null ? "" : filter,
                BuyerSortMode.normalize(sort == null || sort.isBlank() ? BuyerSortMode.PRICE_DESC : sort),
                Math.max(0, pageIndex),
                payoutMode == null ? BuyerPayoutMode.VAULT : payoutMode
        );
    }
}
