package loranc.dawid.onepound;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_CODE = 0;

    private boolean hasReadSmsPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasValidPreConditions() {
        if (!hasReadSmsPermission()) {
            requestReadAndSendSmsPermission();
            return false;
        }

        return true;
    }

    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }

        ActivityCompat.requestPermissions(
            MainActivity.this,
            new String[] {
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            },
            SMS_PERMISSION_CODE
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText phoneNumberEditText = (EditText) findViewById(R.id.phone_number);
        String momPhoneNumber = getMomPhoneNumber();
        phoneNumberEditText.setText(momPhoneNumber);

        Button savePhoneNumber = (Button) findViewById(R.id.save_phone_number);
        savePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                savePhoneNumber(phoneNumber);
            }
        });

        Button sendSMSButton = (Button) findViewById(R.id.send_sms_button);
        sendSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasValidPreConditions()) return;

                sendSMS();
            }
        });
    }

    private void sendSMS() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://api.nbp.pl/api/exchangerates/rates/a/gbp/?format=json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TextView currentRateTextView = (TextView) findViewById(R.id.current_rate);

                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray rates = json.getJSONArray("rates");
                            JSONObject firstRate = rates.getJSONObject(0);
                            String rate = firstRate.getString("mid");

                            String message = "1 funt to "+ rate + " złotych";
                            currentRateTextView.setText(message);

                            try {
                                sendSMSMessage(message);
                            } catch (Exception exception) {
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    private void sendSMSMessage(String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(getMomPhoneNumber(), null, message, null, null);
    }

    private String getMomPhoneNumber() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        return sharedPref.getString("phone_number", "");
    }

    private void savePhoneNumber(String phoneNumber) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("phone_number", phoneNumber);
        editor.apply();
        Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_SHORT).show();
    }
}
