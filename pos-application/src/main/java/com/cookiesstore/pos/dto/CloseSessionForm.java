package com.cookiesstore.pos.dto;

import java.util.List;
import java.util.ArrayList;

public class CloseSessionForm
 {
    private List<CloseSessionBreakdownForm> breakdowns = new ArrayList<>();
    private String drawerNote;

    public CloseSessionForm() {
    }


    public CloseSessionForm(List<CloseSessionBreakdownForm> breakdowns) {
        this.breakdowns = breakdowns == null ? new ArrayList<>() : new ArrayList<>(breakdowns);
    }

    public CloseSessionForm(List<CloseSessionBreakdownForm> breakdowns, String drawerNote) {
        this.breakdowns = breakdowns == null ? new ArrayList<>() : new ArrayList<>(breakdowns);
        this.drawerNote = drawerNote;
    }

    public List<CloseSessionBreakdownForm> getBreakdowns() {
        if (breakdowns == null) {
            breakdowns = new ArrayList<>();
        }
        return breakdowns;
    }

    public void setBreakdowns(List<CloseSessionBreakdownForm> breakdowns) {
        this.breakdowns = breakdowns == null ? new ArrayList<>() : breakdowns;
    }

    public String getDrawerNote() {
        return drawerNote;
    }

    public void setDrawerNote(String drawerNote) {
        this.drawerNote = drawerNote;
    }
}
