package com.example.soft1c.utils.calculator;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.soft1c.R;


/**
 * Dialog with calculator for entering and calculating a number.
 * All settings must be set before showing the dialog or unexpected behavior will occur.
 */
public class CalcDialog extends AppCompatDialogFragment {

    // Indexes of text elements in R.array.calc_dialog_btn_texts
    private static final int TEXT_INDEX_ADD = 10;
    private static final int TEXT_INDEX_SUB = 11;
    private static final int TEXT_INDEX_MUL = 12;
    private static final int TEXT_INDEX_DIV = 13;
    private static final int TEXT_INDEX_SIGN = 14;
    private static final int TEXT_INDEX_DEC_SEP = 15;
    private static final int TEXT_INDEX_EQUAL = 16;

    private Context context;
    private CalcPresenter presenter;

    private CalcSettings settings = new CalcSettings();

    private HorizontalScrollView expressionHsv;
    private TextView expressionTxv;
    private TextView valueTxv;
    private TextView decimalSepBtn;
    private TextView equalBtn;
    private TextView answerBtn;
    private TextView signBtn;

    private CharSequence[] errorMessages;

    ////////// LIFECYCLE METHODS //////////
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Wrap calculator dialog's theme to context
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.calcDialogStyle});
        int style = ta.getResourceId(0, R.style.CalcDialogStyle);
        ta.recycle();
        this.context = new ContextThemeWrapper(context, style);
    }

    @SuppressLint({"PrivateResource", "InflateParams"})
    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle state) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_calc, null);

        // Get attributes
        final TypedArray ta = context.obtainStyledAttributes(R.styleable.CalcDialog);
        final CharSequence[] btnTexts = ta.getTextArray(R.styleable.CalcDialog_calcButtonTexts);
        errorMessages = ta.getTextArray(R.styleable.CalcDialog_calcErrors);
        final int maxDialogWidth = ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxWidth, -1);
        final int maxDialogHeight = ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxHeight, -1);
        final int headerColor = getColor(ta, R.styleable.CalcDialog_calcHeaderColor);
        final int headerElevationColor = getColor(ta, R.styleable.CalcDialog_calcHeaderElevationColor);
        final int separatorColor = getColor(ta, R.styleable.CalcDialog_calcDividerColor);
        final int numberBtnColor = getColor(ta, R.styleable.CalcDialog_calcDigitBtnColor);
        final int operationBtnColor = getColor(ta, R.styleable.CalcDialog_calcOperationBtnColor);
        ta.recycle();

        view.setFocusable(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP)
                switch (keyCode) {
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16: {
                        presenter.onDigitBtnClicked(keyCode - 7);
                        break;
                    }
                    case 55:
                    case 56: {
                        presenter.onDecimalSepBtnClicked();
                        break;
                    }
                    case 66:
                    case 135: {
                        presenter.onOkBtnClicked();
                        break;
                    }
                    case 62:
                    case 67: {
                        presenter.onErasedOnce();
                        break;
                    }
                    case 131: {
                        presenter.onOperatorBtnClicked(Expression.Operator.DIVIDE);
                        break;
                    }
                    case 132: {
                        presenter.onOperatorBtnClicked(Expression.Operator.MULTIPLY);
                        break;
                    }
                    case 133: {
                        presenter.onOperatorBtnClicked(Expression.Operator.SUBTRACT);
                        break;
                    }
                    case 134: {
                        presenter.onOperatorBtnClicked(Expression.Operator.ADD);
                        break;
                    }
                }
            return false;
        });

        // Header
        final View headerBgView = view.findViewById(R.id.calc_view_header_background);
        final View headerElevationBgView = view.findViewById(R.id.calc_view_header_elevation);
        headerBgView.setBackgroundColor(headerColor);
        headerElevationBgView.setBackgroundColor(headerElevationColor);
        headerElevationBgView.setVisibility(View.GONE);

        // Value and expression views
        valueTxv = view.findViewById(R.id.calc_txv_value);

        expressionHsv = view.findViewById(R.id.calc_hsv_expression);
        expressionTxv = view.findViewById(R.id.calc_txv_expression);

        // Erase button
        CalcEraseButton eraseBtn = view.findViewById(R.id.calc_btn_erase);
        eraseBtn.setOnEraseListener(new CalcEraseButton.EraseListener() {
            @Override
            public void onErase() {
                if (presenter != null) {
                    presenter.onErasedOnce();
                }
            }

            @Override
            public void onEraseAll() {
                presenter.onErasedAll();
            }
        });

        // Digit buttons
        for (int i = 0; i < 10; i++) {
            TextView digitBtn = view.findViewById(settings.getNumpadLayout().buttonIds[i]);
            digitBtn.setText(btnTexts[i]);

            final int digit = i;
            digitBtn.setOnClickListener(v -> presenter.onDigitBtnClicked(digit));
        }

        final View numberBtnBgView = view.findViewById(R.id.calc_view_number_bg);
        numberBtnBgView.setBackgroundColor(numberBtnColor);

        // Operator buttons
        final TextView addBtn = view.findViewById(R.id.calc_btn_add);
        final TextView subBtn = view.findViewById(R.id.calc_btn_sub);
        final TextView mulBtn = view.findViewById(R.id.calc_btn_mul);
        final TextView divBtn = view.findViewById(R.id.calc_btn_div);

        addBtn.setText(btnTexts[TEXT_INDEX_ADD]);
        subBtn.setText(btnTexts[TEXT_INDEX_SUB]);
        mulBtn.setText(btnTexts[TEXT_INDEX_MUL]);
        divBtn.setText(btnTexts[TEXT_INDEX_DIV]);

        addBtn.setOnClickListener(v -> presenter.onOperatorBtnClicked(Expression.Operator.ADD));
        subBtn.setOnClickListener(v -> presenter.onOperatorBtnClicked(Expression.Operator.SUBTRACT));
        mulBtn.setOnClickListener(v -> presenter.onOperatorBtnClicked(Expression.Operator.MULTIPLY));
        divBtn.setOnClickListener(v -> presenter.onOperatorBtnClicked(Expression.Operator.DIVIDE));

        final View opBtnBgView = view.findViewById(R.id.calc_view_op_bg);
        opBtnBgView.setBackgroundColor(operationBtnColor);

        // Sign button: +/-
        signBtn = view.findViewById(R.id.calc_btn_sign);
        signBtn.setText(btnTexts[TEXT_INDEX_SIGN]);
        signBtn.setOnClickListener(v -> presenter.onSignBtnClicked());

        // Decimal separator button
        decimalSepBtn = view.findViewById(R.id.calc_btn_decimal);
        decimalSepBtn.setText(btnTexts[TEXT_INDEX_DEC_SEP]);
        decimalSepBtn.setOnClickListener(v -> presenter.onDecimalSepBtnClicked());

        // Equal button
        equalBtn = view.findViewById(R.id.calc_btn_equal);
        equalBtn.setText(btnTexts[TEXT_INDEX_EQUAL]);
        equalBtn.setOnClickListener(v -> presenter.onEqualBtnClicked());

        // Answer button
        answerBtn = view.findViewById(R.id.calc_btn_answer);
        answerBtn.setOnClickListener(v -> presenter.onAnswerBtnClicked());

        // Divider
        final View footerDividerView = view.findViewById(R.id.calc_view_footer_divider);
        footerDividerView.setBackgroundColor(separatorColor);

        // Dialog buttons
        Button clearBtn = view.findViewById(R.id.calc_btn_clear);
        clearBtn.setOnClickListener(v -> presenter.onClearBtnClicked());

        Button cancelBtn = view.findViewById(R.id.calc_btn_cancel);
        cancelBtn.setOnClickListener(v -> presenter.onCancelBtnClicked());

        Button okBtn = view.findViewById(R.id.calc_btn_ok);
        okBtn.setOnClickListener(v -> presenter.onOkBtnClicked());

        // Set up dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnShowListener(dialogInterface -> {
            // Get maximum dialog dimensions
            Rect fgPadding = new Rect();
            dialog.getWindow().getDecorView().getBackground().getPadding(fgPadding);
            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            int height = metrics.heightPixels - fgPadding.top - fgPadding.bottom;
            int width = metrics.widthPixels - fgPadding.top - fgPadding.bottom;

            // Set dialog's dimensions
            if (width > maxDialogWidth) width = maxDialogWidth;
            if (height > maxDialogHeight) height = maxDialogHeight;
            dialog.getWindow().setLayout(width, height);

            // Set dialog's content
            view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            dialog.setContentView(view);

            // Presenter
            presenter = new CalcPresenter();
            presenter.attach(CalcDialog.this, state);
        });

        if (state != null) {
            settings = state.getParcelable("settings");
        }

        return dialog;
    }

    private int getColor(TypedArray ta, int index) {
        int resId = ta.getResourceId(index, 0);
        if (resId == 0) {
            // Raw color value e.g.: #FF000000
            return ta.getColor(index, 0);
        } else {
            // Color reference pointing to color state list or raw color.
            return AppCompatResources.getColorStateList(context, resId).getDefaultColor();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (presenter != null) {
            // On config change, presenter is detached before this is called
            presenter.onDismissed();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (presenter != null) {
            presenter.writeStateToBundle(state);
        }
        state.putParcelable("settings", settings);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) {
            presenter.detach();
        }

        presenter = null;
        context = null;
    }

    @Nullable
    private CalcDialogCallback getCallback() {
        CalcDialogCallback cb = null;
        if (getParentFragment() != null) {
            try {
                cb = (CalcDialogCallback) getParentFragment();
            } catch (Exception e) {
                // Interface callback is not implemented in fragment
            }
        } else if (getTargetFragment() != null) {
            try {
                cb = (CalcDialogCallback) getTargetFragment();
            } catch (Exception e) {
                // Interface callback is not implemented in fragment
            }
        } else {
            // Caller was an activity
            try {
                cb = (CalcDialog.CalcDialogCallback) requireActivity();
            } catch (Exception e) {
                // Interface callback is not implemented in activity
            }
        }
        return cb;
    }

    /**
     * @return the calculator settings that can be changed.
     */
    public CalcSettings getSettings() {
        return settings;
    }

    ////////// VIEW METHODS //////////
    void exit() {
        dismissAllowingStateLoss();
    }

    void sendValueResult(BigDecimal value) {
        CalcDialogCallback cb = getCallback();
        if (cb != null) {
            cb.onValueEntered(settings.requestCode, value);
        }
    }

    void setExpressionVisible(boolean visible) {
        expressionHsv.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    void setAnswerBtnVisible(boolean visible) {
        answerBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        equalBtn.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
    }

    void setSignBtnVisible(boolean visible) {
        signBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    void setDecimalSepBtnEnabled(boolean enabled) {
        decimalSepBtn.setEnabled(enabled);
    }

    void updateExpression(@NonNull String text) {
        expressionTxv.setText(text);

        // Scroll to the end.
        expressionHsv.post(() -> {
            expressionHsv.fullScroll(View.FOCUS_RIGHT);
            expressionHsv.clearFocus();
        });
    }

    void updateCurrentValue(@Nullable String text) {
        valueTxv.setText(text);
    }

    void showErrorText(int error) {
        valueTxv.setText(errorMessages[error]);
    }

    void showAnswerText() {
        valueTxv.setText(R.string.calc_answer);
    }

    public interface CalcDialogCallback {
        /**
         * Called when the dialog's OK button is clicked.
         *
         * @param value       value entered. May be null if no value was entered, in this case,
         *                    it should be interpreted as zero or absent value.
         * @param requestCode dialog request code from {@link CalcSettings#getRequestCode()}.
         */
        void onValueEntered(int requestCode, @Nullable BigDecimal value);
    }

}
