package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayListRendererTest {
    @Test
    void animationIsFrameRateIndependentForEquivalentElapsedTime() {
        float oneStep = ArrayListRenderer.advanceAnimation(0.0f, 1.0f, 0.1f, 7.0f);
        float manySteps = 0.0f;
        for (int i = 0; i < 5; i++) {
            manySteps = ArrayListRenderer.advanceAnimation(manySteps, 1.0f, 0.02f, 7.0f);
        }

        assertEquals(oneStep, manySteps, 0.0001f);
    }

    @Test
    void animationNeverOvershootsAndMovesTowardTarget() {
        float entering = ArrayListRenderer.advanceAnimation(0.2f, 1.0f, 0.1f, 21.0f);
        float leaving = ArrayListRenderer.advanceAnimation(0.8f, 0.0f, 0.1f, 21.0f);

        assertTrue(entering > 0.2f && entering < 1.0f);
        assertTrue(leaving < 0.8f && leaving > 0.0f);
    }

    @Test
    void invalidInputsCannotProduceNonFiniteAnimationState() {
        float result = ArrayListRenderer.advanceAnimation(Float.NaN, 1.0f, Float.NaN, 7.0f);

        assertTrue(Float.isFinite(result));
        assertTrue(result >= 0.0f && result <= 1.0f);
    }
}
