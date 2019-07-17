package com.example.haoko.creditcardwallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask;
import com.pro100svitlo.creditCardNfcReader.utils.CardNfcUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.card.payment.CardIOActivity;

public class MainActivity extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface {

    private SimpleAdapter sa;
    private NfcAdapter mNfcAdapter;
    private CardNfcUtils mCardNfcUtils;
    private boolean mIntentFromCreate;
    private CardNfcAsyncTask mCardNfcAsyncTask;
    public int noselected;
    EditText etholder;
    RelativeLayout rl1;
    RelativeLayout rl2;
    cardDatabase carddb;
    List<String> li;
    ListView list_cards;
    TabHost th;
    String cardSelected;
    FloatingActionButton fab;
    TextView tx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        carddb = new cardDatabase(MainActivity.this);
        carddb.openDB();

        etholder = (EditText) findViewById(R.id.editHolder);
        list_cards = (ListView) findViewById(R.id.listview_cards);
        th = (TabHost) findViewById(R.id.th);
        th.setup();

        //Tab 1
        TabHost.TabSpec spec = th.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Scan Card");
        th.addTab(spec);

        //Tab2
        spec = th.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Cards");
        th.addTab(spec);

        tx = (TextView) findViewById(R.id.textView);
        tx.setText(String.valueOf(carddb.getTaskCount()));

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                carddb.bindTab(noselected, etholder.getText().toString());
                Toast.makeText(MainActivity.this, "Update Successful!", Toast.LENGTH_SHORT).show();
                load2();
                th.setCurrentTab(1);
            }
        });
        fab.hide();
        load2();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            //do something if there are no nfc module on device
            Toast.makeText(this, "This device does not support NFC!", Toast.LENGTH_SHORT).show();
        } else {
            //do something if there are nfc module on device
            mCardNfcUtils = new CardNfcUtils(this);
            //next few lines here needed in case you will scan credit card when app is closed
            mIntentFromCreate = true;
            onNewIntent(getIntent());

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_addcam){
            Intent scanIntent = new Intent(this, CardIOActivity.class);

            // customize these values to suit your needs.
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

            // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
            startActivityForResult(scanIntent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(resultCode, data);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        carddb.openDB();
    }

    @Override
    protected void onStop() {
        super.onStop();
        carddb.closeDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIntentFromCreate = false;
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            //show some turn on nfc dialog here. take a look in the samle ;-)
        } else if (mNfcAdapter != null) {
            mCardNfcUtils.enableDispatch();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mCardNfcUtils.disableDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            //this - interface for callbacks
            //intent = intent :)
            //mIntentFromCreate - boolean flag, for understanding if onNewIntent() was called from onCreate or not
            mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                    .build();
        }
    }

    @Override
    public void startNfcReadCard() {
        //notify user that scannig start
    }

    @Override
    public void cardIsReadyToRead() {
        String card = mCardNfcAsyncTask.getCardNumber();
        fab.show();
        rl1 = (RelativeLayout) findViewById(R.id.relatively1);
        rl2 = (RelativeLayout) findViewById(R.id.relatively2);
        rl1.setVisibility(View.INVISIBLE);
        rl2.setVisibility(View.VISIBLE);
        check(card);


    }

    @Override
    public void doNotMoveCardSoFast() {
        //notify user do not move the card
        Toast.makeText(this, "Do not move card so fast!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unknownEmvCard() {
        //notify user that current card has unnown nfc tag
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl);
        Snackbar.make(rl, "Unknown Card type detected!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    @Override
    public void cardWithLockedNfc() {
        //notify user that current card has locked nfc tag
        Toast.makeText(this, "This card is protected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finishNfcReadCard() {
        //notify user that scannig finished
    }


    public void load2() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> item;
        Cursor cursor = carddb.getAllRecords();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            item = new HashMap<String, String>();
            item.put("line1", cursor.getString(cursor.getColumnIndex(carddb.KEY_ID)) + " " +
                    cursor.getString(cursor.getColumnIndex(carddb.KEY_NAME)) + " " +
                    formatcard(cursor.getString(cursor.getColumnIndex(carddb.KEY_CARD))));

           item.put("line2", cursor.getString(cursor.getColumnIndex(carddb.KEY_EXPIRED))
                        + " " + cursor.getString(cursor.getColumnIndex(carddb.KEY_TYPE)) + " ");

            list.add(item);

        }
        sa = new SimpleAdapter(this, list, R.layout.twolines, new String[]{"line1", "line2"},
                new int[]{R.id.line_a, R.id.line_b});
        ((ListView) findViewById(R.id.listview_cards)).setAdapter(sa);
        list_cards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Click item " + position, Toast.LENGTH_SHORT).show();
                noselected = position;
                binddata(noselected);
                fab.show();
            }
        });

        list_cards.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                carddb.delete(position);
                Toast.makeText(MainActivity.this, "Deleted Card: " + position, Toast.LENGTH_SHORT).show();
                load2();
                return false;
            }
        });
    }

    public void check(String cardno) {
        //Cursor cursor = carddb.getAllRecords();
        TextView tx = (TextView) findViewById(R.id.textView);
        if (String.valueOf(carddb.getTaskCount()).equals("0")) {
            tx.setText(tx.getText() + " GAGA");
            cardinsert();
            load2();
        } else {
            checkData(cardno);
        }
    }

    public void checkData(String cardno) {
        Cursor cursor = carddb.getAllRecords();
        int checkdata = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String cardnumbercheck = cursor.getString(cursor.getColumnIndexOrThrow(carddb.KEY_CARD));
            if (cardnumbercheck.equals(cardno)) {
                //if card is found in database
                noselected =checkdata;
                checkdata -= 1;
            } else {
                //if card is not found
                checkdata += 1;
            }
        }
        TextView tx2 = (TextView) findViewById(R.id.textView2);
        tx2.setText(checkdata + "task count  " + carddb.getTaskCount());
        if (checkdata == (int) carddb.getTaskCount()) {
            cardinsert();
            Toast.makeText(this, "Card added!", Toast.LENGTH_SHORT).show();

            load2();

        } else {
            Toast.makeText(this, "Card had been registered in DataBase! ", Toast.LENGTH_SHORT).show();
            th.setCurrentTab(1);
            binddata(noselected);
        }

    }

    public void binddata(int binding) {
        Cursor cursor = carddb.getAllRecords();
        if (carddb.getTaskCount() == 0){
            cursor.moveToFirst();
        } else {
            cursor.moveToPosition(binding);
        }
        String card = cursor.getString(cursor.getColumnIndexOrThrow(carddb.KEY_CARD));
        String expiredDate = cursor.getString(cursor.getColumnIndexOrThrow(carddb.KEY_EXPIRED));
        String cardType = cursor.getString(cursor.getColumnIndexOrThrow(carddb.KEY_TYPE));
        String cardHolder = cursor.getString(cursor.getColumnIndexOrThrow(carddb.KEY_NAME));


        TextView text_cardno = (TextView) findViewById(R.id.text_cardno);
        TextView text_expired = (TextView) findViewById(R.id.text_expired);
        TextView text_cardtype = (TextView) findViewById(R.id.text_cardtype);
        EditText text_name = (EditText) findViewById(R.id.editHolder);


        text_cardno.setText(formatcard(card));
        text_expired.setText(expiredDate);
        text_cardtype.setText(cardType);
        text_name .setText(cardHolder);
        rl1 = (RelativeLayout) findViewById(R.id.relatively1);
        rl2 = (RelativeLayout) findViewById(R.id.relatively2);
        rl1.setVisibility(View.INVISIBLE);
        rl2.setVisibility(View.VISIBLE);

        th.setCurrentTab(0);
    }

    public void cardinsert() {
        String card = mCardNfcAsyncTask.getCardNumber();
        String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        String cardType = mCardNfcAsyncTask.getCardType();
        if (cardType.equals("NAB_VISA")) {
            cardType = "VISA";
            carddb.insert(card, expiredDate, cardType);
            noselected = (int) carddb.getTaskCount();

        } else {
            if (card.substring(0, 1).equals("4")){
                cardType = "VISA";
                carddb.insert(card, expiredDate, cardType);
            } else if (card.substring(0, 1).equals("5")){
                cardType = "MASTER";
                carddb.insert(card, expiredDate, cardType);
            } else{
                unknownAlert(card, expiredDate);
            }
        }
        binddata(noselected);

        /*if (cardType.equals("UNKNOWN")){
            unknownAlert();
        }*/
    }



    public String formatcard(String card){
        String formatted = card.substring(0, 4) + " " + card.substring(4, 8) + " "
                + card.substring(8, 12) + " " + card.substring(12, 16);
        return formatted;
    }
    public void unknownAlert(final String card, final String expired) {
        cardSelected = "UNKNOWN";
        Toast.makeText(this, "Card type could not be detected", Toast.LENGTH_SHORT).show();
        noselected = (int) carddb.getTaskCount();

        String[] items = {"VISA", "MASTER"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select the card type");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        cardSelected = "VISA";
                        carddb.insert(card, expired, cardSelected);
                        load2();
                        binddata(noselected);

                        break;
                    case 1:
                        cardSelected = "MASTER";
                        carddb.insert(card, expired, cardSelected);
                        load2();
                        binddata(noselected);

                        break;
                    default:
                        cardSelected = "UNKNOWN";
                        carddb.insert(card, expired, cardSelected);
                        load2();
                        binddata(noselected);

                        break;

                }

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }


}



