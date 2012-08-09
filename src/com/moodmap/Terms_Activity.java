/*******************************************************************************
 * Copyright 2012 Intel-GE Care Innovations(TM)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.moodmap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

/**
 * TermsActivity class is the activity which shows terms to the user.
 * 
 * @author Chougule Shivaprasad
 * @since Apr 19, 2011
 */
public class Terms_Activity extends Activity {

    ImageButton btnIAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.terms);

        btnIAgree = (ImageButton) findViewById(R.id.btnIAgree);
        btnIAgree.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                setResult(RESULT_OK);
                /** Update shared prefrence for key 'isAgree' */
                SharedPreferences spTerms = PreferenceManager
                        .getDefaultSharedPreferences(Terms_Activity.this); // STC
                                                                           // 12/28/11
                // SharedPreferences spTerms =
                // Terms_Activity.this.getSharedPreferences("Terms",
                // MODE_PRIVATE);
                spTerms.edit().putBoolean("isAgree", true).commit();// save in
                                                                    // shared
                                                                    // prefrence

                finish();

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        this.finish();

    }

}
