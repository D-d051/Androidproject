package com.dipen.sqlite_recview;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class ListViewFragment extends Fragment {

    ListView mListView;
    FragmentInterface fragmentInterface;

    public void setFragmentInterface(FragmentInterface fragmentInterface) {
        this.fragmentInterface = fragmentInterface;
    }

    public ListViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = ((ListView) view.findViewById(R.id.lv_container));

        final ListSqliteOpenHelper helper = new ListSqliteOpenHelper(getContext());
        Cursor cursor = helper.getAllData();
        ListCursorAdapter cursorAdapter = new ListCursorAdapter(getContext(), cursor);

        mListView.setAdapter(cursorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String rowId = ((TextView) view.findViewById(R.id.tv_id_holder)).getText().toString();

                if (fragmentInterface != null) {
                    fragmentInterface.onItemOpened(rowId);
                }
            }
        });


    }
}
