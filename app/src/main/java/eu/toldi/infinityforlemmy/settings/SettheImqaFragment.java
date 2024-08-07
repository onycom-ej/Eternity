package eu.toldi.infinityforlemmy.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import eu.toldi.infinityforlemmy.R;
import eu.toldi.infinityforlemmy.activities.MainActivity;
import eu.toldi.infinityforlemmy.customviews.CustomFontPreferenceFragmentCompat;
import io.imqa.crash.IMQACrashAgent;
import io.imqa.mpm.IMQAMpmAgent;

public class SettheImqaFragment  extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setimqa_preferences, rootKey);

        configureEditTextPreference("edit_server");
        configureEditTextPreference("edit_wcrash");
        configureEditTextPreference("edit_project");
        configureEditTextPreference("edit_customID");
        configureEditTextPreference("edit_customEmail");
        configureEditTextPreference("edit_customName");
    }

    private void configureEditTextPreference(String key) {
        EditTextPreference editTextPreference = findPreference(key);

        if (editTextPreference != null) {
            editTextPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String newValueString = (String) newValue;
                editTextPreference.setSummary(newValueString);
                return true;
            });

            String currentValue = editTextPreference.getText();
            if (currentValue != null) {
                editTextPreference.setSummary(currentValue);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // 저장 버튼을 클릭한 경우
        if (preference.getKey().equals("save_button")) {
            // 토스트 메시지를 띄웁니다.
            Toast.makeText(requireContext(), "재시작후 반영됩니다!", Toast.LENGTH_SHORT).show();
            return true; // 이벤트 처리 완료를 나타냄
        } else if (preference.getKey().equals("CUSTOM_CRASH")) {
            IMQACrashAgent.sendCustomException(new Exception("Custom_test"));
            Toast.makeText(getContext(), "커스텀 크래시 발생!", Toast.LENGTH_SHORT).show();
        }
        return super.onPreferenceTreeClick(preference);
    }

}
