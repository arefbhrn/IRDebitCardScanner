package ir.arefdev.irdebitcardscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.io.File;
import java.util.LinkedList;

class MachineLearningThread implements Runnable {

	class RunArguments {

		private final byte[] mFrameBytes;
		private final Bitmap mBitmap;
		private final OnScanListener mScanListener;
		private final OnObjectListener mObjectListener;
		private final Context mContext;
		private final int mWidth;
		private final int mHeight;
		private final int mFormat;
		private final int mSensorOrientation;
		private final float mRoiCenterYRatio;
		private final boolean mIsOcr;
		private final File mObjectDetectFile;

		RunArguments(byte[] frameBytes, int width, int height, int format,
					 int sensorOrientation, OnScanListener scanListener, Context context,
					 float roiCenterYRatio) {
			mFrameBytes = frameBytes;
			mBitmap = null;
			mWidth = width;
			mHeight = height;
			mFormat = format;
			mScanListener = scanListener;
			mContext = context;
			mSensorOrientation = sensorOrientation;
			mRoiCenterYRatio = roiCenterYRatio;
			mIsOcr = true;
			mObjectListener = null;
			mObjectDetectFile = null;
		}

		RunArguments(byte[] frameBytes, int width, int height, int format,
					 int sensorOrientation, OnObjectListener objectListener, Context context,
					 float roiCenterYRatio, File objectDetectFile) {
			mFrameBytes = frameBytes;
			mBitmap = null;
			mWidth = width;
			mHeight = height;
			mFormat = format;
			mScanListener = null;
			mContext = context;
			mSensorOrientation = sensorOrientation;
			mRoiCenterYRatio = roiCenterYRatio;
			mIsOcr = false;
			mObjectListener = objectListener;
			mObjectDetectFile = objectDetectFile;
		}

		// this should only be used for testing
		RunArguments(Bitmap bitmap, OnScanListener scanListener, Context context) {
			mFrameBytes = null;
			mBitmap = bitmap;
			mWidth = bitmap == null ? 0 : bitmap.getWidth();
			mHeight = bitmap == null ? 0 : bitmap.getHeight();
			mFormat = 0;
			mScanListener = scanListener;
			mContext = context;
			mSensorOrientation = 0;
			mRoiCenterYRatio = 0;
			mIsOcr = true;
			mObjectListener = null;
			mObjectDetectFile = null;
		}

		// this should only be used for testing
		RunArguments(Bitmap bitmap, OnObjectListener objectListener, Context context,
					 File objectDetectFile) {
			mFrameBytes = null;
			mBitmap = bitmap;
			mWidth = bitmap == null ? 0 : bitmap.getWidth();
			mHeight = bitmap == null ? 0 : bitmap.getHeight();
			mFormat = 0;
			mScanListener = null;
			mContext = context;
			mSensorOrientation = 0;
			mRoiCenterYRatio = 0;
			mIsOcr = false;
			mObjectListener = objectListener;
			mObjectDetectFile = objectDetectFile;
		}
	}

	private LinkedList<RunArguments> queue = new LinkedList<>();

	MachineLearningThread() {
		super();
	}

	synchronized void warmUp(Context context) {
		if (OCR.isInit() || !queue.isEmpty()) {
			return;
		}
		RunArguments args = new RunArguments(null, 0, 0, 0,
				90, null, context, 0.5f);
		queue.push(args);
		notify();
	}

	synchronized void post(Bitmap bitmap, OnScanListener scanListener, Context context) {
		RunArguments args = new RunArguments(bitmap, scanListener, context);
		queue.push(args);
		notify();
	}

	synchronized void post(byte[] bytes, int width, int height, int format, int sensorOrientation,
						   OnScanListener scanListener, Context context, float roiCenterYRatio) {
		RunArguments args = new RunArguments(bytes, width, height, format, sensorOrientation,
				scanListener, context, roiCenterYRatio);
		queue.push(args);
		notify();
	}

	synchronized void post(Bitmap bitmap, OnObjectListener objectListener, Context context,
						   File objectDetectFile) {
		RunArguments args = new RunArguments(bitmap, objectListener, context, objectDetectFile);
		queue.push(args);
		notify();
	}

	synchronized void post(byte[] bytes, int width, int height, int format, int sensorOrientation,
						   OnObjectListener objectListener, Context context, float roiCenterYRatio,
						   File objectDetectFile) {
		RunArguments args = new RunArguments(bytes, width, height, format, sensorOrientation,
				objectListener, context, roiCenterYRatio, objectDetectFile);
		queue.push(args);
		notify();
	}

