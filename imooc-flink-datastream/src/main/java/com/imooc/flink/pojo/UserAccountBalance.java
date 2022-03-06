package com.imooc.flink.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author jared
 * @since 2022/2/12
 */
public class UserAccountBalance {
    private final long userId;
    /**
     * 1 - asset / 2 - liability / 3 - equity
     */
    private final int type;
    private final String balance;
    private final int unitId;

    @JsonCreator
    public UserAccountBalance(@JsonProperty("userId") long userId,
                              @JsonProperty("type") int type,
                              @JsonProperty("balance") String balance,
                              @JsonProperty("unitId") int unitId) {
        this.userId = userId;
        this.type = type;
        this.balance = balance;
        this.unitId = unitId;
    }

    public long getUserId() {
        return userId;
    }

    public int getType() {
        return type;
    }

    public String getBalance() {
        return balance;
    }

    @JsonIgnore
    public BigDecimal getBalanceInBigDecimal() {
        return new BigDecimal(balance);
    }

    public int getUnitId() {
        return unitId;
    }

    @Override
    public String toString() {
        return "UserAccountBalance{" +
                "userId=" + userId +
                ", type=" + type +
                ", balance='" + balance + '\'' +
                ", unitId=" + unitId +
                '}';
    }
}
