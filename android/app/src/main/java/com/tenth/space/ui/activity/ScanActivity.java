/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.tenth.space.R;
import com.tenth.space.utils.PermissionUtil;
import com.tenth.space.utils.ThreadUtil;
import com.tenth.space.utils.Utils;
import com.tenth.space.utils.camera.CameraManager;
import com.tenth.space.BitherjSettings;
import com.tenth.space.utils.qrcode.ScannerView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class ScanActivity extends Activity implements SurfaceHolder.Callback,
        CompoundButton.OnCheckedChangeListener {
    public static final String INTENT_EXTRA_RESULT = "result";
    public static final int FromGalleryRequestCode = 1606;

    private static final long VIBRATE_DURATION = 50L;
	private static final long AUTO_FOCUS_INTERVAL_MS = 2500L;

    protected TextView tv;
    protected TextView tvTitle;

	private final CameraManager cameraManager = new CameraManager();
	protected ScannerView scannerView;
	private SurfaceHolder surfaceHolder;
	protected FrameLayout flOverlayContainer;
    protected ImageButton ibtnGallery;
    private Vibrator vibrator;
    private HandlerThread cameraThread;
    private Handler cameraHandler;

    private boolean fromGallery;

    private static boolean DISABLE_CONTINUOUS_AUTOFOCUS = Build.MODEL.equals("GT-I9100") //
            // Galaxy S2
            || Build.MODEL.equals("SGH-T989") // Galaxy S2
            || Build.MODEL.equals("SGH-T989D") // Galaxy S2 X
            || Build.MODEL.equals("SAMSUNG-SGH-I727") // Galaxy S2 Skyrocket
            || Build.MODEL.equals("GT-I9300") // Galaxy S3
            || Build.MODEL.equals("GT-N7000"); // Galaxy Note

    private static final Logger log = LoggerFactory.getLogger(ScanActivity.class);

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, R.anim.scanner_in_exit);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		setContentView(R.layout.scan_activity);
		flOverlayContainer = (FrameLayout) findViewById(R.id.fl_overlay_container);
		scannerView = (ScannerView) findViewById(R.id.scan_activity_mask);
        ibtnGallery = (ImageButton) findViewById(R.id.ibtn_gallery);
        ibtnGallery.setOnClickListener(galleryClick);
        ((CheckBox) findViewById(R.id.cbx_torch)).setOnCheckedChangeListener(this);
        fromGallery = false;
        setOverlay(R.layout.layout_scan_qr_code_transport_overlay);
        tv = findViewById(R.id.tv);
        tvTitle = findViewById(R.id.tv_title);
        if (!Utils.isEmpty(getIntentTitle())) {
            tvTitle.setText(getIntentTitle());
        }
    }

    public void setOverlay(View v) {
        flOverlayContainer.removeAllViews();
        flOverlayContainer.addView(v, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void setOverlay(int resource) {
        setOverlay(LayoutInflater.from(this).inflate(resource, null));
    }

    private String getIntentTitle() {
        String title = null;
        if (getIntent() != null
                && getIntent().getExtras() != null
                && getIntent().getExtras().containsKey(
				BitherjSettings.INTENT_REF.TITLE_STRING)) {
            title = getIntent().getExtras().getString(
					BitherjSettings.INTENT_REF.TITLE_STRING);
        }
        return title;
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraThread = new HandlerThread("cameraThread", Process.THREAD_PRIORITY_BACKGROUND);
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());

        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.scan_activity_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
    	if (PermissionUtil.isCameraPermission(this, BitherjSettings.REQUEST_CODE_PERMISSION_CAMERA)) {
			cameraHandler.post(openRunnable);
		}
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case BitherjSettings.REQUEST_CODE_PERMISSION_CAMERA:
				if (grantResults != null && grantResults.length > 0) {
					if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        ThreadUtil.getMainThreadHandler().postDelayed(openRunnable, 400);
                    }
				}
			default:
				break;
		}
	}

	@Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
    }

    @Override
	public void surfaceChanged(final SurfaceHolder holder, final int format,
			final int width, final int height) {
	}

	@Override
	protected void onPause() {
		cameraHandler.post(closeRunnable);

		surfaceHolder.removeCallback(this);

		super.onPause();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_CAMERA:
			// don't launch camera app
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraHandler.post(new Runnable() {
				@Override
				public void run() {
					cameraManager
							.setTorch(keyCode == KeyEvent.KEYCODE_VOLUME_UP);
				}
			});
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void handleResult(final Result scanResult, Bitmap thumbnailImage,
                             final float thumbnailScaleFactor) {
		vibrate();
		// superimpose dots to highlight the key features of the qr code
		final ResultPoint[] points = scanResult.getResultPoints();
		if (points != null && points.length > 0) {
			final Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.scan_result_dots));
			paint.setStrokeWidth(10.0f);

			final Canvas canvas = new Canvas(thumbnailImage);
			canvas.scale(thumbnailScaleFactor, thumbnailScaleFactor);
			for (final ResultPoint point : points)
				canvas.drawPoint(point.getX(), point.getY(), paint);
		}

		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		thumbnailImage = Bitmap.createBitmap(thumbnailImage, 0, 0,
				thumbnailImage.getWidth(), thumbnailImage.getHeight(), matrix,
				false);
		scannerView.drawResultBitmap(thumbnailImage);

		final Intent result = getIntent();
		result.putExtra(INTENT_EXTRA_RESULT, scanResult.getText());
		setResult(RESULT_OK, result);

		// delayed finish
		log.info("wystan scan result {}",scanResult.getText());
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});
	}

	public void vibrate() {
		vibrator.vibrate(VIBRATE_DURATION);
	}

	private final Runnable openRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				final Camera camera = cameraManager.open(surfaceHolder,
						!DISABLE_CONTINUOUS_AUTOFOCUS);

				final Rect framingRect = cameraManager.getFrame();
				final Rect framingRectInPreview = cameraManager
						.getFramePreview();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						scannerView.setFraming(framingRect,
								framingRectInPreview);
					}
				});

				final String focusMode = camera.getParameters().getFocusMode();
				final boolean nonContinuousAutoFocus = Camera.Parameters.FOCUS_MODE_AUTO
						.equals(focusMode)
						|| Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode);

				if (nonContinuousAutoFocus)
					cameraHandler.post(new AutoFocusRunnable(camera));

				cameraHandler.post(fetchAndDecodeRunnable);
			} catch (final IOException x) {
				log.info("problem opening camera", x);
				finish();
			} catch (final RuntimeException x) {
				log.info("problem opening camera", x);
				finish();
			}
		}
	};

	private final Runnable closeRunnable = new Runnable() {
		@Override
		public void run() {
			cameraManager.close();

			// cancel background thread
			cameraHandler.removeCallbacksAndMessages(null);
			cameraThread.quit();
		}
	};

    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        if(buttonView.getId() == R.id.cbx_torch){
            if(cameraHandler == null){
                return;
            }
            cameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    cameraManager
                            .setTorch(isChecked);
                }
            });
        }
    }

    private final class AutoFocusRunnable implements Runnable {
		private final Camera camera;

		public AutoFocusRunnable(final Camera camera) {
			this.camera = camera;
		}

		@Override
		public void run() {
			camera.autoFocus(new Camera.AutoFocusCallback() {
				@Override
				public void onAutoFocus(final boolean success,
						final Camera camera) {
					// schedule again
					cameraHandler.postDelayed(AutoFocusRunnable.this,
							AUTO_FOCUS_INTERVAL_MS);
				}
			});
		}
	}

    private View.OnClickListener galleryClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fromGallery = true;
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, FromGalleryRequestCode);
            overridePendingTransition(0, R.anim.scanner_in_exit);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == FromGalleryRequestCode) {
/*
            overridePendingTransition(R.anim.scanner_out_enter, 0);
            if (resultCode == RESULT_OK) {
                fromGallery = true;
                final DialogProgress dp = new DialogProgress(this, R.string.please_wait);
                dp.show();
                new Thread() {
                    @Override
                    public void run() {
                        String text = null;
                        Uri uri = data.getData();
                        if (uri != null) {
                            File fromFile = FileUtil.convertUriToFile(ScanActivity.this, uri);
                            if (fromFile != null && fromFile.exists()) {
                                Bitmap bmp = ImageManageUtil.getBitmapNearestSize(fromFile,
                                        ImageManageUtil.IMAGE_SIZE);
                                if (bmp != null) {
                                    text = decodeQrCodeFromBitmap(bmp);
                                }
                            }
                        }
                        final String r = text;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dp.dismiss();
                                if (r == null) {
                                    fromGallery = false;
                                    DropdownMessage.showDropdownMessage(com.tenth.space.bither.qrcode.ScanActivity.this,
                                            R.string.scan_qr_code_from_photo_wrong);
                                } else {
                                    final Intent result = getIntent();
                                    result.putExtra(INTENT_EXTRA_RESULT, r);
                                    setResult(RESULT_OK, result);
                                    finish();
                                }
                            }
                        });
                    }
                }.start();
            } else {
                fromGallery = false;
            }


            return;
             */
        }


        super.onActivityResult(requestCode, resultCode, data);

		setResult(RESULT_OK, data);

		// delayed finish
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});
    }

    private final Runnable fetchAndDecodeRunnable = new Runnable() {
        private final QRCodeReader reader = new QRCodeReader();
        private final Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType,
                Object>(DecodeHintType.class);

        @Override
        public void run() {
            if (fromGallery) {
                cameraHandler.postDelayed(fetchAndDecodeRunnable, 500);
                return;
            }
            cameraManager.requestPreviewFrame(new PreviewCallback() {
                @Override
                public void onPreviewFrame(final byte[] data, final Camera camera) {
                    decode(data);
                }
            });
        }

        private void decode(final byte[] data) {
            final PlanarYUVLuminanceSource source = cameraManager.buildLuminanceSource(data);
            final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
				hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
						new ResultPointCallback() {
							@Override
							public void foundPossibleResultPoint(
									final ResultPoint dot) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										scannerView.addDot(dot);
									}
								});
							}
						});
				final Result scanResult = reader.decode(bitmap, hints);
				if (!resultValid(scanResult.getText())) {
					cameraHandler.post(fetchAndDecodeRunnable);
					return;
				}
				final int thumbnailWidth = source.getThumbnailWidth();
				final int thumbnailHeight = source.getThumbnailHeight();
				final float thumbnailScaleFactor = (float) thumbnailWidth
						/ source.getWidth();

				final Bitmap thumbnailImage = Bitmap.createBitmap(
						thumbnailWidth, thumbnailHeight,
						Bitmap.Config.ARGB_8888);
				thumbnailImage.setPixels(source.renderThumbnail(), 0,
						thumbnailWidth, 0, 0, thumbnailWidth, thumbnailHeight);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						handleResult(scanResult, thumbnailImage,
								thumbnailScaleFactor);
					}
				});
			} catch (final Exception x) {
				cameraHandler.post(fetchAndDecodeRunnable);
			} finally {
				reader.reset();
			}
		}
	};

	public boolean resultValid(String result) {
		return true;
	}

	public final void startScan() {
		cameraHandler.post(fetchAndDecodeRunnable);
	}

	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.scanner_out_enter, 0);
    }

    private String decodeQrCodeFromBitmap(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        bmp.recycle();
        bmp = null;
        QRCodeReader reader = new QRCodeReader();
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        try {
            Result result = reader.decode(new BinaryBitmap(new HybridBinarizer(new
                    RGBLuminanceSource(width, height, pixels))), hints);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }
}
