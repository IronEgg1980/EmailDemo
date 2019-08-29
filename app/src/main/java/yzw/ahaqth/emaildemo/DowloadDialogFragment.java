package yzw.ahaqth.emaildemo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

public class DowloadDialogFragment extends DialogFragment {
    private TextView title;
    private ProgressBar progress;
    private TextView messageCount;
    private TextView messagePersent;

    public static DowloadDialogFragment newInstant(){
        DowloadDialogFragment  fragment = new DowloadDialogFragment();
        return fragment;
    }

    public void setTotalFileSize(int totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    private int totalFileSize;

    private void initialView(View view) {
        title = view.findViewById(R.id.title);
        progress = view.findViewById(R.id.progress);
        messageCount = view.findViewById(R.id.messageCount);
        messagePersent = view.findViewById(R.id.messagePersent);
    }

    public void changeMessage(int count) {
        if(messageCount == null || messagePersent == null || progress==null)
            return;
        float value = totalFileSize == 0 ? 0 : count * 100f / totalFileSize;
        int progressValue = totalFileSize == 0?0:count * 100 / totalFileSize;
        String s = count + "/" + totalFileSize;
        String percentS = String.format(Locale.CHINA, "%.2f%s", value, "%");
        messageCount.setText(s);
        messagePersent.setText(percentS);
        progress.setProgress(progressValue);
    }

    public void setTitle(String text) {
        if(title!=null)
            title.setText(text);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_dialog_layout, container, false);
        initialView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog!=null){
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
        }
    }
}
