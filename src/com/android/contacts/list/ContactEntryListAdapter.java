/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.contacts.list;

import com.android.contacts.R;
import com.android.contacts.widget.IndexerListAdapter;
import com.android.contacts.widget.TextWithHighlightingFactory;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;

/**
 * Common base class for various contact-related lists, e.g. contact list, phone number list
 * etc.
 */
public abstract class ContactEntryListAdapter extends IndexerListAdapter {

    private static final String TAG = "ContactEntryListAdapter";

    /**
     * Indicates whether the {@link Directory#LOCAL_INVISIBLE} directory should
     * be included in the search.
     */
    private static final boolean LOCAL_INVISIBLE_DIRECTORY_ENABLED = false;

    /**
     * The animation is used here to allocate animated name text views.
     */
    private TextWithHighlightingFactory mTextWithHighlightingFactory;
    private int mDisplayOrder;
    private int mSortOrder;
    private boolean mNameHighlightingEnabled;

    private boolean mDisplayPhotos;
    private boolean mQuickContactEnabled;
    private ContactPhotoLoader mPhotoLoader;

    private String mQueryString;
    private char[] mUpperCaseQueryString;
    private boolean mSearchMode;
    private int mDirectorySearchMode;
    private int mDirectoryResultLimit = Integer.MAX_VALUE;

    private boolean mLoading = true;
    private boolean mEmptyListEnabled = true;

    private boolean mSelectionVisible;

    public ContactEntryListAdapter(Context context) {
        super(context);
        addPartitions();
    }

    @Override
    protected View createPinnedSectionHeaderView(Context context, ViewGroup parent) {
        return new ContactListPinnedHeaderView(context, null);
    }

    @Override
    protected void setPinnedSectionTitle(View pinnedHeaderView, String title) {
        ((ContactListPinnedHeaderView)pinnedHeaderView).setSectionHeader(title);
    }

    protected void addPartitions() {
        addPartition(createDefaultDirectoryPartition());
    }

    protected DirectoryPartition createDefaultDirectoryPartition() {
        DirectoryPartition partition = new DirectoryPartition(true, true);
        partition.setDirectoryId(Directory.DEFAULT);
        partition.setDirectoryType(getContext().getString(R.string.contactsList));
        partition.setPriorityDirectory(true);
        partition.setPhotoSupported(true);
        return partition;
    }

