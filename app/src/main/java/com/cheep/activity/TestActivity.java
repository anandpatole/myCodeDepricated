package com.cheep.activity;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.cheep.R;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.TextThumbSeekBar;

import org.json.JSONException;
import org.json.JSONObject;

public class TestActivity extends AppCompatActivity {

    private static final String TABLE_NAME = "psConversation";
    private static final String[] SAVED_FIELDS = {
            "message",
            "from",
            "to",
            "size",
            "duration"
    };
    private static final String query = "INSERT INTO " + TABLE_NAME + " ( %s ) VALUES ( %s ) ";

    private BottomSheetBehavior mBottomSheetBehavior;
    int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheet.post(new Runnable() {
            @Override
            public void run() {
                height = bottomSheet.getHeight();
//                mBottomSheetBehavior.setPeekHeight(200);
//                bottomSheet.getLayoutParams().height = bottomSheet.getHeight() - accountHeight;
//                bottomSheet.requestLayout();
//                mBottomSheetBehavior.onLayoutChild(coordinatorLayout, bottomSheet, ViewCompat.LAYOUT_DIRECTION_LTR);
            }
        });


        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String body = "{\n" +
                        "\"message\":\"Hello pa'nkaj sharma\",\n" +
                        "\"attachment_type\":\"media\",\n" +
                        "\"size\":\"56mb\",\n" +
                        "\"duration\":\"89:12\",\n" +
                        "\"to\":\"rahul\"\n" +
                        "}\n";
                try {
                    JSONObject jsonBody = new JSONObject(body);
                    String fields = "";
                    String values = "";
                    for (String savedField : SAVED_FIELDS) {
                        if (jsonBody.has(savedField)) {
                            fields += savedField + ",";
                            values += " '" + getActualValue(jsonBody.getString(savedField)) + "',";
                        }
                    }
                    fields = fields.replaceAll(",$", "");
                    values = values.replaceAll(",$", "");
                    Log.i("TAG", String.format(query, fields, values));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//

                final BottomAlertDialog dialog = new BottomAlertDialog(TestActivity.this);
                dialog.setTitle("ACTION");
                dialog.setMessage("Morgan has sent you a request for more information");
                dialog.addPositiveButton("ACCEPT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Toast.makeText(TestActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.addNegativeButton("DECLINE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Toast.makeText(TestActivity.this, "No", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.showDialog();
            }
        });

        TextThumbSeekBar textThumbSeekBar = (TextThumbSeekBar) findViewById(R.id.seekbar);
        textThumbSeekBar.setSuffix("Km");

    }


    private String getActualValue(String value) {
        if (value != null) {
            return value.replace("'", "\\'");
        }
        return value;
    }


    @Override
    protected void onDestroy() {


        super.onDestroy();
    }

}
