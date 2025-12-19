package com.example.vo;

import java.math.BigDecimal;

public class KillCourseVO {
   private Boolean killing;
    private Boolean  unbegin;
    private BigDecimal killPrice;
    private Long timeDiffMill;

    public Boolean getKilling() {
        return killing;
    }

    public void setKilling(Boolean killing) {
        this.killing = killing;
    }

    public Boolean getUnbegin() {
        return unbegin;
    }

    public void setUnbegin(Boolean unbegin) {
        this.unbegin = unbegin;
    }

    public BigDecimal getKillPrice() {
        return killPrice;
    }

    public void setKillPrice(BigDecimal killPrice) {
        this.killPrice = killPrice;
    }

    public Long getTimeDiffMill() {
        return timeDiffMill;
    }

    public void setTimeDiffMill(Long timeDiffMill) {
        this.timeDiffMill = timeDiffMill;
    }
}
