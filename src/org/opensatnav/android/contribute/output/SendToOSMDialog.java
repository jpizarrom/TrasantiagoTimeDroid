/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opensatnav.android.contribute.output;

import org.opensatnav.android.R;
import org.opensatnav.android.SatNavActivity;
import org.opensatnav.android.contribute.util.constants.ContributeConstants;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * A dialog where the user can choose where to send the tracks to, i.e.
 * to Google My Maps, Google Docs, etc.
 *
 * @author Leif Hendrik Wilden
 */
public class SendToOSMDialog extends Dialog {

  private RadioGroup groupPrivacySettings;
  private RadioButton privateButton;
  private RadioButton publicButton;
  private RadioButton trackableButton;
  private RadioButton identifiableButton;
  private EditText track_name;
  private Button sendButton;

  public SendToOSMDialog(Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.send_to_osm);
    setTitle(R.string.sendtoosm_title);
    Button cancel = (Button) findViewById(R.id.sendtoosm_cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        SatNavActivity.getInstance().dismissDialog(ContributeConstants.DIALOG_SEND_TO_OSM);
      }
    });

    Button send = (Button) findViewById(R.id.sendtoosm_send_now);
    send.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        dismiss();
        if (identifiableButton.isChecked()){
        	SatNavActivity.getInstance().sendtoosm(track_name.getText().toString(), "identifiable");
        }
        else
        	SatNavActivity.getInstance().sendtoosm(track_name.getText().toString(), "trackable");
      }
    });

    
    track_name = (EditText) findViewById(R.id.contribute_form_edit_name);
    sendButton = (Button) findViewById(R.id.sendtoosm_send_now);
    groupPrivacySettings = (RadioGroup) findViewById(R.id.sendtoosm_privacy_settings);
    
    privateButton =
        (RadioButton) findViewById(R.id.sendtoosm_private);
    
    identifiableButton =
        (RadioButton) findViewById(R.id.sendtoosm_identifiable);
    
    privateButton.setChecked(true);

    
  }

  @Override
  protected void onStop() {
    
    super.onStop();
  }

  
}
