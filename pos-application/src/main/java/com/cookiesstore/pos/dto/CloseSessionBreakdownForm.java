package com.cookiesstore.pos.dto;



public class CloseSessionBreakdownForm {

    private Long currencyBreadownId;
    private Integer currencyBreakdonwCount;

    public CloseSessionBreakdownForm() {
    }

    public CloseSessionBreakdownForm(Long currencyBreadownId, Integer currencyBreakdonwCount) {
        this.currencyBreadownId = currencyBreadownId;
        this.currencyBreakdonwCount = currencyBreakdonwCount;
    }

    public Long getCurrencyBreadownId() {
        return currencyBreadownId;
    }

    public void setCurrencyBreadownId(Long currencyBreadownId) {
        this.currencyBreadownId = currencyBreadownId;
    }

    public Integer getCurrencyBreakdonwCount() {
        return currencyBreakdonwCount;
    }

    public void setCurrencyBreakdonwCount(Integer currencyBreakdonwCount) {
        this.currencyBreakdonwCount = currencyBreakdonwCount;
    }

}    
