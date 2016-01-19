package io.github.stevenrudenko.iot.sample.ui.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

/** FAB scroll behavior. */
public class FabScrollBehavior extends FloatingActionButton.Behavior {
    /** FAB animation interpolator. */
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    /** Animation duration. */
    private final long animationDuration;

    /** Indicates whether FAB animating out. */
    private boolean animatingOut = false;

    public FabScrollBehavior(Context context, AttributeSet attrs) {
        super();
        animationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout,
            final FloatingActionButton child,
            final View directTargetChild,
            final View target, final int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout,
                child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout,
            final FloatingActionButton child,
            final View target, final int dxConsumed, final int dyConsumed,
            final int dxUnconsumed, final int dyUnconsumed) {

        super.onNestedScroll(coordinatorLayout, child, target,
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (dyConsumed > 0 && !this.animatingOut && child.getVisibility() == View.VISIBLE) {
            // User scrolled down and the FAB is currently visible -> hide the FAB
            animateOut(child);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            // User scrolled up and the FAB is currently not visible -> show the FAB
            animateIn(child);
        }
    }

    /**
     * Same animation that FloatingActionButton.Behavior uses to hide
     * the FAB when the AppBarLayout exits. */
    private void animateOut(final FloatingActionButton button) {
        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) button.getLayoutParams();
        final int fabBottom = lp.bottomMargin;
        ViewCompat.animate(button).setDuration(animationDuration)
                .translationY(button.getHeight() + fabBottom)
                .setInterpolator(INTERPOLATOR)
                .setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        FabScrollBehavior.this.animatingOut = true;
                    }

                    public void onAnimationCancel(View view) {
                        FabScrollBehavior.this.animatingOut = false;
                    }

                    public void onAnimationEnd(View view) {
                        FabScrollBehavior.this.animatingOut = false;
                        view.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    /**
     * Same animation that FloatingActionButton.Behavior uses
     * to show the FAB when the AppBarLayout enters.
     */
    private void animateIn(FloatingActionButton button) {
        button.setVisibility(View.VISIBLE);
        ViewCompat.animate(button).setDuration(animationDuration)
                .translationY(0.0F)
                .scaleX(1.0F)
                .scaleY(1.0F)
                .alpha(1.0F)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();
    }
}
