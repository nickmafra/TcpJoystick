package com.nickmafra.tcpjoystick;

import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButtonView extends AppCompatTextView implements JoyItemView {

    private static final String PRESS_PATTERN = "{\"B\":{\"Index\":${buttonIndex},\"Mode\":\"p\",\"JNo\":${joyIndex}}}";
    private static final String RELEASE_PATTERN = "{\"B\":{\"Index\":${buttonIndex},\"Mode\":\"r\",\"JNo\":${joyIndex}}}";

    private final MainActivity mainActivity;
    private int joyIndex;
    private String buttonIndex;
    
    public JoyButtonView(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
    }

    public JoyButtonView(MainActivity mainActivity, @Nullable AttributeSet attrs) {
        super(mainActivity, attrs);
        this.mainActivity = mainActivity;
    }

    public JoyButtonView(MainActivity mainActivity, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(mainActivity, attrs, defStyleAttr);
        this.mainActivity = mainActivity;
    }

    @Override
    public View asView() {
        return this;
    }

    @Override
    public void onResume() {
        // do nothing
    }

    @Override
    public void onPause() {
        // do nothing
    }

    public String applyPattern(String pattern) {
        return pattern
                .replace("${joyIndex}", String.valueOf(joyIndex))
                .replace("${buttonIndex}", buttonIndex);
    }

    @Override
    public void config(JoyButton joyButton) {
        if (joyButton.getFormat() == null)
            joyButton.setFormat("round");

        switch (joyButton.getFormat()) {
            case "rectangle":
                setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.rect_button));
                break;
            case "round":
            default:
                setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.round_button));
                break;
        }

        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        setText(joyButton.getText());
        setJoyIndex(mainActivity.getJoyIndex());
        setButtonIndex(joyButton.getIndex());
        JoyButtonTouchListener listener = new JoyButtonTouchListener(this);
        listener.setPressData(applyPattern(PRESS_PATTERN).getBytes());
        listener.setReleaseData(applyPattern(RELEASE_PATTERN).getBytes());
        setOnTouchListener(listener);
    }
}
