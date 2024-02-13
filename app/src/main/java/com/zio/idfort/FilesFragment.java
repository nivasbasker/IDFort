package com.zio.idfort;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.zio.idfort.data.DocsDAO;
import com.zio.idfort.data.DocsDB;
import com.zio.idfort.data.DocsEntity;
import com.zio.idfort.databinding.FragmentFilesBinding;
import com.zio.idfort.ui.AddDetails;
import com.zio.idfort.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FilesFragment extends Fragment {

    FloatingActionButton fab;
    DocsDAO docsDao;
    AdapterFiles adapter;
    List<DocsEntity> list;
    DocsDB db;
    RecyclerView rvDocs;

    FragmentFilesBinding binding;

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFilesBinding.inflate(inflater, container, false);
        fab = binding.fab;
        rvDocs = binding.doclist;

        setList();

        PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals("Aadhaar")) {
                    Intent i = new Intent(getContext(), AddDetails.class);
                    i.putExtra("Name", item.getTitle());
                    startActivity(i);
                } else {
                    Snackbar.make(binding.getRoot(), "Feature coming soon", Snackbar.LENGTH_SHORT).show();
                }
                return true;
            }
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(getContext(), fab);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.docs_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(listener);
                popup.show();
            }
        });

        return binding.getRoot();
    }


    private void setList() {
        db = Room.databaseBuilder(getContext(), DocsDB.class, Constants.DatabaseName).allowMainThreadQueries().fallbackToDestructiveMigration().build();
        docsDao = db.docsdao();

        List<DocsEntity> docslist = docsDao.getAll();
        list = new ArrayList<>();
        for (DocsEntity x : docslist)
            list.add(new DocsEntity(x.getDocument_name(), null, x.getId(), x.getName()));
        //db.close();

        adapter = new AdapterFiles(getContext(), list);
        rvDocs.setAdapter(adapter);
        rvDocs.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }


    @Override
    public void onPause() {
        db.close();
        super.onPause();
    }

    @Override
    public void onResume() {
        setList();
        super.onResume();
    }
}