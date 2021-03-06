package com.larryhsiao.nyx.jot;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.google.gson.Gson;
import com.larryhsiao.nyx.BuildConfig;
import com.larryhsiao.nyx.LocationString;
import com.larryhsiao.nyx.R;
import com.larryhsiao.nyx.attachments.AttachmentPickerIntent;
import com.larryhsiao.nyx.attachments.AttachmentsFragment;
import com.larryhsiao.nyx.attachments.IsLocalExist;
import com.larryhsiao.nyx.attachments.JotImageLoading;
import com.larryhsiao.nyx.base.JotFragment;
import com.larryhsiao.nyx.core.attachments.Attachment;
import com.larryhsiao.nyx.core.attachments.AttachmentsByJotId;
import com.larryhsiao.nyx.core.attachments.NewAttachment;
import com.larryhsiao.nyx.core.attachments.QueriedAttachments;
import com.larryhsiao.nyx.core.attachments.RemovalAttachment;
import com.larryhsiao.nyx.core.attachments.RemovalAttachmentByJotId;
import com.larryhsiao.nyx.core.jots.ConstJot;
import com.larryhsiao.nyx.core.jots.Jot;
import com.larryhsiao.nyx.core.jots.JotRemoval;
import com.larryhsiao.nyx.core.jots.JotUri;
import com.larryhsiao.nyx.core.jots.PostedJot;
import com.larryhsiao.nyx.core.jots.WrappedJot;
import com.larryhsiao.nyx.core.jots.moods.DefaultMoods;
import com.larryhsiao.nyx.core.jots.moods.MergedMoods;
import com.larryhsiao.nyx.core.jots.moods.RankedMood;
import com.larryhsiao.nyx.core.jots.moods.RankedMoods;
import com.larryhsiao.nyx.core.tags.AllTags;
import com.larryhsiao.nyx.core.tags.JotTagRemoval;
import com.larryhsiao.nyx.core.tags.NewJotTag;
import com.larryhsiao.nyx.core.tags.NewTag;
import com.larryhsiao.nyx.core.tags.QueriedTags;
import com.larryhsiao.nyx.core.tags.Tag;
import com.larryhsiao.nyx.core.tags.TagsByJotId;
import com.larryhsiao.nyx.core.tags.TagsByKeyword;
import com.larryhsiao.nyx.sync.SyncService;
import com.larryhsiao.nyx.util.EmbedMapFragment;
import com.larryhsiao.nyx.util.JpegDateComparator;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.schibstedspain.leku.LocationPickerActivity;
import com.silverhetch.aura.BackControl;
import com.silverhetch.aura.images.exif.ExifAttribute;
import com.silverhetch.aura.images.exif.ExifUnixTimeStamp;
import com.silverhetch.aura.location.LocationAddress;
import com.silverhetch.aura.uri.UriMimeType;
import com.silverhetch.aura.view.alert.Alert;
import com.silverhetch.aura.view.dialog.FullScreenDialogFragment;
import com.silverhetch.aura.view.dialog.InputDialog;
import com.silverhetch.aura.view.fab.FabBehavior;
import com.silverhetch.clotho.date.DateCalendar;
import com.silverhetch.clotho.source.ConstSource;
import com.stfalcon.imageviewer.StfalconImageViewer;
import io.github.ponnamkarthik.richlinkpreview.MetaData;
import io.github.ponnamkarthik.richlinkpreview.ResponseListener;
import io.github.ponnamkarthik.richlinkpreview.RichPreview;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static androidx.appcompat.app.AlertDialog.Builder;
import static androidx.exifinterface.media.ExifInterface.TAG_DATETIME_ORIGINAL;
import static com.android.billingclient.api.BillingClient.SkuType.SUBS;
import static com.android.billingclient.api.Purchase.PurchaseState.PURCHASED;
import static com.larryhsiao.nyx.JotApplication.FILE_PROVIDER_AUTHORITY;
import static com.larryhsiao.nyx.JotApplication.URI_FILE_TEMP_PROVIDER;
import static com.linkedin.urls.detection.UrlDetectorOptions.Default;
import static com.schibstedspain.leku.LocationPickerActivityKt.ADDRESS;
import static com.schibstedspain.leku.LocationPickerActivityKt.LATITUDE;
import static com.schibstedspain.leku.LocationPickerActivityKt.LONGITUDE;
import static java.lang.Double.MIN_VALUE;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.ZONE_OFFSET;

