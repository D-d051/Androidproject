package com.dipen.sqlite_recview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends
        AppCompatActivity
implements FragmentInterface{

    ListViewFragment listViewFragment;
    ItemViewFragment itemViewFragment;

    ListSqliteOpenHelper openHelper;

    boolean bIsListViewFragment = false;
    boolean bItemViewIsOpenedToEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        openHelper = new ListSqliteOpenHelper(this);

        openListViewFragment();

        EnableRuntimePermission();

    }

    public void openListViewFragment() {
        bIsListViewFragment = true;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        listViewFragment = new ListViewFragment();
        listViewFragment.setFragmentInterface(this);
        ft.replace(R.id.fragment_container, listViewFragment);
        ft.commit();
        invalidateOptionsMenu();
    }

    public void openItemViewFragment(String rowId) {
        bIsListViewFragment = false;
        bItemViewIsOpenedToEdit = false;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        itemViewFragment = new ItemViewFragment();

        Bundle bundle = new Bundle();
        if (rowId != null && !TextUtils.isEmpty(rowId)) {
            bundle.putString(ListSqliteOpenHelper.COL_1_ID, rowId);
            bItemViewIsOpenedToEdit = true;
        }
        itemViewFragment.setArguments(bundle);

        ft.replace(R.id.fragment_container, itemViewFragment);
        ft.commit();
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.menu_delete).setVisible(!bIsListViewFragment && bItemViewIsOpenedToEdit);
        menu.findItem(R.id.menu_save).setVisible(!bIsListViewFragment);
        menu.findItem(R.id.menu_new).setVisible(bIsListViewFragment);
        menu.findItem(R.id.menu_help).setVisible(bIsListViewFragment);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_new:
                openItemViewFragment(null);
                return true;
            case R.id.menu_help:
                showHelpWebview();
                return true;
            case R.id.menu_save:
                if (itemViewFragment != null) {
                    boolean bWasSuccess = bItemViewIsOpenedToEdit ? itemViewFragment.updateData() : itemViewFragment.saveData();
                    if (bWasSuccess) {
                        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                        openListViewFragment();
                    } else {
                        Toast.makeText(this, "Saving Failed!", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            case R.id.menu_delete:
                if (itemViewFragment != null && bItemViewIsOpenedToEdit) {
                    boolean bWasSuccess = itemViewFragment.deleteCurrentData();
                    openListViewFragment();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!bIsListViewFragment) {
            openListViewFragment();
        } else {
            super.onBackPressed();
        }

    }

    private void showHelpWebview() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemOpened(String rowId) {
        openItemViewFragment(rowId);
    }

    public void EnableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA)) {
            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        super.onRequestPermissionsResult(RC, per, PResult);
        switch (RC) {
            case 0:
                if (!(PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
