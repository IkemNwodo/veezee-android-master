package cloud.veezee.android.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import cloud.veezee.android.Constants
import cloud.veezee.android.application.App
import cloud.veezee.android.models.SettingModel
import cloud.veezee.android.R
import cloud.veezee.android.Theme

class ThemeFragment : BottomSheetDialogFragment() {
    private val TAG = "ThemeFragment";

    private var bottomSheet: BottomSheetBehavior<LinearLayout>? = null;
    private var radioGroup: RadioGroup? = null;

    private val bottomSheetCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }
    }

    private fun setTheme(theme: Theme) {
        Constants.THEME = theme;

        recreateActivities();
    }

    private fun recreateActivities() {
        val recreateRequest: Intent = Intent(Constants.SETTINGS_CHANGED_NOTIFICATION_ID);
        recreateRequest.flags = SettingModel.THEME_CHANGED;
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(recreateRequest);
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style);
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_theme, null);

        radioGroup = view.findViewById(R.id.theme_radio_group);
        val radioBlack: RadioButton = view.findViewById(R.id.radio_button_black);
        val radioWhite: RadioButton = view.findViewById(R.id.radio_button_white);
        val radioPurpleDark: RadioButton = view.findViewById(R.id.radio_button_purple_dark);

        radioBlack.isChecked = true;

        when(Constants.THEME) {
            Theme.UNKNOWN_SECOND -> radioWhite.isChecked = true;
            Theme.UNKNOWN_THIRD -> radioBlack.isChecked = true;
            Theme.UNKNOWN -> radioPurpleDark.isChecked = true;
        }

        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                radioBlack.id -> {
                    setTheme(Theme.UNKNOWN_THIRD);
                }
                radioPurpleDark.id -> {
                    setTheme(Theme.UNKNOWN);
                }
                radioWhite.id -> {
                    setTheme(Theme.UNKNOWN_SECOND);
                }
            }
        }

        dialog?.setContentView(view);

        val params: CoordinatorLayout.LayoutParams = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams;
        val behavior: CoordinatorLayout.Behavior<View>? = params.behavior;

        if (behavior != null) {
            bottomSheet = (behavior as BottomSheetBehavior<LinearLayout>);
            bottomSheet?.setBottomSheetCallback(bottomSheetCallBack);
        }

    }
}