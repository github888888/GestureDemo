package com.example.chenlong.gesturedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Long
 * date： 2015-12-22 0022.
 * email: github888888@163.com
 */
public class GestureView extends View {
    private static final String TAG = "GestureView";
    private Context context;
    /**
     * 控件的寬度
     */
    private int mWidth;
    /**
     * 控件的高度
     */
    private int mHeight;
    /**
     * 屏幕宽度
     */
    private int screenWidth;
    /**
     * 屏幕高度
     */
    private int screenHeight;
    /**
     * 屏幕密度，不需要资源文件
     */
    private float dencity;
    /**
     * 外部空心圓选中画笔
     **/
    private Paint outdeepBluePaint;
    /**
     * 外部空心圆未选中画笔
     */
    private Paint outlightBluePaint;
    /**
     * 内部实心圆选中画笔
     */
    private Paint indeepBluePaint;
    /**
     * 连线画笔
     */
    private Paint linePaint;
    /**
     * 圆列表信息
     */
    private List<CircleInfo> list;
    /**
     * 初始密码
     */
    private List<Integer> rowPasswordlist = new ArrayList<Integer>();
    /**
     * 输入密码
     */
    private List<Integer> passwordList;

    /**
     * 手指移动的X位置信息，只相对于本控件
     */
    private float curX;
    /**
     * 手指移动的Y位置信息，只相对于本控件
     */
    private float curY;

    /**
     * 上一个圆信息，用于画线
     */
    private CircleInfo pre = null;
    /**
     * 箭头信息，用于绘制箭头
     */
    private Path path;

    /**
     * 手指离开屏幕的标志位，用于控制显示箭头
     */
    private boolean isUP = false;

    public GestureView(Context context) {
        this(context, null);
    }

    public GestureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        // 获取屏幕宽度和高度
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        dencity = metrics.density;

        // 考虑横屏的问题，显示为正方形
        screenWidth = screenWidth < screenHeight ? screenWidth : screenHeight;

        outdeepBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outdeepBluePaint.setColor(0xFF00009C); // 新深藏青色
        outdeepBluePaint.setStyle(Paint.Style.STROKE);
        outdeepBluePaint.setStrokeWidth(2 * dencity); // 设置刻画的宽度

        outlightBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlightBluePaint.setColor(0xFF4D4DFF); // 霓虹蓝
        outlightBluePaint.setStyle(Paint.Style.STROKE);
        outlightBluePaint.setStrokeWidth(1 * dencity); // 设置刻画的宽度

        indeepBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indeepBluePaint.setColor(0xFF00009C); // 新深藏青色
        indeepBluePaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF00009C);
        linePaint.setStrokeWidth(2 * dencity);

        // 'Z'型密码
        rowPasswordlist.add(0);
        rowPasswordlist.add(1);
        rowPasswordlist.add(2);
        rowPasswordlist.add(4);
        rowPasswordlist.add(6);
        rowPasswordlist.add(7);
        rowPasswordlist.add(8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthsize = MeasureSpec.getSize(widthMeasureSpec);
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);
        int heightsize = MeasureSpec.getSize(heightMeasureSpec);
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);

        // 测量宽度，如果是wrapcontent就指定为屏幕宽度的 4/5
        if (MeasureSpec.EXACTLY == widthmode) {
            mWidth = widthsize;
        } else {
            mWidth = screenWidth * 4 / 5;
            if (MeasureSpec.AT_MOST == widthmode) {
                mWidth = Math.min(mWidth, widthsize);
            }
        }

        // 测量宽度，如果是wrapcontent就指定为屏幕宽度的 4/5 (正方形)
        if (MeasureSpec.EXACTLY == heightmode) {
            mHeight = heightsize;
        } else {
            mHeight = screenWidth * 4 / 5;
            if (MeasureSpec.AT_MOST == heightmode) {
                mHeight = Math.min(mHeight, heightsize);
            }
        }

        setMeasuredDimension(mWidth, mHeight);
        // 计算每个圆的坐标
        prepareData();
    }

    private void prepareData() {
        int outWidth = mWidth / 10;
        int inWidth = mWidth / 30;
        list = new ArrayList<CircleInfo>();
        for (int i = 0; i < 9; i++) {
            CircleInfo info = new CircleInfo();
            info.id = i;
            info.x = mWidth * (i % 3) / 3 + mWidth / 6;
            info.y = mWidth * (i / 3) / 3 + mWidth / 6;
            info.outRadius = outWidth;
            info.inRadius = inWidth;
            info.isSelect = false;
            list.add(info);
        }
        // 初始化手势密码的列表，用于记录移动中的手势轨迹
        passwordList = new ArrayList<Integer>();
        // 准备一个三角形的路径，用于绘制箭头
        path = new Path();
        path.moveTo( -mWidth * 1f / 90, - mWidth * 1f / 18);
        path.lineTo(0f, - mWidth * 7f / 90);
        path.lineTo( mWidth * 1f / 90, - mWidth * 1f / 18);
        path.close();
        path.setFillType(Path.FillType.WINDING);
    }


    // 九宫格圆信息（索引，坐标，外圆半径，内圆半径，是否被选中）
    private class CircleInfo {
        public int id; // 索引
        public int x; // x坐标
        public int y; // y坐标
        public int outRadius; // 外圆半径
        public int inRadius; // 内圆半径
        public boolean isSelect; // 是否选中

        /**
         * 判断是否在圆内
         * @param movex
         * @param movey
         * @return
         */
        public boolean isContain(float movex, float movey) {
            return Math.sqrt(Math.pow(Math.abs(movex - x), 2) + Math.pow(Math.abs(movey - y), 2)) <= outRadius ? true : false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        pre = null;
        canvas.drawColor(Color.WHITE);
        for (CircleInfo item : list) {
            if (item.isSelect) {
                // 选中绘制新深藏青色
                canvas.drawCircle(item.x, item.y, item.outRadius, outdeepBluePaint);
                canvas.drawCircle(item.x, item.y, item.inRadius, indeepBluePaint);
            } else {
                // 不选中绘制霓虹蓝
                canvas.drawCircle(item.x, item.y, item.outRadius, outlightBluePaint);
            }
        }
        for (int position : passwordList) {
            if (null != pre) {
                // 绘制线段
                canvas.drawLine(pre.x, pre.y, list.get(position).x, list.get(position).y, outdeepBluePaint);
            }
            pre = list.get(position);
        }
        // 绘制手指和最后一个轨迹记录的连线
        if (null != pre) {
            canvas.drawLine(pre.x, pre.y, curX, curY, outdeepBluePaint);
        }
        // 抬手后绘制的箭头信息
        for (int i = 0;isUP &&  i + 1 < passwordList.size(); i++) {
            CircleInfo startChild = list.get(passwordList.get(i));
            CircleInfo nextChild = list.get(passwordList.get(i + 1));

            int dx = nextChild.x - startChild.x;
            int dy = nextChild.y - startChild.y;
            //  计算角度
            int angle = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
            // 首先保存状态，然后移动画布，接着旋转画布，最后画箭头
            canvas.save();
            canvas.translate(startChild.x, startChild.y);
            canvas.rotate(angle);
            canvas.drawPath(path, outdeepBluePaint);
            canvas.restore();
        }
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                // 根据curX,curY可以确定position
                int position = (int) (curX * 3 / mWidth) + (int) (curY * 3 / mWidth) * 3;
                // 防止下标越界的问题
                position = position < 0 ? 0 : position >= 8 ? 8 : position;
                Log.i(TAG, "position = " + position);
                // 如果轨迹中不包含position，接着需要判断是否在圆内
                if (!passwordList.contains(position) && list.get(position).isContain(curX, curY)) {
                    list.get(position).isSelect = true;
                    passwordList.add(position);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 验证手势轨迹和原密码是否相同
                int i = 0;
                for (; i < rowPasswordlist.size() && i < passwordList.size(); i++) {
                    if (rowPasswordlist.get(i) != passwordList.get(i)) break;
                }
                // 长度要保持一致，同时i的值要等于原密码的长度
                if (passwordList.size() == rowPasswordlist.size() && i == rowPasswordlist.size()) {
                    Toast.makeText(context, "success", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "failure", Toast.LENGTH_LONG).show();
                    outdeepBluePaint.setColor(Color.RED);
                    indeepBluePaint.setColor(Color.RED);
                }
                // 延时500ms，重置状态
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isUP = false;
                        // reset list
                        for (CircleInfo item : list) {
                            item.isSelect = false;
                        }
                        passwordList.clear();
                        pre = null;
                        outdeepBluePaint.setColor(0xFF00009C);
                        indeepBluePaint.setColor(0xFF00009C);
                        invalidate();
                    }
                }, 500);
                // 记录抬起状态
                isUP = true;
                invalidate();
                break;
        }
        return true;
    }
}
