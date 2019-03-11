/*
 * Copyright (c) 2018 NUTES/UEPB
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.edu.uepb.nutes.simplesurvey.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

import java.io.Serializable;

import br.edu.uepb.nutes.simplesurvey.R;
import br.edu.uepb.nutes.simplesurvey.base.BaseConfigPage;
import br.edu.uepb.nutes.simplesurvey.base.BasePage;
import br.edu.uepb.nutes.simplesurvey.base.OnPageListener;

/**
 * RadioQuestion implementation.
 */
public class RadioQuestion extends BasePage<RadioQuestion.Config> implements ISlideBackgroundColorHolder {
    private final String TAG = "RadioQuestion";

    private static final String ARG_CONFIGS_PAGE = "arg_configs_page";
    private static final String KEY_OLD_ANSWER_BUNDLE = "old_answer";

    private OnRadioListener mListener;
    private boolean answerValue, actionClearCheck;
    private int oldAnswer;
    private Config configPage;
    private RadioGroup radioGroup;
    private RadioButton radioLeft;
    private RadioButton radioRight;

    public RadioQuestion() {
    }

    /**
     * New RadioQuestion instance.
     *
     * @param configPage {@link Config}
     * @return RadioQuestion
     */
    private static RadioQuestion newInstance(Config configPage) {
        RadioQuestion pageFragment = new RadioQuestion();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONFIGS_PAGE, configPage);

        pageFragment.setArguments(args);
        return pageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting default values
        oldAnswer = -1;
        answerValue = false;
        actionClearCheck = false;