/**
 * Fragment that shows the Jot content.
 *
 * @todo #0 One click touch template jot with geometry, and some pictures.
 * @todo #0 Survey capture image, video and audio in app or use third-party apps.
 * @todo #0 Handle removing http url will still touch new attachment.
 * @todo #0 Loading progress for loading preview url image.
 */
public class JotContentFragment extends JotFragment
    implements BackControl, BillingClientStateListener {
    private static final int REQUEST_CODE_LOCATION_PICKER = 1000;
    private static final int REQUEST_CODE_PICK_FILE = 1001;
    private static final int REQUEST_CODE_INPUT_CUSTOM_MOOD = 1002;
    private static final int REQUEST_CODE_ALERT = 1003;
    private static final int REQUEST_CODE_ATTACHMENT_DIALOG = 1004;
    private static final int REQUEST_CODE_TAKE_PICTURE = 1005;
    private static final int REQUEST_CODE_AI_MAGIC_CAPTURE = 1006;

    private static final String ARG_JOT_JSON = "ARG_JOT";
    private static final String ARG_ATTACHMENT_URI = "ARG_ATTACHMENT_URI";
    private static final String ARG_REQUEST_CODE = "ARG_REQUEST_CODE";
    /**
     * We use fixed file name for taking picture, make sure to move the previous taken file
     * before taking new one.
     */
    private static final String TEMP_FILE_NAME = "JotContentTakePicture.jpg";
    private final List<Uri> attachmentOnView = new ArrayList<>();
    private final Handler mainHandler = new Handler();
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private ChipGroup chipGroup;
    private TextView dateText;
    private TextView locationText;
    private TextView moodText;
    private Jot jot;
    private BillingClient billing;

    public static Fragment newInstance() {
        return newInstance(
            new ConstJot(
                -1,
                "",
                System.currentTimeMillis(),
                new double[]{MIN_VALUE, MIN_VALUE},
                "",
                1,
                false
            ),
            new ArrayList<>(),
            0
        );
    }

    public static Fragment newInstance(Jot jot) {
        return newInstance(jot, new ArrayList<>(), 0);
    }

    public static Fragment newInstance(Jot jot, int requestCode) {
        return newInstance(jot, new ArrayList<>(), requestCode);
    }

    public static Fragment newInstance(Jot jot, ArrayList<String> uris, int requestCode) {
        final Fragment frag = new JotContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JOT_JSON, new Gson().toJson(jot));
        args.putStringArrayList(ARG_ATTACHMENT_URI, uris);
        args.putInt(ARG_REQUEST_CODE, requestCode);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundThread.quitSafely();
        backgroundThread.interrupt();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backgroundThread = new HandlerThread("ContentBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            jot = new Gson().fromJson(
                getArguments().getString(ARG_JOT_JSON, "{}"),
                ConstJot.class
            );
        } else {
            jot = new ConstJot(
                -1,
                "",
                System.currentTimeMillis(),
                new double[]{MIN_VALUE, MIN_VALUE},
                "",
                1,
                false
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_jot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        billing = BillingClient.newBuilder(requireContext())
            .setListener((res, list) -> {

            })
            .enablePendingPurchases()
            .build();
        billing.startConnection(this);
        dateText = view.findViewById(R.id.jot_date);
        dateText.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(view.getContext());
            Calendar calendar = new DateCalendar(jot.createdTime(), Calendar.getInstance()).value();
            dialog.getDatePicker().updateDate(
                calendar.get(YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.setOnDateSetListener((view1, year, month, dayOfMonth) -> {
                jot = new WrappedJot(jot) {
                    @Override
                    public long createdTime() {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, dayOfMonth);
                        return calendar.getTimeInMillis();
                    }
                };
                updateDateIndicator();
            });
            dialog.show();
        });
        updateDateIndicator();
        chipGroup = view.findViewById(R.id.jot_tagGroup);
        EditText contentEditText = view.findViewById(R.id.jot_content);
        contentEditText.setText(jot.content());
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Leave it empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Leave it empty
            }

            @Override
            public void afterTextChanged(Editable s) {
                jot = new WrappedJot(jot) {
                    @Override
                    public String content() {
                        return s.toString();
                    }
                };
                mainHandler.removeCallbacks(handler);
                if (contentEditText.getText().toString().endsWith("\n")) {
                    mainHandler.postDelayed(handler, 1000);
                }
            }

            private final Runnable handler = () -> {
                // @todo #10 Detects too many unusable uri.I
                List<Url> detect = new UrlDetector(
                    contentEditText.getText().toString(),
                    Default
                ).detect();
                if (detect.size() > 0) {
                    preferEnterUrl(detect.get(detect.size() - 1).toString());
                }
            };
        });
        locationText = view.findViewById(R.id.jot_location);
        locationText.setOnClickListener(v -> pickLocation());
        Location location = new Location("Constant");
        location.setLongitude(jot.location()[0]);
        location.setLatitude(jot.location()[1]);
        updateAddress(location);
        loadEmbedMapByJot();
        attachmentOnView.clear();
        attachmentOnView.addAll(
            new QueriedAttachments(new AttachmentsByJotId(db, jot.id()))
                .value()
                .stream()
                .map(it -> Uri.parse(it.uri()))
                .collect(Collectors.toList())
        );

        if (getArguments() != null) {
            final List<String> attachments = getArguments().getStringArrayList(ARG_ATTACHMENT_URI);
            if (attachments != null) {
                for (String uri : attachments) {
                    addAttachmentGrantPermission(Uri.parse(uri));
                }
                updateAttachmentView();
            }
        }

        ImageView tagIcon = view.findViewById(R.id.jot_tagIcon);
        tagIcon.setOnClickListener(v -> {
            final AutoCompleteTextView editText = new AutoCompleteTextView(v.getContext());
            editText.setLines(1);
            editText.setMaxLines(1);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setAdapter(new ArrayAdapter<>(
                    v.getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    new QueriedTags(
                        new AllTags(db)
                    ).value().stream().map(Tag::title).collect(Collectors.toList())
                )
            );
            new AlertDialog.Builder(v.getContext())
                .setTitle(getString(R.string.new_tag))
                .setMessage(getString(R.string.enter_tag_name))
                .setView(editText)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    final String preferTagName = editText.getText().toString();
                    newChip(preferTagName);
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
        });

        for (Tag tag : new QueriedTags(new TagsByJotId(db, jot.id())).value()) {
            Chip chip = new Chip(view.getContext());
            chip.setTag(tag);
            chip.setText(tag.title());
            chip.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(tag.title())
                .setMessage(R.string.delete)
                .setPositiveButton(R.string.confirm, (dialog, which) ->
                    chipGroup.removeView(v)
                )
                .setNegativeButton(R.string.cancel, null)
                .show());
            chipGroup.addView(chip);
        }
        moodText = view.findViewById(R.id.jot_mood);

        String mood = String.valueOf(jot.mood());
        if (mood.isEmpty()) {
            mood = "+";
        }
        moodText.setText(mood);
        moodText.setOnClickListener(v -> {
            final GridView gridView = new GridView(v.getContext());
            gridView.setNumColumns(4);
            gridView.setAdapter(
                new MoodAdapter(
                    v.getContext(),
                    new MergedMoods(
                        new ConstSource<>(
                            new RankedMoods(db).value()
                                .stream()
                                .map(RankedMood::mood)
                                .collect(Collectors.toList())
                        ),
                        new DefaultMoods()
                    ).value()
                )
            );
            AlertDialog moodDialog = new AlertDialog.Builder(v.getContext())
                .setTitle(getString(R.string.moods))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                })
                .setView(gridView)
                .show();
            gridView.setOnItemClickListener((parent, view12, position, id) -> {
                if (position == gridView.getAdapter().getCount() - 1) { // last custom dialog
                    moodDialog.dismiss();
                    InputDialog dialog = InputDialog.Companion.newInstance(
                        getString(R.string.moods),
                        REQUEST_CODE_INPUT_CUSTOM_MOOD
                    );
                    dialog.setTargetFragment(this, REQUEST_CODE_INPUT_CUSTOM_MOOD);
                    dialog.show(getParentFragmentManager(), null);
                    return;
                }
                final String newMood;
                if (position == 0) {
                    newMood = "+";
                } else {
                    newMood = ((TextView) view12).getText().toString();
                }
                jot = new WrappedJot(jot) {
                    @Override
                    public String mood() {
                        return newMood;
                    }
                };
                moodText.setText(newMood);
                moodDialog.dismiss();
            });
        });
        view.findViewById(R.id.jot_ai_magic).setOnClickListener(v ->
            takePicture(REQUEST_CODE_AI_MAGIC_CAPTURE)
        );
    }

    private void preferEnterUrl(String url) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Response res = client.newCall(
                    new Request.Builder()
                        .method("HEAD", null)
                        .url(url)
                        .build()
                ).execute();
                if (res.code() >= 200 && res.code() <= 299) {
                    postAddAttachment(url);
                }
            } catch (Exception ignore) {
            }
        }).start();
    }

    private void postAddAttachment(String url) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                addAttachment(
                    Uri.parse(url),
                    () -> {
                    }
                );
                updateAttachmentView();
            }
        });
    }

    private void newChip(String preferTagName) {
        Tag tag = null;
        for (Tag searched : new QueriedTags(new TagsByKeyword(db, preferTagName)).value()) {
            if (searched.title().equals(preferTagName)) {
                tag = searched;
                break;
            }
        }
        if (tag == null) {
            tag = new NewTag(db, preferTagName).value();
        }
        final Chip tagChip = new Chip(requireContext());
        tagChip.setText(tag.title());
        tagChip.setLines(1);
        tagChip.setMaxLines(1);
        tagChip.setTag(tag);
        tagChip.setOnClickListener(v1 -> tagChipClicked(tagChip));
        chipGroup.addView(tagChip);
    }

    @Override
    public void onResume() {
        super.onResume();
        attachFab(new FabBehavior() {
            @Override
            public int icon() {
                return R.drawable.ic_save;
            }

            @Override
            public void onClick() {
                save();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        detachFab();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        billing.endConnection();
    }

    private void updateAddress(Location location) {
        backgroundHandler.post(() -> {
            final String value = new LocationString(
                new LocationAddress(locationText.getContext(), location).value()
            ).value();
            locationText.post(() -> locationText.setText(value));
        });
    }

    private void pickLocation() {
        LocationPickerActivity.Builder build = new LocationPickerActivity.Builder();
        if (!Arrays.equals(jot.location(), new double[]{MIN_VALUE, MIN_VALUE})
            && !Arrays.equals(jot.location(), new double[]{0.0, 0.0})) {
            build.withLocation(jot.location()[1], jot.location()[0]);
        }
        startActivityForResult(
            build.build(requireContext()),
            REQUEST_CODE_LOCATION_PICKER
        );
    }

    private void tagChipClicked(Chip tagChip) {
        new AlertDialog.Builder(requireContext())
            .setTitle(tagChip.getText().toString())
            .setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new String[]{
                    getString(R.string.delete)
                }
            ), (dialog1, which1) -> onTagOptionClicked(tagChip, which1))
            .show();
    }

    private void onTagOptionClicked(Chip tagChip, int which1) {
        if (which1 == 0) {
            new Builder(requireContext())
                .setTitle(tagChip.getText().toString())
                .setMessage(getString(R.string.delete))
                .setPositiveButton(R.string.confirm, (dialog2, which2) ->
                    chipGroup.removeView(tagChip)
                ).setNegativeButton(R.string.cancel, null)
                .show();
        }
    }

    private void updateDateIndicator() {
        dateText.setText(SimpleDateFormat.getDateInstance().format(new Date(jot.createdTime())));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.jot_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuItem_delete) {
            preferDelete();
        }
        return false;
    }

    private void save() {
        jot = new PostedJot(db, jot).value();
        saveAttachment();
        saveTag();
        SyncService.enqueue(requireContext());
        final Intent intent = new Intent();
        intent.setData(
            Uri.parse(new JotUri(BuildConfig.URI_HOST, jot).value().toASCIIString())
        );
        sendResult(
            requireArguments().getInt(ARG_REQUEST_CODE, 0),
            RESULT_OK,
            intent
        );
    }

    private void saveTag() {
        final Map<Long, Tag> dbTags = new QueriedTags(new TagsByJotId(db, jot.id()))
            .value()
            .stream()
            .collect(Collectors.toMap(Tag::id, tag -> tag));
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Tag tagOnView = ((Tag) chipGroup.getChildAt(i).getTag());
            if (!dbTags.containsKey(tagOnView.id())) {
                // update JOT TAG
                new NewJotTag(
                    db,
                    new ConstSource<>(jot.id()),
                    new ConstSource<>(tagOnView.id())
                ).fire();
            } else {
                dbTags.remove(tagOnView.id());
            }
        }
        dbTags.forEach((aLong, tag) -> new JotTagRemoval(db, jot.id(), tag.id()).fire());
    }

    private void saveAttachment() {
        final List<Attachment> dbAttachments = new QueriedAttachments(
            new AttachmentsByJotId(db, jot.id())
        ).value();
        attachmentOnView.forEach(uri -> {
            boolean hasItem = false;
            List<Attachment> existOnView = new ArrayList<>();
            for (Attachment dbAttachment : dbAttachments) {
                if (dbAttachment.uri().equals(uri.toString())) {
                    hasItem = true;
                    existOnView.add(dbAttachment);
                }
            }
            dbAttachments.removeAll(existOnView);
            if (!hasItem) {
                new NewAttachment(db, uri.toString(), jot.id()).value();
            }
        });
        dbAttachments.forEach((attachment) ->
            new RemovalAttachment(db, attachment.id()).fire()
        );
    }

    private void deleteFlow() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setPositiveButton(R.string.confirm, (dialog, which) -> {
                deleteTempAttachments();
                new RemovalAttachmentByJotId(db, jot.id()).fire();
                new JotRemoval(db, jot.id()).fire();
                getParentFragmentManager().popBackStack();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void discardFlow() {
        if (jot.id() <= 0) {
            try {
                new JotRemoval(db, jot.id()).fire();
            } catch (Exception e) {
                // make sure not have actually inserted into db
                e.printStackTrace();
            }
        }
        if (jot instanceof WrappedJot) { // modified
            new AlertDialog.Builder(requireContext())
                .setTitle(R.string.discard)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    deleteTempAttachments();
                    getParentFragmentManager().popBackStack();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        } else {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            jot = new WrappedJot(jot) {
                @Override
                public double[] location() {
                    return new double[]{
                        data.getDoubleExtra(LONGITUDE, 0.0),
                        data.getDoubleExtra(LATITUDE, 0.0)
                    };
                }
            };
            Address address = data.getParcelableExtra(ADDRESS);
            locationText.setText(address == null ? "" : new LocationString(address).value());
            loadEmbedMapByJot();
        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                addAttachmentGrantPermission(data.getData());
            } else {
                ClipData clip = data.getClipData();
                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        addAttachmentGrantPermission(clip.getItemAt(i).getUri());
                    }
                }
            }
            updateAttachmentView();
        } else if (requestCode == REQUEST_CODE_INPUT_CUSTOM_MOOD && resultCode == RESULT_OK && data != null) {
            final String newMoodRaw = data.getStringExtra("INPUT_FIELD");
            final String newMood;
            if (newMoodRaw != null && newMoodRaw.length() > 1) {
                newMood = newMoodRaw.substring(0, 2);
            } else {
                newMood = "+";
            }
            moodText.setText(newMood);
            jot = new WrappedJot(jot) {
                @Override
                public String mood() {
                    return newMood;
                }
            };
        } else if (requestCode == REQUEST_CODE_ATTACHMENT_DIALOG && data != null) {
            List<Uri> uris = data.getParcelableArrayListExtra("ARG_ATTACHMENT_URI");
            if (uris == null) {
                return;
            }
            attachmentOnView.clear();
            for (Uri uri : uris) {
                addAttachment(uri, this::unsupportedDialog);
            }
            updateAttachmentView();
        } else if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                takeTempPhoto();
            }
        } else if (requestCode == REQUEST_CODE_AI_MAGIC_CAPTURE) {
            if (resultCode == RESULT_OK) {
                premiumAiMagic(
                    FileProvider.getUriForFile(
                        requireContext(),
                        FILE_PROVIDER_AUTHORITY,
                        new File(
                            new File(requireContext().getFilesDir(), "attachments_temp"),
                            TEMP_FILE_NAME
                        )
                    )
                );
            }
        }
    }

    private void takeTempPhoto() {
        File tempDir = new File(requireContext().getFilesDir(), "attachments_temp");
        File fileNameByTime = new File(tempDir, "" + System.currentTimeMillis() + ".jpg");
        new File(tempDir, TEMP_FILE_NAME).renameTo(fileNameByTime);
        addAttachment(
            FileProvider.getUriForFile(
                requireContext(),
                FILE_PROVIDER_AUTHORITY,
                fileNameByTime
            ), this::unsupportedDialog
        );
        updateAttachmentView();
    }

    private void premiumAiMagic(Uri fileUri) {
        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(requireContext(), fileUri);
            final EditText contentEditText = requireView().findViewById(R.id.jot_content);
            FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer()
                .processImage(image)
                .addOnSuccessListener(text -> {
                        if (getView() == null) {
                            return;
                        }
                        contentEditText.append(text.getText());
                    }
                )
                .addOnCompleteListener(task -> FirebaseVision.getInstance()
                    .getVisionBarcodeDetector()
                    .detectInImage(image)
                    .addOnSuccessListener(it -> {
                            for (FirebaseVisionBarcode barcode : it) {
                                contentEditText.append("\n");
                                contentEditText.append(barcode.getDisplayValue());
                            }
                        }
                    ).addOnCompleteListener(it2 -> FirebaseVision.getInstance()
                        .getVisionCloudLandmarkDetector()
                        .detectInImage(image)
                        .addOnSuccessListener(it3 -> {
                            it3.sort((o1, o2) ->
                                (int) ((o1.getConfidence() - o2.getConfidence()) * 100));
                            if (it3.size() > 0) {
                                FirebaseVisionCloudLandmark landmark = it3.get(0);
                                newChip(landmark.getLandmark());
                                if (landmark.getLocations().size() > 0) {
                                    updateJotLocation(landmark.getLocations().get(0));
                                }
                            }
                        }).addOnCompleteListener(it -> takeTempPhoto())
                    )
                );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTempAttachments() {
        for (Uri uri : attachmentOnView) {
            if (uri.toString().startsWith(URI_FILE_TEMP_PROVIDER)) {
                new File(
                    new File(
                        requireContext().getFilesDir(),
                        "attachments_temp"
                    ),
                    uri.toString().replace(URI_FILE_TEMP_PROVIDER, "")
                ).delete();
            }
        }
    }

    private void updateAttachmentView() {
        final FrameLayout root = requireView().findViewById(R.id.jot_attachment_container);
        final TextView newAttachment = requireView().findViewById(R.id.jot_attachment_new);
        newAttachment.setVisibility(VISIBLE);
        newAttachment.setOnClickListener(v -> startPicker());

        final TextView countText = requireView().findViewById(R.id.jot_attachment_count);
        countText.setVisibility(VISIBLE);
        countText.setText(attachmentOnView.size() + "");
        root.removeAllViews();

        if (attachmentOnView.size() == 0) {
            updateEmptyAttachment(root);
            return;
        }
        updateAttachmentViewByMimeType(root);
    }

    private void updateAttachmentViewByMimeType(FrameLayout root) {
        final Uri attachmentUri = attachmentOnView.get(0);
        final String mimeType = new UriMimeType(
            root.getContext(), attachmentUri.toString()
        ).value();
        if (mimeType.startsWith("video/")) {
            updateVideo(root, attachmentUri);
        } else if (mimeType.startsWith("audio/")) {
            updateAudio(root, attachmentUri);
        } else if (mimeType.startsWith("image/")) {
            updateImage(root, attachmentUri);
        } else {
            if (attachmentUri.toString().startsWith("http")) {
                updatePreview(root, attachmentUri);
            } else {
                updateImage(root, attachmentUri);
            }
        }
    }

    private void updatePreview(FrameLayout root, Uri attachmentUri) {
        LayoutInflater.from(requireContext()).inflate(
            R.layout.item_attachment_link_preview,
            root,
            true
        );
        root.setOnClickListener(v -> {
                if (attachmentOnView.size() > 1) {
                    browseAttachments();
                } else {
                    root.getContext().startActivity(new Intent(ACTION_VIEW, attachmentUri));
                }
            }
        );
        root.setOnLongClickListener(v -> {
                showProperties(root, attachmentUri);
                return true;
            }
        );
        RichPreview preview = new RichPreview(new ResponseListener() {
            @Override
            public void onData(MetaData metaData) {
                ImageView icon = root.findViewById(R.id.itemAttachmentUrlPreview_icon);
                Glide.with(root.getContext())
                    .load(metaData.getImageurl())
                    .into(icon);
                TextView content = root.findViewById(R.id.itemAttachmentUrlPreview_title);
                content.setText(metaData.getTitle());
                TextView urlText = root.findViewById(R.id.itemAttachmentUrlPreview_urlText);
                urlText.setText(metaData.getUrl());
            }

            @Override
            public void onError(Exception e) {
                // @todo #0 Error icon for loading preview failed
            }
        });
        preview.getPreview(attachmentUri.toString());
    }

    private void updateEmptyAttachment(FrameLayout root) {
        final TextView countText = requireView().findViewById(R.id.jot_attachment_count);
        countText.setVisibility(GONE);
        final TextView attachmentCount = requireView().findViewById(R.id.jot_attachment_new);
        attachmentCount.setVisibility(GONE);
        LayoutInflater.from(root.getContext()).inflate(
            R.layout.item_add_attachment,
            root
        ).setOnClickListener(it -> startPicker());
    }

    private void startPicker() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_attachment)
            .setItems(R.array.newAttachmentMethods, (dialog, which) -> {
                switch (which) {
                    case 0:
                        takePicture();
                        break;
                    default:
                    case 1:
                        startActivityForResult(
                            new AttachmentPickerIntent().value(),
                            REQUEST_CODE_PICK_FILE
                        );
                }
            })
            .show();
    }

    private void takePicture() {
        takePicture(REQUEST_CODE_TAKE_PICTURE);
    }

    private void takePicture(int requestCode) {
        try {
            Intent intent = new Intent(ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                File tempDir = new File(requireContext().getFilesDir(), "attachments_temp");
                tempDir.mkdir();
                intent.putExtra(EXTRA_OUTPUT, FileProvider.getUriForFile(
                    requireContext(),
                    FILE_PROVIDER_AUTHORITY,
                    new File(tempDir, TEMP_FILE_NAME)
                ));
                startActivityForResult(intent, requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVideo(FrameLayout root, Uri uri) {
        boolean isLocalExist = new IsLocalExist(root.getContext(), uri.toString()).value();
        LayoutInflater.from(requireContext())
            .inflate(R.layout.item_attachment_video, root, true);
        ImageView imageView = root.findViewById(R.id.itemAttachmentVideo_icon);

        if (isLocalExist) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(root.getContext(), uri);
            imageView.setImageBitmap(mmr.getFrameAtTime());
            mmr.release();
        } else {
            imageView.setImageResource(R.drawable.ic_syncing);
        }
        imageView.setOnClickListener(v -> {
            if (attachmentOnView.size() > 1) {
                browseAttachments();
            } else if (isLocalExist) {
                final Intent intent = new Intent(ACTION_VIEW);
                intent.setDataAndType(uri, "video/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                makeText(requireContext(), R.string.File_no_yet_synced, LENGTH_SHORT).show();
            }
        });
        imageView.setOnLongClickListener(v -> {
            showProperties(v, uri);
            return true;
        });
    }

    private void updateAudio(FrameLayout root, Uri uri) {
        boolean isLocalExist = new IsLocalExist(root.getContext(), uri.toString()).value();
        LayoutInflater.from(requireContext()).inflate(R.layout.item_attachment_audio, root, true);
        ImageView imageView = root.findViewById(R.id.itemAttachmentAudio_icon);
        if (!isLocalExist) {
            imageView.setImageResource(R.drawable.ic_syncing);
        }
        imageView.setOnClickListener(v -> {
            if (attachmentOnView.size() > 1) {
                browseAttachments();
            } else if (isLocalExist) {
                final Intent intent = new Intent(ACTION_VIEW);
                intent.setDataAndType(uri, "audio/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                makeText(requireContext(), R.string.File_no_yet_synced, LENGTH_SHORT).show();
            }
        });
        imageView.setOnLongClickListener(v -> {
            showProperties(v, uri);
            return true;
        });
    }

    private void updateImage(FrameLayout root, Uri uri) {
        LayoutInflater.from(requireContext())
            .inflate(R.layout.item_attachment_image, root, true);
        final ImageView icon = root.findViewById(R.id.itemAttachmentImage_icon);
        new JotImageLoading(icon, uri.toString()).fire();
        icon.setOnClickListener(v -> {
            if (attachmentOnView.size() > 1) {
                browseAttachments();
            } else {
                showImage(root.getContext(), uri);
            }
        });
        icon.setOnLongClickListener(v -> {
            showProperties(v, uri);
            return true;
        });
    }

    private void browseAttachments() {
        attachmentOnView.sort(new JpegDateComparator(requireContext()));
        FullScreenDialogFragment dialog = AttachmentsFragment.newInstance(attachmentOnView);
        dialog.setTargetFragment(this, REQUEST_CODE_ATTACHMENT_DIALOG);
        dialog.show(getParentFragmentManager(), null);
    }

    private void showImage(Context context, Uri uri) {
        new StfalconImageViewer.Builder<>(
            context,
            Collections.singletonList(uri),
            (imageView, image) -> new JotImageLoading(imageView, image.toString()).fire()
        ).show();
    }

    private void showProperties(View view, Uri uri) {
        final PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenu().add(R.string.delete).setOnMenuItemClickListener(item -> {
            attachmentOnView.remove(uri);
            updateAttachmentView();
            return true;
        });
        popup.getMenu()
            .add(view.getContext().getString(R.string.properties))
            .setOnMenuItemClickListener(item -> {
                final AlertDialog dialog = new Builder(view.getContext())
                    .setView(R.layout.dialog_properties)
                    .show();
                ((TextView) dialog.findViewById(R.id.properties_text)).setText(
                    getString(R.string.Uri___, uri.toString())
                );
                return true;
            });
        popup.show();
    }

    private void addAttachmentGrantPermission(Uri uri) {
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                uri,
                FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        addAttachment(uri, this::unsupportedDialog);
    }

    private void unsupportedDialog() {
        Alert.Companion.newInstance(
            REQUEST_CODE_ALERT,
            getString(R.string.not_supported_file)
        ).show(getChildFragmentManager(), null);
    }

    private void addAttachment(Uri uri, Runnable unsupportedCallback) {
        if (attachmentOnView.contains(uri)) {
            return;
        }
        final String mimeType = new UriMimeType(requireContext(), uri.toString()).value();
        if (mimeType.startsWith("image")) {
            updateJotWithExif(uri);
            attachmentOnView.add(uri);
        } else if (mimeType.startsWith("video")) {
            attachmentOnView.add(uri);
        } else if (mimeType.startsWith("audio")) {
            attachmentOnView.add(uri);
        } else {
            if (uri.toString().startsWith("http")) {
                attachmentOnView.add(uri);
            } else {
                unsupportedCallback.run();
            }
        }
    }

    private void updateJotWithExif(Uri data) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(data);
            if (inputStream == null) {
                return;
            }
            final ExifInterface exif = new ExifInterface(inputStream);
            updateJotLocation(exif);
            updateJotDateByExif(exif);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateJotDateByExif(ExifInterface exif) {
        final Long time = new ExifUnixTimeStamp(
            new ExifAttribute(
                new ConstSource<>(exif),
                TAG_DATETIME_ORIGINAL
            )
        ).value();

        // invalid time or is a created jot or there are already have attachment there,
        // remains unchanged.
        if (time == -1L || jot.id() != -1L || attachmentOnView.size() > 0) {
            return;
        }
        jot = new WrappedJot(jot) {
            @Override
            public long createdTime() {
                // @todo #0 Time zone determination for EXIF info.
                TimeZone tz = TimeZone.getDefault();
                return time - tz.getOffset(ZONE_OFFSET);
            }
        };
        updateDateIndicator();
    }

    private boolean isLocationSetup() {
        return !Arrays.equals(jot.location(), new double[]{MIN_VALUE, MIN_VALUE})
            && !Arrays.equals(jot.location(), new double[]{0.0, 0.0});
    }

    private void updateJotLocation(ExifInterface exif) {
        final double[] latLong = exif.getLatLong();
        if (isLocationSetup() || latLong == null) {
            return;
        }
        updateJotLocation(latLong[0], latLong[1]);
    }

    private void updateJotLocation(FirebaseVisionLatLng latLng) {
        if (isLocationSetup()) {
            return;
        }
        updateJotLocation(latLng.getLatitude(), latLng.getLongitude());
    }

    private void updateJotLocation(double latitude, double longitude) {
        jot = new WrappedJot(jot) {
            @Override
            public double[] location() {
                return new double[]{longitude, latitude};
            }
        };
        Location location = new Location("constant");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        updateAddress(location);
        loadEmbedMapByJot();
    }

    private void loadEmbedMapByJot() {
        FragmentManager mgr = getChildFragmentManager();
        if (!Arrays.equals(jot.location(), new double[]{MIN_VALUE, MIN_VALUE})
            && !Arrays.equals(jot.location(), new double[]{0.0, 0.0})) {
            mgr.beginTransaction().replace(
                R.id.jot_embedMapContainer,
                EmbedMapFragment.newInstance(jot.location()[0], jot.location()[1])
            ).commit();
            requireView().findViewById(R.id.jot_embedMapContainer).setVisibility(VISIBLE);
        } else {
            Fragment map = mgr.findFragmentById(R.id.jot_embedMapContainer);
            if (map != null) {
                mgr.beginTransaction().remove(map).commit();
            }
            requireView().findViewById(R.id.jot_embedMapContainer).setVisibility(GONE);
        }
    }

    @Override
    public boolean onBackPress() {
        if (jot instanceof WrappedJot) {
            discardFlow();
            return true;
        } else {
            deleteTempAttachments();
            return false;
        }
    }

    private void preferDelete() {
        if (jot.id() < 0) {
            discardFlow();
        } else {
            deleteFlow();
        }
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        List<Purchase> purchasesList = billing.queryPurchases(SUBS).getPurchasesList();
        if (purchasesList != null) {
            for (Purchase purchase : purchasesList) {
                if ("premium".equals(purchase.getSku())
                    && purchase.getPurchaseState() == PURCHASED) {
                    View view = getView();
                    if (view != null) {
                        view.findViewById(R.id.jot_ai_magic).setVisibility(VISIBLE);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void onBillingServiceDisconnected() {

    }
}
