package com.chenjimou.braceletdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.chenjimou.braceletdemo.R;
import com.chenjimou.braceletdemo.service.BraceletServiceConnection;

import androidx.annotation.NonNull;

public class LoadAnimationDialog extends Dialog
{
    private LoadAnimationDialog(@NonNull Context context)
    {
        super(context);
    }

    public static LoadAnimationDialog showDialog(Context context, String text)
    {
        LoadAnimationDialog dialog = new LoadAnimationDialog(context);
        dialog.setContentView(R.layout.dialog_animation_load);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView textView = dialog.findViewById(R.id.text_LoadAnimationDialog);
        textView.setText(text);
        return dialog;
    }
}