    private int getPartitionByDirectoryId(long id) {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                if (((DirectoryPartition)partition).getDirectoryId() == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public abstract String getContactDisplayName(int position);
    public abstract void configureLoader(CursorLoader loader, long directoryId);

    /**
     * Marks all partitions as "loading"
     */
    public void onDataReload() {
        boolean notify = false;
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition)partition;
                if (!directoryPartition.isLoading()) {
                    notify = true;
                }
                directoryPartition.setStatus(DirectoryPartition.STATUS_NOT_LOADED);
            }
        }
        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void clearPartitions() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition)partition;
                directoryPartition.setStatus(DirectoryPartition.STATUS_NOT_LOADED);
            }
        }
        super.clearPartitions();
    }

    public boolean isSearchMode() {
        return mSearchMode;
    }

    public void setSearchMode(boolean flag) {
        mSearchMode = flag;
    }

    public String getQueryString() {
        return mQueryString;
    }

    public void setQueryString(String queryString) {
        mQueryString = queryString;
        if (TextUtils.isEmpty(queryString)) {
            mUpperCaseQueryString = null;
        } else {
            mUpperCaseQueryString = queryString.toUpperCase().toCharArray();
        }
    }

    public char[] getUpperCaseQueryString() {
        return mUpperCaseQueryString;
    }

    public int getDirectorySearchMode() {
        return mDirectorySearchMode;
    }

    public void setDirectorySearchMode(int mode) {
        mDirectorySearchMode = mode;
    }

    public int getDirectoryResultLimit() {
        return mDirectoryResultLimit;
    }

    public void setDirectoryResultLimit(int limit) {
        this.mDirectoryResultLimit = limit;
    }

    public int getContactNameDisplayOrder() {
        return mDisplayOrder;
    }

    public void setContactNameDisplayOrder(int displayOrder) {
        mDisplayOrder = displayOrder;
    }

    public int getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        mSortOrder = sortOrder;
    }

    public void setNameHighlightingEnabled(boolean flag) {
        mNameHighlightingEnabled = flag;
    }

    public boolean isNameHighlightingEnabled() {
        return mNameHighlightingEnabled;
    }

    public void setTextWithHighlightingFactory(TextWithHighlightingFactory factory) {
        mTextWithHighlightingFactory = factory;
    }

    protected TextWithHighlightingFactory getTextWithHighlightingFactory() {
        return mTextWithHighlightingFactory;
    }

    public void setPhotoLoader(ContactPhotoLoader photoLoader) {
        mPhotoLoader = photoLoader;
    }

    protected ContactPhotoLoader getPhotoLoader() {
        return mPhotoLoader;
    }

    public boolean getDisplayPhotos() {
        return mDisplayPhotos;
    }

    public void setDisplayPhotos(boolean displayPhotos) {
        mDisplayPhotos = displayPhotos;
    }

    public boolean isEmptyListEnabled() {
        return mEmptyListEnabled;
    }

    public void setEmptyListEnabled(boolean flag) {
        mEmptyListEnabled = flag;
    }

    public boolean isSelectionVisible() {
        return mSelectionVisible;
    }

    public void setSelectionVisible(boolean flag) {
        this.mSelectionVisible = flag;
    }

    public boolean isQuickContactEnabled() {
        return mQuickContactEnabled;
    }

    public void setQuickContactEnabled(boolean quickContactEnabled) {
        mQuickContactEnabled = quickContactEnabled;
    }

    public void configureDirectoryLoader(DirectoryListLoader loader) {
        loader.setDirectorySearchMode(mDirectorySearchMode);
        loader.setLocalInvisibleDirectoryEnabled(LOCAL_INVISIBLE_DIRECTORY_ENABLED);
    }

    /**
     * Updates partitions according to the directory meta-data contained in the supplied
     * cursor.  Takes ownership of the cursor and will close it.
     */
    public void changeDirectories(Cursor cursor) {
        HashSet<Long> directoryIds = new HashSet<Long>();

        int idColumnIndex = cursor.getColumnIndex(Directory._ID);
        int directoryTypeColumnIndex = cursor.getColumnIndex(DirectoryListLoader.DIRECTORY_TYPE);
        int displayNameColumnIndex = cursor.getColumnIndex(Directory.DISPLAY_NAME);
        int photoSupportColumnIndex = cursor.getColumnIndex(Directory.PHOTO_SUPPORT);

        // TODO preserve the order of partition to match those of the cursor
        // Phase I: add new directories
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumnIndex);
            directoryIds.add(id);
            if (getPartitionByDirectoryId(id) == -1) {
                DirectoryPartition partition = new DirectoryPartition(false, true);
                partition.setDirectoryId(id);
                partition.setDirectoryType(cursor.getString(directoryTypeColumnIndex));
                partition.setDisplayName(cursor.getString(displayNameColumnIndex));
                int photoSupport = cursor.getInt(photoSupportColumnIndex);
                partition.setPhotoSupported(photoSupport == Directory.PHOTO_SUPPORT_THUMBNAIL_ONLY
                        || photoSupport == Directory.PHOTO_SUPPORT_FULL);
                addPartition(partition);
            }
        }

        // Phase II: remove deleted directories
        int count = getPartitionCount();
        for (int i = count; --i >= 0; ) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                long id = ((DirectoryPartition)partition).getDirectoryId();
                if (!directoryIds.contains(id)) {
                    removePartition(i);
                }
            }
        }

        invalidate();
        notifyDataSetChanged();
    }

    @Override
    public void changeCursor(int partitionIndex, Cursor cursor) {
        if (partitionIndex >= getPartitionCount()) {
            // There is no partition for this data
            return;
        }

        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            ((DirectoryPartition)partition).setStatus(DirectoryPartition.STATUS_LOADED);
        }

        if (mDisplayPhotos && mPhotoLoader != null && isPhotoSupported(partitionIndex)) {
            mPhotoLoader.refreshCache();
        }

        super.changeCursor(partitionIndex, cursor);

        if (isSectionHeaderDisplayEnabled() && partitionIndex == getIndexedPartition()) {
            updateIndexer(cursor);
        }
    }

    public void changeCursor(Cursor cursor) {
        changeCursor(0, cursor);
    }

    /**
     * Updates the indexer, which is used to produce section headers.
     */
    private void updateIndexer(Cursor cursor) {
        if (cursor == null) {
            setIndexer(null);
            return;
        }

        Bundle bundle = cursor.getExtras();
        if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
            String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
            int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
            setIndexer(new ContactsSectionIndexer(sections, counts));
        } else {
            setIndexer(null);
        }
    }

    @Override
    public int getViewTypeCount() {
        // We need a separate view type for each item type, plus another one for
        // each type with header, plus one for "other".
        return getItemViewTypeCount() * 2 + 1;
    }

    @Override
    public int getItemViewType(int partitionIndex, int position) {
        int type = super.getItemViewType(partitionIndex, position);
        if (isSectionHeaderDisplayEnabled() && partitionIndex == getIndexedPartition()) {
            Placement placement = getItemPlacementInSection(position);
            return placement.firstInSection ? type : getItemViewTypeCount() + type;
        } else {
            return type;
        }
    }

    @Override
    public boolean isEmpty() {
        // TODO
//        if (contactsListActivity.mProviderStatus != ProviderStatus.STATUS_NORMAL) {
//            return true;
//        }

        if (!mEmptyListEnabled) {
            return false;
        } else if (isSearchMode()) {
            return TextUtils.isEmpty(getQueryString());
        } else if (mLoading) {
            // We don't want the empty state to show when loading.
            return false;
        } else {
            return super.isEmpty();
        }
    }

    public boolean isLoading() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition
                    && ((DirectoryPartition) partition).isLoading()) {
                return true;
            }
        }
        return false;
    }

    public boolean areAllPartitionsEmpty() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            if (!isPartitionEmpty(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Changes visibility parameters for the default directory partition.
     */
    public void configureDefaultPartition(boolean showIfEmpty, boolean hasHeader) {
        int defaultPartitionIndex = -1;
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition &&
                    ((DirectoryPartition)partition).getDirectoryId() == Directory.DEFAULT) {
                defaultPartitionIndex = i;
                break;
            }
        }
        if (defaultPartitionIndex != -1) {
            setShowIfEmpty(defaultPartitionIndex, showIfEmpty);
            setHasHeader(defaultPartitionIndex, hasHeader);
        }
    }

    @Override
    protected View newHeaderView(Context context, int partition, Cursor cursor,
            ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.directory_header, parent, false);
    }

    @Override
    protected void bindHeaderView(View view, int partitionIndex, Cursor cursor) {
        Partition partition = getPartition(partitionIndex);
        if (!(partition instanceof DirectoryPartition)) {
            return;
        }

        DirectoryPartition directoryPartition = (DirectoryPartition)partition;
        long directoryId = directoryPartition.getDirectoryId();
        TextView labelTextView = (TextView)view.findViewById(R.id.label);
        TextView displayNameTextView = (TextView)view.findViewById(R.id.display_name);
        if (directoryId == Directory.DEFAULT || directoryId == Directory.LOCAL_INVISIBLE) {
            labelTextView.setText(R.string.local_search_label);
            displayNameTextView.setText(null);
        } else {
            labelTextView.setText(R.string.directory_search_label);
            displayNameTextView.setText(buildDirectoryName(directoryPartition.getDirectoryType(),
                    directoryPartition.getDisplayName()));
        }

        TextView countText = (TextView)view.findViewById(R.id.count);
        if (directoryPartition.isLoading()) {
            countText.setText(R.string.search_results_searching);
        } else {
            int count = cursor == null ? 0 : cursor.getCount();
            if (directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE
                    && count >= getDirectoryResultLimit()) {
                countText.setText(mContext.getString(
                        R.string.foundTooManyContacts, getDirectoryResultLimit()));
            } else {
                countText.setText(getQuantityText(
                        count, R.string.listFoundAllContactsZero, R.plurals.searchFoundContacts));
            }
        }
    }

    private CharSequence buildDirectoryName(String directoryType, String directoryName) {
        String title;
        if (!TextUtils.isEmpty(directoryName)) {
            title = directoryName;
            // TODO: STOPSHIP - remove this once this is done by both directory providers
            int atIndex = title.indexOf('@');
            if (atIndex != -1 && atIndex < title.length() - 2) {
                final char firstLetter = Character.toUpperCase(title.charAt(atIndex + 1));
                title = firstLetter + title.substring(atIndex + 2);
            }
        } else {
            title = directoryType;
        }

        return title;
    }

    // TODO: fix PluralRules to handle zero correctly and use Resources.getQuantityText directly
    public String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
        if (count == 0) {
            return getContext().getString(zeroResourceId);
        } else {
            String format = getContext().getResources()
                    .getQuantityText(pluralResourceId, count).toString();
            return String.format(format, count);
        }
    }

    public boolean isPhotoSupported(int partitionIndex) {
        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            return ((DirectoryPartition) partition).isPhotoSupported();
        }
        return true;
    }
}
