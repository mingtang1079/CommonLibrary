package com.appbaselib.common;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Description: 简化TextWatcher
 * Created by tm on 2017/7/18 0018.
 */

public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void afterTextChanged(Editable mEditable) {

    }
}