	// from https://stackoverflow.com/questions/43623817/android-yuv-nv12-to-rgb-conversion-with-renderscript
	// interestingly the question had the right algorithm for our format (yuv nv21)
	private Bitmap YUV_toRGB(byte[] yuvByteArray, int W, int H, Context ctx) {
		RenderScript rs = RenderScript.create(ctx);
		ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs,
				Element.U8_4(rs));

		Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuvByteArray.length);
		Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

		Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(W).setY(H);
		Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

		in.copyFrom(yuvByteArray);

		yuvToRgbIntrinsic.setInput(in);
		yuvToRgbIntrinsic.forEach(out);
		Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
		out.copyTo(bmp);

		yuvToRgbIntrinsic.destroy();
		rs.destroy();
		in.destroy();
		out.destroy();
		return bmp;
	}

	private Bitmap getBitmap(byte[] bytes, int width, int height, int format, int sensorOrientation,
							 float roiCenterYRatio, Context ctx, boolean isOcr) {
		final Bitmap bitmap = YUV_toRGB(bytes, width, height, ctx);

		sensorOrientation = sensorOrientation % 360;

		double h;
		double w;
		int x;
		int y;

		if (sensorOrientation == 0) {
			w = bitmap.getWidth();
			h = isOcr ? w * 302.0 / 480.0 : w;
			x = 0;
			y = (int) Math.round(((double) bitmap.getHeight()) * roiCenterYRatio - h * 0.5);
		} else if (sensorOrientation == 90) {
			h = bitmap.getHeight();
			w = isOcr ? h * 302.0 / 480.0 : h;
			y = 0;
			x = (int) Math.round(((double) bitmap.getWidth()) * roiCenterYRatio - w * 0.5);
		} else if (sensorOrientation == 180) {
			w = bitmap.getWidth();
			h = isOcr ? w * 302.0 / 480.0 : w;
			x = 0;
			y = (int) Math.round(((double) bitmap.getHeight()) * (1.0 - roiCenterYRatio) - h * 0.5);
		} else {
			h = bitmap.getHeight();
			w = isOcr ? h * 302.0 / 480.0 : h;
			x = (int) Math.round(((double) bitmap.getWidth()) * (1.0 - roiCenterYRatio) - w * 0.5);
			y = 0;
		}

		// make sure that our crop stays within the image
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		if ((x + w) > bitmap.getWidth()) {
			x = bitmap.getWidth() - (int) w;
		}
		if ((y + h) > bitmap.getHeight()) {
			y = bitmap.getHeight() - (int) h;
		}

		Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, (int) w, (int) h);

		Matrix matrix = new Matrix();
		matrix.postRotate(sensorOrientation);
		Bitmap bm = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(),
				matrix, true);

		croppedBitmap.recycle();
		bitmap.recycle();

		return bm;
	}

	private synchronized RunArguments getNextImage() {
		while (queue.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return queue.pop();
	}

	private void runObjectModel(final Bitmap bitmap, final RunArguments args) {
		if (args.mObjectDetectFile == null) {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (args.mObjectListener != null) {
						args.mObjectListener.onPrediction(bitmap, bitmap.getWidth(), bitmap.getHeight());
					}
				}
			});
			return;
		}

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			public void run() {
				try {
					if (args.mObjectListener != null) {
						args.mObjectListener.onPrediction(bitmap, bitmap.getWidth(), bitmap.getHeight());
					}
				} catch (Error | Exception e) {
					// prevent callbacks from crashing the app, swallow it
					e.printStackTrace();
				}
			}
		});
	}

	private void runOcrModel(final Bitmap bitmap, final RunArguments args) {
		final OCR ocr = new OCR();
		final String number = ocr.predict(bitmap, args.mContext);
		final boolean hadUnrecoverableException = ocr.hadUnrecoverableException;
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			public void run() {
				try {
					if (args.mScanListener != null) {
						if (hadUnrecoverableException) {
							args.mScanListener.onFatalError();
						} else {
							args.mScanListener.onPrediction(number, ocr.expiry, bitmap, ocr.digitBoxes,
									ocr.expiryBox);
						}
					}
				} catch (Error | Exception e) {
					// prevent callbacks from crashing the app, swallow it
					e.printStackTrace();
				}
			}
		});
	}

	private void runModel() {
		final RunArguments args = getNextImage();

		Bitmap bm;
		if (args.mFrameBytes != null) {
			bm = getBitmap(args.mFrameBytes, args.mWidth, args.mHeight, args.mFormat,
					args.mSensorOrientation, args.mRoiCenterYRatio, args.mContext, args.mIsOcr);
		} else if (args.mBitmap != null) {
			bm = args.mBitmap;
		} else {
			bm = Bitmap.createBitmap(480, 302, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bm);
			Paint paint = new Paint();
			paint.setColor(Color.GRAY);
			canvas.drawRect(0.0f, 0.0f, 480.0f, 302.0f, paint);
		}

		if (args.mIsOcr) {
			runOcrModel(bm, args);
		} else {
			runObjectModel(bm, args);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				runModel();
			} catch (Error | Exception e) {
				// center field exception handling, make sure that the ml thread keeps running
				e.printStackTrace();
			}
		}
	}
}
