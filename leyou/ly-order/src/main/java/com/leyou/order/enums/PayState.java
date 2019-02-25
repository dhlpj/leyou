package com.leyou.order.enums;

public enum PayState {
    /**
     * 支付交易状态分为3种情况：
     * <p>
     * - 0，通信失败或未支付，需要重新查询。
     * - 1，支付成功
     * - 2，支付失败
     */
    NOT_PAY(0), SUCCESS(1), FAIL(2);
    private Integer state;

    PayState(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }
}