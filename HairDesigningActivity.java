package com.example.menhairstyles;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HairDesigningActivity extends AppCompatActivity {
    ImageView gallry2,  turbanedit, save, ShareImg,adj_brightness,back;
    LinearLayout brightness_pannel;
    TextView brightnes,brightnesval;
    int check_act = 0;
    ImageView selectedpic;
    Handler handler;
    Runnable runnable;
    FrameLayout main_layout;
    int counter = 0;
    private int stickerReturn = 340;
    StickerView stickerView;
    float contrast;
    float brightness = 0;
    LinearLayout linearLayout;
    PorterDuff.Mode[] optMode = PorterDuff.Mode.values();
    RelativeLayout mRelativeLayout;
    SeekBar seekbar;
    private RelativeLayout mRootLayout;
    Context context;
    int main_w;
    int main_h;
    File mediaStorageDir;
    File mediaStorageDir_thumb;
    String mImageName;
    public static final int STORAGE_PERMISSION_REQ = 121;
    private static final String TAG = "CHECK_SAVE";
    Bitmap cache = null;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hair_designing);

        selectedpic = findViewById(R.id.select_image);
        gallry2 = findViewById(R.id.gallery2);
        turbanedit = findViewById(R.id.turbans_edit);
        save = findViewById(R.id.save_img);
        ShareImg = findViewById(R.id.share_img);
        adj_brightness=findViewById(R.id.adjust_brightness);
        seekbar=findViewById(R.id.brightness_bar);
        back=findViewById(R.id.back);
        stickerView = findViewById(R.id.sticker_view);
        mRootLayout = (RelativeLayout) findViewById(R.id.mRootLayout);
        mRelativeLayout=findViewById(R.id.layout2);
        brightness_pannel=findViewById(R.id.brightness_pannel);
        main_layout=findViewById(R.id.frame);

        this.context = this;
        this.main_layout =  findViewById(R.id.frame);
        int[] iArr = get_dis_dims();
        this.main_layout.setMinimumWidth(iArr[0]);
        this.main_layout.setMinimumHeight(iArr[0]);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Interstitialad();


        stickerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                stickerView.setLocked(false);
                return false;
            }
        });

        Intent intent = getIntent();
        String image_path = intent.getStringExtra("Img");
        Uri fileUri = Uri.parse(image_path);
        selectedpic.setImageURI(fileUri);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        gallry2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentgallery = new Intent(Intent.ACTION_PICK);
                intentgallery.setType("image/*");
                startActivityForResult(intentgallery, 4);
            }
        });

        turbanedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_act = 5;
                handler.postDelayed(runnable, 0);
            }
        });

        save.setOnClickListener(v->{
            if (stickerView.isNoneSticker()) {
                Toast.makeText(HairDesigningActivity.this, "Design Something", Toast.LENGTH_SHORT).show();
                return;
            }

            HairDesigningActivity.this.check_act = 6;
            if (HairDesigningActivity.this.check_Storage_Permission()) {
                HairDesigningActivity.this.stickerView.setLocked(true);
                HairDesigningActivity.this.main_layout.setDrawingCacheEnabled(false);
                HairDesigningActivity.this.main_layout.setDrawingCacheEnabled(true);
                HairDesigningActivity.this.main_layout.buildDrawingCache();
                HairDesigningActivity.this.cache = HairDesigningActivity.this.main_layout.getDrawingCache();
                HairDesigningActivity.this.main_w = HairDesigningActivity.this.main_layout.getMeasuredWidth();
                HairDesigningActivity.this.main_h = HairDesigningActivity.this.main_layout.getMeasuredHeight();
                Bitmap viewCache = main_layout.getDrawingCache();
                Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
                new SaveTask(false).execute(bitmap);
            }

        });

        ShareImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stickerView.setLocked(true);
                main_layout.setDrawingCacheEnabled(true);
                Bitmap bitmap;
                bitmap = main_layout.getDrawingCache();
                bitmap = main_layout.getDrawingCache();

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "title");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);
                Log.e("URIIIII",uri+"");

                OutputStream outstream;
                try {

                    outstream = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                    outstream.close();
                } catch (Exception e) {
                    System.err.println(e.toString());
                }

                share.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(share, "Share Image"));

            }
        });

        adj_brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(brightness_pannel.getVisibility()==View.GONE) {
                    brightness_pannel.setVisibility(View.VISIBLE);
                }else {
                    brightness_pannel.setVisibility(View.GONE);
                }
            }
        });


        this.handler = new Handler();
        this.runnable = new Runnable() {
            public void run() {
                HairDesigningActivity.this.handler.postDelayed(this, 10);
                HairDesigningActivity.this.counter++;
                if (HairDesigningActivity.this.counter == 12) {
                    HairDesigningActivity.this.handler.removeCallbacks(HairDesigningActivity.this.runnable);
                    HairDesigningActivity.this.counter = 0;

                    switch (HairDesigningActivity.this.check_act) {

                        case 5:
                            HairDesigningActivity.this.startActivityForResult(new Intent(HairDesigningActivity.this, HairActivity.class), stickerReturn);
                            break;
                        case 6:
                            HairDesigningActivity.this.finish();
                            break;
                    }
                    HairDesigningActivity.this.check_act = 0;
                }
            }
        };

    }

    public int[] get_dis_dims() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    //// Color Matrix
    ColorMatrixColorFilter getContrastBrightnessFilter(float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        return new ColorMatrixColorFilter(cm);
    }

    private class SaveTask extends AsyncTask<Bitmap, Void, File> {
        private boolean isShare;
        private ProgressDialog mProgressDialog;

        public SaveTask(boolean b) {
            this.isShare = b;
        }

        protected void onPreExecute() {
            this.mProgressDialog = new ProgressDialog(HairDesigningActivity.this);
            this.mProgressDialog.setMessage(HairDesigningActivity.this.getString(R.string.saving));
            this.mProgressDialog.setIndeterminate(true);
            this.mProgressDialog.show();
        }

        protected void onPostExecute(final File result) {
            this.mProgressDialog.dismiss();
            if (result == null) {
                return;
            }
            if (this.isShare) {
                Intent sendIntent = new Intent("android.intent.action.SEND");
                sendIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(result.getAbsolutePath())));
                sendIntent.setType("image/jpeg*");
                HairDesigningActivity.this.startActivity(Intent.createChooser(sendIntent, "Share via"));
                return;
            }

            Snackbar.make(findViewById(android.R.id.content), HairDesigningActivity.this.getString(R.string.paint_saved), Snackbar.LENGTH_LONG).show();
        }

        @SuppressLint({"SimpleDateFormat"})
        protected File doInBackground(Bitmap... params) {
            Throwable th;
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/Men Hair Styles");
//            File folder = new File(Environment.getExternalStorageDirectory().toString() + File.separator + AppConstance.DIRECTORY_NAME);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File result = new File(folder.toString(), new SimpleDateFormat("'Hair_'yyyy-MM-dd_HH-mm-ss.S'.png'").format(new Date()));
            FileOutputStream stream = null;
            try {
                FileOutputStream stream2 = new FileOutputStream(result);
                try {
                    if (params[0].compress(Bitmap.CompressFormat.JPEG, 100, stream2)) {
                        HairDesigningActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(result)));
                    } else {
                        result = null;
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e) {
                            stream = stream2;
                            result = null;
                            Thread.sleep(1000);
                            return result;
                        }
                    }
                    stream = stream2;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e3) {
                            result = null;
                            Thread.sleep(1000);
                            return result;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (stream != null) {

                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    throw th;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            return result;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == stickerReturn) {  ///StickerActivity Data Returning
            if (resultCode == Activity.RESULT_OK) {
                String stickerPath = data.getExtras().getString("stickerPath");
                Glide.with(this).load(stickerPath)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                stickerView.addSticker(new DrawableSticker(resource),
                                        Sticker.Position.CENTER | Sticker.Position.CENTER);
                            }
                        });
            }
        } else if (requestCode == 4 && data != null) {
            if(resultCode == Activity.RESULT_OK)
                switch (requestCode){
                    case 4:
                        Uri selectedImage = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImage);
                            selectedpic.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                        break;
                }

        }

    }

    /* access modifiers changed from: private */
    public File getOutputMediaFile() {
        this.mediaStorageDir = new File(getExternalFilesDir((String) null), "hairedits");
        if (!this.mediaStorageDir.exists()) {
            this.mediaStorageDir.mkdir();
        }
        this.mediaStorageDir_thumb = new File(getExternalFilesDir((String) null), "RecyclerDetail_thumb");
        if (!this.mediaStorageDir_thumb.exists()) {
            this.mediaStorageDir_thumb.mkdir();
        }
        String format = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        this.mImageName = "MI_" + format + ".png";
        return new File(this.mediaStorageDir.getPath() + File.separator + this.mImageName);
    }

    @SuppressLint("ResourceType")
    public boolean check_Storage_Permission() {
        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this.context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this.context, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setCancelable(true);
            builder.setTitle("Permission necessary");
            builder.setMessage("Camera permission is needed in order to use this feature");
            builder.setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                @TargetApi(16)
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions((Activity) HairDesigningActivity.this.context, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, HairDesigningActivity.STORAGE_PERMISSION_REQ);
                }
            });
            builder.create().show();
        } else {
            ActivityCompat.requestPermissions((Activity) this.context, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, STORAGE_PERMISSION_REQ);
        }
        return false;
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 121 && iArr.length > 0 && iArr[0] == 0) {
            Bitmap viewCache = main_layout.getDrawingCache();
            Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
            new SaveTask(false).execute(bitmap);
        }
    }

    private void Interstitialad(){

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,getString(R.string.interstitial), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });

    }
}
