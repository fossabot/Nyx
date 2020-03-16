package com.larryhsiao.nyx.tag;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.larryhsiao.nyx.R;
import com.larryhsiao.nyx.base.JotFragment;
import com.larryhsiao.nyx.core.jots.Jot;
import com.larryhsiao.nyx.core.jots.QueriedJots;
import com.larryhsiao.nyx.core.tags.AllTags;
import com.larryhsiao.nyx.core.tags.JotsByTagId;
import com.larryhsiao.nyx.core.tags.NewTag;
import com.larryhsiao.nyx.core.tags.QueriedTags;
import com.larryhsiao.nyx.core.tags.Tag;
import com.larryhsiao.nyx.core.tags.TagRemoval;
import com.larryhsiao.nyx.core.tags.TagsByKeyword;
import com.larryhsiao.nyx.jot.JotListFragment;
import com.larryhsiao.nyx.util.EmptyView;
import com.silverhetch.aura.view.EmptyListAdapter;
import com.silverhetch.aura.view.dialog.InputDialog;
import com.silverhetch.clotho.source.ConstSource;
import com.silverhetch.clotho.utility.comparator.StringComparator;

import java.util.Comparator;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to showing/manage tags.
 */
public class TagListFragment extends JotFragment {
    private static final int REQUEST_CODE_NEW_TAG = 1000;
    private TagListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setTitle(getString(R.string.tags));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new TagListAdapter(tag -> {
            nextPage(
                JotListFragment.newInstanceByJotIds(
                    getString(R.string.tag_title, tag.title()),
                    new QueriedJots(new JotsByTagId(db, new ConstSource<>(tag.id())))
                        .value().stream().mapToLong(Jot::id).toArray()
                )
            );
            return new Object();
        }, tag -> {
            final ArrayAdapter<String> tagOptions = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_list_item_1,
                new String[]{getString(R.string.delete)}
            );
            new AlertDialog.Builder(view.getContext())
                .setTitle(tag.title())
                .setAdapter(tagOptions, (dialog, which) -> {
                    if (which == 0) {
                        new TagRemoval(db, tag.id()).fire();
                        adapter.removeTag(tag);
                    }
                }).show();
            return new Object();
        });
        final RecyclerView list = view.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(view.getContext()));
        list.setAdapter(new EmptyListAdapter(adapter, new EmptyView(view.getContext())));
        adapter.loadTags(
            new QueriedTags(
                new AllTags(db)
            ).value().stream().sorted(new Comparator<Tag>() {
                final StringComparator comparator = new StringComparator();

                @Override
                public int compare(Tag o1, Tag o2) {
                    return comparator.compare(o2.title(), o1.title());
                }
            }).collect(Collectors.toList())
        );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.jot_list, menu);
        menu.findItem(R.id.menuItem_viewMode).setVisible(false);

        SearchManager searchManager = ((SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE));
        MenuItem searchMenuItem = menu.findItem(R.id.menuItem_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnCloseListener(() -> {
            searchMenuItem.collapseActionView();
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                StringComparator comparator = new StringComparator();
                adapter.loadTags(
                    new QueriedTags(new TagsByKeyword(db, newText))
                        .value()
                        .stream()
                        .sorted((o1, o2) ->
                            comparator.compare(o2.title(), o1.title())
                        ).collect(Collectors.toList())
                );
                return true;
            }
        });
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setQuery("", true);
                return true;
            }
        });
        searchMenuItem.setOnMenuItemClickListener(item -> {
            searchView.onActionViewExpanded();
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuItem_newJot) {
            final InputDialog frag = InputDialog.Companion.newInstance(
                getString(R.string.new_tag),
                REQUEST_CODE_NEW_TAG
            );
            frag.setTargetFragment(this, REQUEST_CODE_NEW_TAG);
            frag.show(getFragmentManager(), null);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_NEW_TAG == requestCode && RESULT_OK == resultCode) {
            final String newTagName = data.getStringExtra("INPUT_FIELD");
            adapter.appendTag(new NewTag(db, newTagName).value());
        }
    }
}
