package bm.b0b0b0.soulBuyer.api;

import bm.b0b0b0.soulBuyer.service.SaleDelivery;

public enum SoulBuyerSellDelivery {
    CHAT,
    ACTION_BAR,
    SILENT;

    SaleDelivery toInternal() {
        return switch (this) {
            case ACTION_BAR -> SaleDelivery.ACTION_BAR;
            case SILENT -> SaleDelivery.SILENT;
            default -> SaleDelivery.CHAT;
        };
    }
}
