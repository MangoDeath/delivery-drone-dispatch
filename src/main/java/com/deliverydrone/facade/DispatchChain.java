package com.deliverydrone.facade;

import com.deliverydrone.shared.exception.IllegalStateTransitionException;

public class DispatchChain {
    private final String chainId;
    private ChainStatus status;

    public DispatchChain(String chainId, ChainStatus status) {
        this.chainId = chainId;
        this.status = status;
    }

    public void moveToPendingC() {
        change(ChainStatus.PENDING_B, ChainStatus.PENDING_C);
    }

    public void complete() {
        change(ChainStatus.PENDING_C, ChainStatus.COMPLETED);
    }

    public void compensate() {
        if (status != ChainStatus.PENDING_B && status != ChainStatus.PENDING_C) {
            throw new IllegalStateTransitionException("DispatchChain", status.name(),
                    ChainStatus.COMPENSATED.name());
        }
        status = ChainStatus.COMPENSATED;
    }

    private void change(ChainStatus expected, ChainStatus next) {
        if (status != expected) {
            throw new IllegalStateTransitionException("DispatchChain", status.name(), next.name());
        }
        status = next;
    }

    public String getChainId() {
        return chainId;
    }

    public ChainStatus getStatus() {
        return status;
    }

    public String toString() {
        return chainId + ": " + status;
    }
}
