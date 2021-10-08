package com.example.dadac.testrosbridge;


import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dwayne.monitor.Main2;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityScenarioRule<Main2> mActivityTestRule =
            new ActivityScenarioRule<Main2>(Main2.class);

    @Test
    public void useAppContext() {
        ActivityScenario activityScenario = ActivityScenario.launch(Main2.class);
        activityScenario.moveToState(Lifecycle.State.STARTED);
    }
}
