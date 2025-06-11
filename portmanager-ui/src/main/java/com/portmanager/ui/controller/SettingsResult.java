package com.portmanager.ui.controller;

import java.util.List;

public interface SettingsResult<T> {
    List<T> getData();
    void setData(List<T> data);

    List<T> collectResult();
}
