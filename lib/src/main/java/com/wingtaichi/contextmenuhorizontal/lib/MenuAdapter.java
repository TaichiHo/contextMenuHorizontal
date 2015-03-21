package com.wingtaichi.contextmenuhorizontal.lib;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.wingtaichi.contextmenuhorizontal.lib.interfaces.OnItemClickListener;
import com.wingtaichi.contextmenuhorizontal.lib.interfaces.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter {

    public static final int ANIMATION_DURATION_MILLIS = 100;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemClickListener mOnItemClickListenerCalled;
    private OnItemLongClickListener mOnItemLongClickListenerCalled;
    private Context mContext;
    private LinearLayout mMenuWrapper;
    private LinearLayout mTextWrapper;
    private View mClickedView;
    private List<MenuObject> mMenuObjects;
    private AnimatorSet mAnimatorSetHideMenu;
    private AnimatorSet mAnimatorSetShowMenu;
    private boolean mIsMenuOpen = false;
    private boolean mIsAnimationRun = false;
    private int mMenuItemSize;
    private int mAnimationDurationMilis = ANIMATION_DURATION_MILLIS;


    // Newly added attribute for horizontal layout
    private int mItemWidth;
    // Indicate the index of the middle item. In this case there are 5 items, so it is 2.
    private int mMiddleIndex = 2;

    public MenuAdapter(Context context, LinearLayout menuWrapper, LinearLayout textWrapper, List<MenuObject> menuObjects,
                       int actionBarHeight) {
        this.mContext = context;
        this.mMenuWrapper = menuWrapper;
        this.mTextWrapper = textWrapper;
        this.mMenuObjects = menuObjects;

/**
 /       Make menu looks better by setting toolbar height as itemSize.
 */
        this.mMenuItemSize = actionBarHeight;
        setViews();
        resetAnimations();
        mAnimatorSetShowMenu = setOpenCloseAnimation(false);
        mAnimatorSetHideMenu = setOpenCloseAnimation(true);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public int getItemCount() {
        return mMenuObjects.size();
    }

    /**
     * Creating views and filling to wrappers
     */
    private void setViews() {
        for (int i = 0; i < mMenuObjects.size(); i++) {
            MenuObject menuObject = mMenuObjects.get(i);
            mTextWrapper.addView(Utils.getItemTextView(mContext, menuObject, mMenuItemSize));
            mMenuWrapper.addView(Utils.getImageWrapper(mContext, menuObject, mMenuItemSize,
                    clickItem, longClickItem, i != mMenuObjects.size() - 1));

        }
    }

    /**
     * Get the width of each horizontal item. Divide the screen width by the number of items.
     *
     * @return
     */
    private int getItemWidth() {
        if (mItemWidth == 0) {
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            int w_screen = dm.widthPixels;
            int h_screen = dm.heightPixels;
            mItemWidth = w_screen / getItemCount();
            return mItemWidth;
        }
        return mItemWidth;
    }

    /**
     * Set starting params to vertical animations
     */
    private void resetVerticalAnimation(View view, boolean toTop) {
        if (!mIsMenuOpen) {
            ViewHelper.setRotation(view, 0);
            ViewHelper.setRotationY(view, 0);
            ViewHelper.setRotationX(view, 90);
        }
        ViewHelper.setPivotX(view, mMenuItemSize / 2);
        ViewHelper.setPivotY(view, !toTop ? 0 : mMenuItemSize);
    }

    /**
     * Set starting params to left animations
     */
    private void resetLeftAnimation(View view) {
        if (!mIsMenuOpen) {
            ViewHelper.setRotation(view, 0);
            ViewHelper.setRotationY(view, -90);
            ViewHelper.setRotationX(view, 0);
        }
        ViewHelper.setPivotX(view, getItemWidth());
        ViewHelper.setPivotY(view, getItemWidth() / 2);
    }

    /**
     * Set starting params to right animations
     */
    private void resetRightAnimation(View view) {
        if (!mIsMenuOpen) {
            ViewHelper.setRotation(view, 0);
            ViewHelper.setRotationY(view, 90);
            ViewHelper.setRotationX(view, 0);
        }
        ViewHelper.setPivotX(view, 0);
        ViewHelper.setPivotY(view, getItemWidth() / 2);
    }

    /**
     * Set starting params of the item showing up from bottom
     *
     * @param view
     */
    private void resetVerticalFromBottomAnimation(View view) {
        if (!mIsMenuOpen) {
            ViewHelper.setRotation(view, 0);
            ViewHelper.setRotationY(view, 0);
            ViewHelper.setRotationX(view, 90);
        }
        ViewHelper.setPivotX(view, 0);
        ViewHelper.setPivotY(view, mMenuItemSize);
    }

    /**
     * Set starting params to text animations
     */
    private void resetTextAnimation(View v) {
        ViewHelper.setAlpha(v, !mIsMenuOpen ? 0 : 1);
        ViewHelper.setTranslationX(v, !mIsMenuOpen ? mMenuItemSize : 0);
    }

    /**
     * Set starting params to all animations
     */
    private void resetAnimations() {
        for (int i = 0; i < getItemCount(); i++) {
            resetTextAnimation(mTextWrapper.getChildAt(i));
            if (i == mMiddleIndex) {
                resetVerticalFromBottomAnimation(mMenuWrapper.getChildAt(i));
            } else if (i < mMiddleIndex) {
                resetLeftAnimation(mMenuWrapper.getChildAt(i));
            } else {
                resetRightAnimation(mMenuWrapper.getChildAt(i));
            }
        }
    }

    /**
     * Creates Open / Close AnimatorSet
     */
    private AnimatorSet setOpenCloseAnimation(boolean isCloseAnimation) {
        List<Animator> textAnimations = new ArrayList<>();
        List<Animator> imageAnimations = new ArrayList<>();

        if (isCloseAnimation) {
            for (int i = 1; i + mMiddleIndex < getItemCount(); i++) {
                fillOpenClosingAnimationsParallely(true, textAnimations, imageAnimations, i);
            }
            fillOpenClosingAnimationsParallely(true, textAnimations, imageAnimations, 0);
        } else {
            fillOpenClosingAnimationsParallely(false, textAnimations, imageAnimations, 0);
            for (int i = 1; i + mMiddleIndex < getItemCount(); i++) {
                fillOpenClosingAnimationsParallely(false, textAnimations, imageAnimations, i);
            }
        }

        AnimatorSet textCloseAnimatorSet = new AnimatorSet();
        textCloseAnimatorSet.playSequentially(textAnimations);

        AnimatorSet imageCloseAnimatorSet = new AnimatorSet();
        imageCloseAnimatorSet.playSequentially(imageAnimations);

        AnimatorSet animatorFullSet = new AnimatorSet();
        animatorFullSet.playTogether(imageCloseAnimatorSet, textCloseAnimatorSet);
        animatorFullSet.setDuration(mAnimationDurationMilis);
        animatorFullSet.addListener(mCloseOpenAnimatorListener);
        animatorFullSet.setStartDelay(0);
        animatorFullSet.setInterpolator(new HesitateInterpolator());
        return animatorFullSet;
    }

    private void fillOpenClosingAnimationsParallely(boolean isCloseAnimation, List<Animator> textAnimations, List<Animator> imageAnimations, int offsetToMiddleIndex) {

        int wrapperPositionAfter = offsetToMiddleIndex + mMiddleIndex;
        int wrapperPositionBefore = mMiddleIndex - offsetToMiddleIndex;

        AnimatorSet textAnimator = new AnimatorSet();
        if (isCloseAnimation) {
            if (wrapperPositionAfter == mMiddleIndex) {
                Animator textAppearance =
                        AnimatorUtils.alfaDisappear(mTextWrapper.getChildAt(mMiddleIndex));
                Animator textTranslation =
                        AnimatorUtils.translationRight(mTextWrapper.getChildAt(mMiddleIndex), mContext.getResources().getDimension(R.dimen.text_right_translation));
                textAnimator.playTogether(textAppearance, textTranslation);
            } else if (wrapperPositionAfter != wrapperPositionBefore) {
                Animator textAppearanceBefore =
                        AnimatorUtils.alfaDisappear(mTextWrapper.getChildAt(wrapperPositionBefore));
                Animator textTranslationBefore =
                        AnimatorUtils.translationRight(mTextWrapper.getChildAt(wrapperPositionBefore), mContext.getResources().getDimension(R.dimen.text_right_translation));
                Animator textAppearanceAfter =
                        AnimatorUtils.alfaDisappear(mTextWrapper.getChildAt(wrapperPositionAfter));
                Animator textTranslationAfter =
                        AnimatorUtils.translationRight(mTextWrapper.getChildAt(wrapperPositionAfter), mContext.getResources().getDimension(R.dimen.text_right_translation));
                textAnimator.playTogether(textAppearanceAfter, textAppearanceBefore, textTranslationAfter, textTranslationBefore);


            }
        } else {
            if (wrapperPositionAfter == mMiddleIndex) {
                Animator a =
                        AnimatorUtils.alfaAppear(mTextWrapper.getChildAt(mMiddleIndex));
                Animator b =
                        AnimatorUtils.translationLeft(mTextWrapper.getChildAt(wrapperPositionAfter), mContext.getResources().getDimension(R.dimen.text_right_translation));
                textAnimator.playTogether(a, b);
            } else if (wrapperPositionAfter != wrapperPositionBefore) {
                Animator textAppearanceBefore =
                        AnimatorUtils.alfaAppear(mTextWrapper.getChildAt(wrapperPositionBefore));
                Animator textTranslationBefore =
                        AnimatorUtils.translationLeft(mTextWrapper.getChildAt(wrapperPositionBefore), mContext.getResources().getDimension(R.dimen.text_right_translation));
                Animator textAppearanceAfter =
                        AnimatorUtils.alfaAppear(mTextWrapper.getChildAt(wrapperPositionAfter));
                Animator textTranslationAfter =
                        AnimatorUtils.translationLeft(mTextWrapper.getChildAt(wrapperPositionAfter), mContext.getResources().getDimension(R.dimen.text_right_translation));
                textAnimator.playTogether(textAppearanceAfter, textAppearanceBefore, textTranslationAfter, textTranslationBefore);

            }
        }
        textAnimations.add(textAnimator);


        AnimatorSet imageRotationAnimator = new AnimatorSet();
        if (isCloseAnimation) {
            if (wrapperPositionAfter == mMiddleIndex) {
                Animator a = AnimatorUtils.rotationCloseVerticalFromBottom(mMenuWrapper.getChildAt(wrapperPositionBefore));
                imageRotationAnimator.playTogether(a);
            } else if (wrapperPositionAfter != wrapperPositionBefore) {
                Animator closeToRight = AnimatorUtils.rotationCloseToRight(mMenuWrapper.getChildAt(wrapperPositionBefore));
                Animator closeToLeft = AnimatorUtils.rotationCloseToLeft(mMenuWrapper.getChildAt(wrapperPositionAfter));

                imageRotationAnimator.playTogether(closeToRight, closeToLeft);

            }
        } else {
            if (wrapperPositionAfter == mMiddleIndex) {
                Animator a = AnimatorUtils.rotationOpenVerticalFromBottom(mMenuWrapper.getChildAt(mMiddleIndex));
                imageRotationAnimator.playTogether(a);
            } else if (wrapperPositionAfter != wrapperPositionBefore) {
                Animator openFromRight = AnimatorUtils.rotationOpenFromRight(mMenuWrapper.getChildAt(wrapperPositionBefore));
                Animator openFromLeft = AnimatorUtils.rotationOpenFromLeft(mMenuWrapper.getChildAt(wrapperPositionAfter));
                imageRotationAnimator.playTogether(openFromLeft, openFromRight);
            }
        }
        imageAnimations.add(imageRotationAnimator);
    }

    /**
     * Filling arrays of animations to build Set of Closing / Opening animations
     */
    private void fillOpenClosingAnimations(boolean isCloseAnimation, List<Animator> textAnimations, List<Animator> imageAnimations, int wrapperPosition) {
        AnimatorSet textAnimatorSet = new AnimatorSet();
        Animator textAppearance = isCloseAnimation ?
                AnimatorUtils.alfaDisappear(mTextWrapper.getChildAt(wrapperPosition))
                : AnimatorUtils.alfaAppear(mTextWrapper.getChildAt(wrapperPosition));

        Animator textTranslation = isCloseAnimation ?
                AnimatorUtils.translationRight(mTextWrapper.getChildAt(wrapperPosition), mContext.getResources().getDimension(R.dimen.text_right_translation))
                : AnimatorUtils.translationLeft(mTextWrapper.getChildAt(wrapperPosition), mContext.getResources().getDimension(R.dimen.text_right_translation));

        textAnimatorSet.playTogether(textAppearance, textTranslation);
        textAnimations.add(textAnimatorSet);

        Animator imageRotation = isCloseAnimation ?
                wrapperPosition == 0 ? AnimatorUtils.rotationCloseToRight(mMenuWrapper.getChildAt(wrapperPosition)) : AnimatorUtils.rotationCloseVertical(mMenuWrapper.getChildAt(wrapperPosition))
                : wrapperPosition == 0 ? AnimatorUtils.rotationOpenFromRight(mMenuWrapper.getChildAt(wrapperPosition)) : AnimatorUtils.rotationOpenVertical(mMenuWrapper.getChildAt(wrapperPosition));
        imageAnimations.add(imageRotation);
    }

    private View.OnClickListener clickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mOnItemClickListenerCalled = mOnItemClickListener;
            viewClicked(v);
        }
    };

    private View.OnLongClickListener longClickItem = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            mOnItemLongClickListenerCalled = mOnItemLongClickListener;
            viewClicked(v);
            return true;
        }
    };

    private void viewClicked(View v) {
        if (mIsMenuOpen && !mIsAnimationRun) {
            mClickedView = v;
            int childIndex = mMenuWrapper.indexOfChild(v);
            if (childIndex == -1) {
                return;
            }
            toggleIsAnimationRun();
            buildChosenAnimationForHorizontal(childIndex);
            toggleIsMenuOpen();
        }
    }

    /**
     * Builds and runs chosen item and menu closing animation
     */
    private void buildChosenAnimation(int childIndex) {
        List<Animator> fadeOutTextTopAnimatorList = new ArrayList<>();
        List<Animator> closeToBottomImageAnimatorList = new ArrayList<>();
        for (int i = 0; i < childIndex; i++) {
            View view = mMenuWrapper.getChildAt(i);
            resetVerticalAnimation(view, true);
            closeToBottomImageAnimatorList.add(AnimatorUtils.rotationCloseVertical(view));
            fadeOutTextTopAnimatorList.add(AnimatorUtils.fadeOutSet(mTextWrapper.getChildAt(i), mContext.getResources().getDimension(R.dimen.text_right_translation)));
        }
        AnimatorSet closeToBottom = new AnimatorSet();
        closeToBottom.playSequentially(closeToBottomImageAnimatorList);
        AnimatorSet fadeOutTop = new AnimatorSet();
        fadeOutTop.playSequentially(fadeOutTextTopAnimatorList);

        List<Animator> fadeOutTextBottomAnimatorList = new ArrayList<>();
        List<Animator> closeToTopAnimatorObjects = new ArrayList<>();
        for (int i = getItemCount() - 1; i > childIndex; i--) {
            View view = mMenuWrapper.getChildAt(i);
            resetVerticalAnimation(view, false);
            closeToTopAnimatorObjects.add(AnimatorUtils.rotationCloseVertical(view));
            fadeOutTextBottomAnimatorList.add(AnimatorUtils.fadeOutSet(mTextWrapper.getChildAt(i), mContext.getResources().getDimension(R.dimen.text_right_translation)));
        }
        AnimatorSet closeToTop = new AnimatorSet();
        closeToTop.playSequentially(closeToTopAnimatorObjects);
        AnimatorSet fadeOutBottom = new AnimatorSet();
        fadeOutBottom.playSequentially(fadeOutTextBottomAnimatorList);

        resetRightAnimation(mMenuWrapper.getChildAt(childIndex));
        ObjectAnimator closeToRight = AnimatorUtils.rotationCloseToRight(mMenuWrapper.getChildAt(childIndex));
        closeToRight.addListener(mChosenItemFinishAnimatorListener);
        AnimatorSet fadeOutChosenText = AnimatorUtils.fadeOutSet(mTextWrapper.getChildAt(childIndex), mContext.getResources().getDimension(R.dimen.text_right_translation));

        AnimatorSet imageFullAnimatorSet = new AnimatorSet();
        imageFullAnimatorSet.play(closeToBottom).with(closeToTop);
        AnimatorSet textFullAnimatorSet = new AnimatorSet();
        textFullAnimatorSet.play(fadeOutTop).with(fadeOutBottom);
        if (closeToBottomImageAnimatorList.size() >= closeToTopAnimatorObjects.size()) {
            imageFullAnimatorSet.play(closeToBottom).before(closeToRight);
            textFullAnimatorSet.play(fadeOutTop).before(fadeOutChosenText);
        } else {
            imageFullAnimatorSet.play(closeToTop).before(closeToRight);
            textFullAnimatorSet.play(fadeOutBottom).before(fadeOutChosenText);
        }

        AnimatorSet fullAnimatorSet = new AnimatorSet();
        fullAnimatorSet.playTogether(imageFullAnimatorSet, textFullAnimatorSet);
        fullAnimatorSet.setDuration(mAnimationDurationMilis);
        fullAnimatorSet.setInterpolator(new HesitateInterpolator());
        fullAnimatorSet.start();
    }

    /**
     * Builds and runs chosen item and menu closing animation
     */
    private void buildChosenAnimationForHorizontal(int childIndex) {
        List<Animator> fadeOutTextLeftAnimatorList = new ArrayList<>();
        List<Animator> closeToRightImageAnimatorList = new ArrayList<>();
        for (int i = 0; i < childIndex; i++) {
            View view = mMenuWrapper.getChildAt(i);
            resetLeftAnimation(view);
            closeToRightImageAnimatorList.add(AnimatorUtils.rotationCloseToRight(view));
            fadeOutTextLeftAnimatorList.add(AnimatorUtils.fadeOutSet(mTextWrapper.getChildAt(i), mContext.getResources().getDimension(R.dimen.text_right_translation)));
        }
        AnimatorSet closeToRight = new AnimatorSet();
        closeToRight.playSequentially(closeToRightImageAnimatorList);
        AnimatorSet fadeOutLeft = new AnimatorSet();
        fadeOutLeft.playSequentially(fadeOutTextLeftAnimatorList);

        List<Animator> fadeOutTextRightAnimatorList = new ArrayList<>();
        List<Animator> closeToLeftAnimatorObjects = new ArrayList<>();
        for (int i = getItemCount() - 1; i > childIndex; i--) {
            View view = mMenuWrapper.getChildAt(i);
            resetRightAnimation(view);
            closeToLeftAnimatorObjects.add(AnimatorUtils.rotationCloseToLeft(view));
            fadeOutTextRightAnimatorList.add(AnimatorUtils.fadeOutSet(mTextWrapper.getChildAt(i), mContext.getResources().getDimension(R.dimen.text_right_translation)));
        }
        AnimatorSet closeToLeft = new AnimatorSet();
        closeToLeft.playSequentially(closeToLeftAnimatorObjects);
        AnimatorSet fadeOutRight = new AnimatorSet();
        fadeOutRight.playSequentially(fadeOutTextRightAnimatorList);

        resetVerticalFromBottomAnimation(mMenuWrapper.getChildAt(childIndex));
        ObjectAnimator closeToBottom = AnimatorUtils.rotationCloseVerticalFromBottom(mMenuWrapper.getChildAt(childIndex));
        closeToBottom.addListener(mChosenItemFinishAnimatorListener);
        AnimatorSet fadeOutChosenText = AnimatorUtils.fadeOutSet(mTextWrapper.getChildAt(childIndex), mContext.getResources().getDimension(R.dimen.text_right_translation));

        AnimatorSet imageFullAnimatorSet = new AnimatorSet();
        imageFullAnimatorSet.play(closeToRight).with(closeToLeft);
        AnimatorSet textFullAnimatorSet = new AnimatorSet();
        textFullAnimatorSet.play(fadeOutLeft).with(fadeOutRight);
        if (closeToRightImageAnimatorList.size() >= closeToLeftAnimatorObjects.size()) {
            imageFullAnimatorSet.play(closeToRight).before(closeToBottom);
            textFullAnimatorSet.play(fadeOutLeft).before(fadeOutChosenText);
        } else {
            imageFullAnimatorSet.play(closeToLeft).before(closeToBottom);
            textFullAnimatorSet.play(fadeOutRight).before(fadeOutChosenText);
        }

        AnimatorSet fullAnimatorSet = new AnimatorSet();
        fullAnimatorSet.playTogether(imageFullAnimatorSet, textFullAnimatorSet);
        fullAnimatorSet.setDuration(mAnimationDurationMilis);
        fullAnimatorSet.setInterpolator(new HesitateInterpolator());
        fullAnimatorSet.start();
    }


    public void menuToggle() {
        if (!mIsAnimationRun) {
            resetAnimations();
            mIsAnimationRun = true;
            if (mIsMenuOpen) {
                mAnimatorSetHideMenu.start();
            } else {
                mAnimatorSetShowMenu.start();
            }
            toggleIsMenuOpen();
        }
    }

    private void toggleIsAnimationRun() {
        mIsAnimationRun = !mIsAnimationRun;
    }

    private void toggleIsMenuOpen() {
        mIsMenuOpen = !mIsMenuOpen;
    }

    public void setAnimationDuration(int durationMillis) {
        mAnimationDurationMilis = durationMillis;
    }

    private Animator.AnimatorListener mCloseOpenAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            toggleIsAnimationRun();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    private Animator.AnimatorListener mChosenItemFinishAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            toggleIsAnimationRun();
            if (mOnItemLongClickListenerCalled != null) {
                mOnItemLongClickListenerCalled.onLongClick(mClickedView);
            } else if (mOnItemClickListenerCalled != null) {
                mOnItemClickListenerCalled.onClick(mClickedView);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

}