        // Retrieving arguments
        if (getArguments() != null && getArguments().size() != 0) {
            configPage = (Config) getArguments().getSerializable(ARG_CONFIGS_PAGE);
            super.pageNumber = configPage.getPageNumber();
        }
    }

    @Override
    public void initView(View v) {
        this.radioGroup = v.findViewById(R.id.answer_radioGroup);
        this.radioLeft = v.findViewById(R.id.left_radioButton);
        this.radioRight = v.findViewById(R.id.right_radioButton);

        if (radioGroup != null) {
            if (configPage.radioLeftText != 0)
                radioLeft.setText(configPage.radioLeftText);

            if (configPage.radioRightText != 0)
                radioRight.setText(configPage.radioRightText);

            if (configPage.radioLeftBackground != 0)
                radioLeft.setBackgroundResource(configPage.radioLeftBackground);

            if (configPage.radioRightBackground != 0)
                radioRight.setBackgroundResource(configPage.radioRightBackground);

            if (configPage.radioColorTextNormal != 0) {
                radioLeft.setTextColor(configPage.radioColorTextNormal);
                radioRight.setTextColor(configPage.radioColorTextNormal);
            }

            // init answer
            if (configPage.answerInit != -1)
                setAnswer(configPage.answerInit != 0);


            if (configPage.isAnswerRequired()) {
                super.blockPage();
            } else {
                super.unlockPage();
            }

            refreshStyles();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_OLD_ANSWER_BUNDLE, oldAnswer);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (radioGroup == null) return;

        if (savedInstanceState != null) {
            oldAnswer = savedInstanceState.getInt(KEY_OLD_ANSWER_BUNDLE, -1);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (actionClearCheck) return;

                if (checkedId == R.id.left_radioButton && oldAnswer != 0) {
                    setAnswer(false);
                    if (mListener != null) {
                        mListener.onAnswerRadio(pageNumber, false);
                        if (configPage.isNextQuestionAuto()) nextPage();
                    }
                } else if (checkedId == R.id.right_radioButton && oldAnswer != 1) {
                    setAnswer(true);
                    if (mListener != null) {
                        mListener.onAnswerRadio(pageNumber, true);
                        if (configPage.isNextQuestionAuto()) nextPage();
                    }
                }
            }
        });
    }

    @Override
    public int getLayout() {
        return configPage.getLayout();
    }

    @Override
    public Config getConfigsPage() {
        return this.configPage;
    }

    @Override
    public View getComponentAnswer() {
        return radioGroup;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (radioGroup != null) radioGroup.setOnCheckedChangeListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRadioListener) {
            mListener = (OnRadioListener) context;
            super.mPageListener = mListener;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return configPage.getColorBackground();
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        if (getView() != null) getView().setBackgroundColor(configPage.getColorBackground());
    }

    @Override
    public void clearAnswer() {
        actionClearCheck = true;
        radioGroup.clearCheck();
        actionClearCheck = false;
        oldAnswer = -1;
        refreshStyles();

        // Block page
//        super.blockPage();
    }

    /**
     * Set Answer.
     *
     * @param value boolean
     */
    private void setAnswer(boolean value) {
        super.unlockPage();
        answerValue = value;
        oldAnswer = !value ? 0 : 1;

        if (value) radioRight.setChecked(true);
        else radioLeft.setChecked(true);
        refreshStyles();
    }

    private void refreshStyles() {
        if (this.configPage.radioColorTextNormal == 0 &&
                this.configPage.radioColorTextChecked == 0) return;

        if (this.oldAnswer == 1) {
            this.radioRight.setTextColor(this.configPage.radioColorTextChecked);
            this.radioLeft.setTextColor(this.configPage.radioColorTextNormal);
        } else if (this.oldAnswer == 0) {
            this.radioLeft.setTextColor(this.configPage.radioColorTextChecked);
            this.radioRight.setTextColor(this.configPage.radioColorTextNormal);
        } else {
            this.radioLeft.setTextColor(this.configPage.radioColorTextNormal);
            this.radioRight.setTextColor(this.configPage.radioColorTextNormal);
        }
    }

    /**
     * Class config page.
     */
    public static class Config extends BaseConfigPage<Config> implements Serializable {
        private int radioLeftText,
                radioRightText,
                radioColorTextNormal,
                radioColorTextChecked,
                radioLeftBackground,
                radioRightBackground,
                answerInit;

        public Config() {
            super.layout(R.layout.question_radio);
            this.radioLeftText = 0;
            this.radioRightText = 0;
            this.radioColorTextNormal = 0;
            this.radioColorTextChecked = 0;
            this.radioLeftBackground = 0;
            this.radioRightBackground = 0;
            this.answerInit = -1;
        }

        /**
         * Set left radio text.
         *
         * @param radioLeftText @{@link StringRes} resource text left.
         * @return Config
         */
        public Config radioLeftText(@StringRes int radioLeftText) {
            this.radioLeftText = radioLeftText;
            return this;
        }

        /**
         * Set right radio text.
         *
         * @param radioRightText @{@link StringRes} resource text right.
         * @return Config
         */
        public Config radioRightText(@StringRes int radioRightText) {
            this.radioRightText = radioRightText;
            return this;
        }

        /**
         * Set style radio.
         *
         * @param backgroundLeftRadio   @{@link DrawableRes} resource the left side.
         * @param backgroundRightRadio  @{@link DrawableRes} resource the right side.
         * @param textColorRadioNormal  @{@link ColorInt} resource text color normal.
         * @param textColorRadioChecked @{@link ColorInt} resource text color checked.
         * @return Config
         */
        public Config radioStyle(@DrawableRes int backgroundLeftRadio,
                                 @DrawableRes int backgroundRightRadio,
                                 @ColorInt int textColorRadioNormal,
                                 @ColorInt int textColorRadioChecked) {
            this.radioLeftBackground = backgroundLeftRadio;
            this.radioRightBackground = backgroundRightRadio;
            this.radioColorTextNormal = textColorRadioNormal;
            this.radioColorTextChecked = textColorRadioChecked;
            return this;
        }

        /**
         * Set answer init.
         *
         * @param answerInit boolean answer.
         * @return Config
         */
        public Config answerInit(boolean answerInit) {
            if (answerInit) this.answerInit = 1;
            else this.answerInit = 0;
            return this;
        }

        @Override
        public RadioQuestion build() {
            return RadioQuestion.newInstance(this);
        }
    }

    /**
     * Interface OnRadioListener.
     */
    public interface OnRadioListener extends OnPageListener {
        void onAnswerRadio(int page, boolean value);
    }
}