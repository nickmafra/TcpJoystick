package com.nickmafra.tcpjoystick;

import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButtonPovView extends View implements JoyItemView {

    private static final String PRESS_PATTERN = "{\"P\":{\"Index\":${buttonIndex},\"Direction\":\"${direction}\",\"JNo\":${joyIndex}}}";
    private static final String RELEASE_PATTERN = "{\"P\":{\"Index\":${buttonIndex},\"Direction\":\"c\",\"JNo\":${joyIndex}}}";

    private final MainActivity mainActivity;
    private String buttonIndex;

    public JoyButtonPovView(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
    }

    public JoyButtonPovView(MainActivity mainActivity, @Nullable AttributeSet attrs) {
        super(mainActivity, attrs);
        this.mainActivity = mainActivity;
    }

    public JoyButtonPovView(MainActivity mainActivity, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                .replace("${joyIndex}", String.valueOf(mainActivity.getJoyIndex()))
                .replace("${buttonIndex}", buttonIndex);
    }

    @Override
    public void config(JoyButton joyButton) {
        setBackground(ContextCompat.getDrawable(mainActivity, R.drawable.pentagon_button));

        setButtonIndex(joyButton.getIndex());
        JoyButtonTouchListener listener = new JoyButtonTouchListener(mainActivity);
        listener.setPressData(applyPattern(PRESS_PATTERN).getBytes());
        listener.setReleaseData(applyPattern(RELEASE_PATTERN).getBytes());
        setOnTouchListener(listener);
    }
}
