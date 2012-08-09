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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableRow;

/**
 * IAmAtActivity class is the activity which shows question sheet to the user.
 * 
 * @author Chougule Shivaprasad
 * @since Apr 19, 2011
 * @deprecated
 */
@Deprecated
public class IAmAtActivity extends Activity implements OnClickListener {
    TableRow tr1, tr2, tr3, tr4;

    ImageView imgHome, imgWork, imgOnTheGo, imgOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.i_am_at);

        initUi();
        AlarmReceiver.setNotificationTimer(IAmAtActivity.this);
    }

    private void initUi() {

        imgHome = (ImageView) findViewById(R.id.imageHome);
        imgWork = (ImageView) findViewById(R.id.imageWork);
        imgOnTheGo = (ImageView) findViewById(R.id.imageOnTheGo);
        imgOther = (ImageView) findViewById(R.id.imageOther);

        imgHome.setOnClickListener(this);
        imgWork.setOnClickListener(this);
        imgOnTheGo.setOnClickListener(this);
        imgOther.setOnClickListener(this);

    }

    @Override
    public void onClick(View arg0) {
        Intent data = new Intent();

        switch (arg0.getId()) {
            case R.id.imageHome:

                data.putExtra("at", "Home");
                setResult(RESULT_OK, data);
                finish();

                break;
            case R.id.imageWork:
                data.putExtra("at", "Work");
                setResult(RESULT_OK, data);
                finish();
                break;
            case R.id.imageOnTheGo:
                data.putExtra("at", "On the go");
                setResult(RESULT_OK, data);
                finish();
                break;
            case R.id.imageOther:
                data.putExtra("at", "Other");
                setResult(RESULT_OK, data);
                finish();
                break;

            default:
                break;
        }
    }
}
